/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope.dao.impl;

import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope.AuthScope;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope.dao.AuthScopeDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.sql.DataSource;

/**
 * Class implements {@link AuthScopeDao} to provide database functionality to manage scopes.
 */
public class AuthScopeRdbmsDao extends AuthScopeDao {

    public AuthScopeRdbmsDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public AuthScope read(String scopeName) throws AuthServerException {
        Set<String> userGroups = new HashSet<>();
        String scopeId = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RdbmsConstants.PS_SELECT_AUTH_SCOPE);
            statement.setString(1, scopeName);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if (Objects.isNull(scopeId)) {
                    scopeId = resultSet.getString(1);
                }
                String userGroup = resultSet.getString(2);
                if (Objects.nonNull(userGroup)) {
                    userGroups.add(userGroup);
                }
            }
        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while retrieving scope for name : " + scopeName, e);
        } finally {
            close(connection, statement, resultSet);
        }

        if (Objects.nonNull(scopeId)) {
            return new AuthScope(scopeName, userGroups);
        }
        return null;
    }

    @Override
    public List<AuthScope> readAll() throws AuthServerException {
        Map<String, AuthScope> authScopes = new HashMap<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RdbmsConstants.PS_SELECT_ALL_AUTH_SCOPES);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String scopeName = resultSet.getString(1);
                AuthScope authScope = authScopes.get(scopeName);
                if (Objects.isNull(authScope)) {
                    authScope = new AuthScope(scopeName, new HashSet<>());
                    authScopes.put(scopeName, authScope);
                }
                if (Objects.nonNull(resultSet.getString(2))) {
                    authScope.addUserGroup(resultSet.getString(2));
                }
            }
        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while retrieving scopes", e);
        } finally {
            close(connection, statement, resultSet);
        }

        return new ArrayList<>(authScopes.values());

    }

    @Override
    public void update(String scopeName, List<String> userGroups) throws AuthServerException {
        Connection connection = null;
        try {
            connection = getConnection();
            deleteGroups(scopeName, connection);
            persistGroups(scopeName, userGroups, connection);
            connection.commit();
        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while updating groups for scope name : " +
                                                        scopeName, e);
        } finally {
            close(connection);
        }
    }

    private void persistGroups(String scopeName, List<String> userGroups, Connection connection)
            throws AuthServerException {
        PreparedStatement insertUserGroupsStmt = null;
        try {
            insertUserGroupsStmt = connection.prepareStatement(RdbmsConstants.PS_INSERT_AUTH_SCOPE_GROUPS);
            for (String userGroup : userGroups) {
                insertUserGroupsStmt.setString(1, userGroup);
                insertUserGroupsStmt.setString(2, scopeName);
                insertUserGroupsStmt.addBatch();
            }
            insertUserGroupsStmt.executeBatch();
        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while persisting groups for scope name : " +
                                                        scopeName, e);
        } finally {
            close(insertUserGroupsStmt);
        }
    }

    private void deleteGroups(String scopeName, Connection connection) throws AuthServerException {
        PreparedStatement deleteUserGroupsStmt = null;
        try {
            deleteUserGroupsStmt = connection.prepareStatement(RdbmsConstants.PS_DELETE_ALL_AUTH_SCOPE_GROUPS);
            deleteUserGroupsStmt.setString(1, scopeName);
            deleteUserGroupsStmt.execute();
        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while deleting user groups scope for name : " +
                                                        scopeName, e);
        } finally {
            close(deleteUserGroupsStmt);
        }
    }
}
