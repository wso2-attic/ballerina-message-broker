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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.AuthResourceDao;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthServerException;
import io.ballerina.messaging.broker.common.BaseDao;

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
 * Class implements {@link AuthResourceDao} to provide database functionality to manage auth resources.
 */
public class AuthResourceRdbmsDao extends BaseDao implements AuthResourceDao {

    public AuthResourceRdbmsDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void persist(AuthResource authResource) throws BrokerAuthServerException {
        Connection connection = null;
        PreparedStatement insertAuthResourceStmt = null;
        try {
            connection = getConnection();
            insertAuthResourceStmt = connection.prepareStatement(RdbmsConstants.PS_INSERT_AUTH_RESOURCE);
            insertAuthResourceStmt.setString(1, authResource.getResourceType());
            insertAuthResourceStmt.setString(2, authResource.getResourceName());
            insertAuthResourceStmt.setString(3, authResource.getOwner());
            insertAuthResourceStmt.execute();
            persistUserGroupMappings(authResource.getResourceType(), authResource.getResourceName(),
                                     authResource.getActionsUserGroupsMap(),
                                     connection);
            connection.commit();
        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while persisting resource.", e);
        } finally {
            close(connection, insertAuthResourceStmt);
        }
    }

    @Override
    public void update(AuthResource authResource) throws BrokerAuthServerException {
        Connection connection = null;
        try {
            connection = getConnection();
            updateOwner(connection,
                        authResource.getResourceType(),
                        authResource.getResourceName(),
                        authResource.getOwner());
            deleteUserGroupMappings(authResource.getResourceType(), authResource.getResourceName(), connection);
            persistUserGroupMappings(authResource.getResourceType(),
                                     authResource.getResourceName(),
                                     authResource.getActionsUserGroupsMap(),
                                     connection);
            connection.commit();
        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while persisting resource.", e);
        } finally {
            close(connection);
        }
    }

    @Override
    public boolean delete(String resourceType, String resource) throws BrokerAuthServerException {
        Connection connection = null;
        PreparedStatement deleteAuthResourceStmt = null;
        try {
            connection = getConnection();
            deleteAuthResourceStmt = connection.prepareStatement(RdbmsConstants.PS_DELETE_AUTH_RESOURCE);
            deleteAuthResourceStmt.setString(1, resourceType);
            deleteAuthResourceStmt.setString(2, resource);
            int affectedRows = deleteAuthResourceStmt.executeUpdate();
            connection.commit();
            return affectedRows != 0;
        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while deleting resource.", e);
        } finally {
            close(connection, deleteAuthResourceStmt);
        }
    }

    @Override
    public AuthResource read(String resourceType, String resourceName) throws BrokerAuthServerException {
        Map<String, Set<String>> actionUserGroupMap = new HashMap<>();
        String ownerId = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RdbmsConstants.PS_SELECT_AUTH_RESOURCE_MAPPING);
            statement.setString(1, resourceType);
            statement.setString(2, resourceName);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if (Objects.isNull(ownerId)) {
                    ownerId = resultSet.getString(1);
                }
                String action = resultSet.getString(2);
                if (Objects.nonNull(action)) {
                    Set<String> authorisedGroups = actionUserGroupMap.get(action);
                    if (Objects.isNull(authorisedGroups)) {
                        authorisedGroups = new HashSet<>();
                        actionUserGroupMap.put(action, authorisedGroups);
                    }
                    authorisedGroups.add(resultSet.getString(3));
                }
            }
            if (Objects.nonNull(ownerId)) {
                return new AuthResource(resourceType,
                                        resourceName,
                                        true,
                                        ownerId,
                                        actionUserGroupMap);
            }
            return null;
        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while retrieving auth resource for resource group : " +
                                                        resourceType + " and resource : " +
                                                        resourceName, e);
        } finally {
            close(connection, statement, resultSet);
        }
    }

    @Override
    public List<AuthResource> readAll(String resourceType, String ownerId) throws BrokerAuthServerException {
        Map<String, AuthResource> resourceMap = new HashMap<>();
        String resourceName, action, userGroup;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RdbmsConstants.PS_SELECT_ALL_AUTH_RESOURCE_MAPPING_BY_TYPE_OWNER);
            statement.setString(1, resourceType);
            statement.setString(2, ownerId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resourceName = resultSet.getString(1);
                action = resultSet.getString(2);
                userGroup = resultSet.getString(3);
                AuthResource authResource = resourceMap.get(resourceName);
                if (Objects.isNull(authResource)) {
                    authResource = new AuthResource(resourceType, resourceName, true, ownerId);
                    resourceMap.put(resourceName, authResource);
                }
                if (Objects.nonNull(action)) {
                    Set<String> userGroups = authResource.getActionsUserGroupsMap().get(action);
                    if (Objects.isNull(userGroups)) {
                        userGroups = new HashSet<>();
                        authResource.getActionsUserGroupsMap().put(action, userGroups);
                    }
                    userGroups.add(userGroup);
                }
            }
        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while retrieving auth data for resource group : " +
                                                        resourceType, e);
        } finally {
            close(connection, statement, resultSet);
        }
        return new ArrayList<>(resourceMap.values());
    }

    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<AuthResource> readAll(String resourceType, String action, String ownerId, List<String> userGroups)
            throws
            BrokerAuthServerException {
        Map<String, AuthResource> resourceMap = new HashMap<>();
        String userGroupsList = getSQLFormattedIdList(userGroups.size());
        String resourceName, userGroup;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection
                    .prepareStatement("SELECT r.RESOURCE_NAME, rm.USER_GROUP_ID FROM MB_AUTH_RESOURCE_MAPPING rm "
                                              + "RIGHT JOIN ( SELECT RESOURCE_NAME, RESOURCE_ID, OWNER_ID "
                                              + "FROM MB_AUTH_RESOURCE WHERE RESOURCE_TYPE =  ?) as r "
                                              + "ON r.RESOURCE_ID = rm.RESOURCE_ID WHERE r.OWNER_ID = ? OR "
                                              + "( rm.RESOURCE_ACTION = ? AND rm.USER_GROUP_ID IN (" + userGroupsList
                                              + "))");
            statement.setString(1, resourceType);
            statement.setString(2, action);
            statement.setString(3, ownerId);
            for (int i = 0; i < userGroups.size(); i++) {
                statement.setString(i + 4, userGroups.get(i));
            }
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resourceName = resultSet.getString(1);
                userGroup = resultSet.getString(2);
                AuthResource authResource = resourceMap.get(resourceName);
                if (Objects.isNull(authResource)) {
                    authResource = new AuthResource(resourceType, resourceName, true, ownerId);
                    resourceMap.put(resourceName, authResource);
                }
                if (Objects.nonNull(userGroup)) {
                    Set<String> authorizedUserGroups = authResource.getActionsUserGroupsMap().get(action);
                    if (Objects.isNull(authorizedUserGroups)) {
                        authorizedUserGroups = new HashSet<>();
                        authResource.getActionsUserGroupsMap().put(action, authorizedUserGroups);
                    }
                    authorizedUserGroups.add(userGroup);
                }
            }
        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while retrieving auth data for resource group : " +
                                                        resourceType, e);
        } finally {
            close(connection, statement, resultSet);
        }
        return new ArrayList<>(resourceMap.values());
    }

    @Override
    public boolean isExists(String resourceType, String resource) throws BrokerAuthServerException {
        String resourceId = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RdbmsConstants.PS_SELECT_AUTH_RESOURCE);
            statement.setString(1, resourceType);
            statement.setString(2, resource);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resourceId = resultSet.getString(1);
            }
        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while retrieving existence of resource for resource "
                                                        + "group : " + resourceType + " and resource : " + resource, e);
        } finally {
            close(connection, statement, resultSet);
        }
        return Objects.nonNull(resourceId);
    }

    private void updateOwner(Connection connection, String resourceType, String resourceName, String newOwner)
            throws BrokerAuthServerException {
        PreparedStatement updateResourceOwnerStmt = null;
        try {
            updateResourceOwnerStmt = connection.prepareStatement(RdbmsConstants.PS_UPDATE_AUTH_RESOURCE_OWNER);
            updateResourceOwnerStmt.setString(1, newOwner);
            updateResourceOwnerStmt.setString(2, resourceType);
            updateResourceOwnerStmt.setString(3, resourceName);
            updateResourceOwnerStmt.execute();
        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while persisting resource.", e);
        } finally {
            close(updateResourceOwnerStmt);
        }
    }

    @Override
    public void updateOwner(String resourceType, String resourceName, String newOwner) throws
            BrokerAuthServerException {
        Connection connection = null;
        try {
            connection = getConnection();
            updateOwner(connection, resourceType, resourceName, newOwner);
        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while persisting resource.", e);
        } finally {
            close(connection);
        }
    }

    @Override
    public void addGroup(String resourceType, String resourceName, String action, String group)
            throws BrokerAuthServerException {
        Connection connection = null;
        PreparedStatement insertMappingsStmt = null;
        try {
            connection = getConnection();
            insertMappingsStmt = connection.prepareStatement(RdbmsConstants.PS_INSERT_AUTH_RESOURCE_MAPPING);
            insertMappingsStmt.setString(1, action);
            insertMappingsStmt.setString(2, group);
            insertMappingsStmt.setString(3, resourceType);
            insertMappingsStmt.setString(4, resourceName);

            insertMappingsStmt.executeUpdate();

        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while persisting resource.", e);
        } finally {
            close(connection, insertMappingsStmt);
        }

    }

    @Override
    public void removeGroup(String resourceType, String resourceName, String action, String group)
            throws BrokerAuthServerException {
        Connection connection = null;
        PreparedStatement insertMappingsStmt = null;
        try {
            connection = getConnection();
            insertMappingsStmt = connection.prepareStatement(RdbmsConstants.PS_DELETE_AUTH_RESOURCE_MAPPING);
            insertMappingsStmt.setString(1, resourceType);
            insertMappingsStmt.setString(2, resourceName);
            insertMappingsStmt.setString(3, action);
            insertMappingsStmt.setString(4, group);

            insertMappingsStmt.executeUpdate();

        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while persisting resource.", e);
        } finally {
            close(connection, insertMappingsStmt);
        }
    }

    private void persistUserGroupMappings(String resourceType, String resource,
                                          Map<String, Set<String>> userGroupsMapping,
                                          Connection connection) throws BrokerAuthServerException {
        PreparedStatement insertMappingsStmt = null;
        try {
            insertMappingsStmt = connection.prepareStatement(RdbmsConstants.PS_INSERT_AUTH_RESOURCE_MAPPING);
            for (Map.Entry<String, Set<String>> mapping : userGroupsMapping.entrySet()) {
                Set<String> userGroups = mapping.getValue();
                for (String userGroup : userGroups) {
                    insertMappingsStmt.setString(1, mapping.getKey());
                    insertMappingsStmt.setString(2, userGroup);
                    insertMappingsStmt.setString(3, resourceType);
                    insertMappingsStmt.setString(4, resource);
                    insertMappingsStmt.addBatch();
                }
            }
            insertMappingsStmt.executeBatch();
        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while persisting auth resource user groups.", e);
        } finally {
            close(insertMappingsStmt);
        }
    }

    private void deleteUserGroupMappings(String resourceType, String resource, Connection connection)
            throws BrokerAuthServerException {
        PreparedStatement deleteMappingsStmt = null;
        try {
            deleteMappingsStmt = connection.prepareStatement(RdbmsConstants.PS_DELETE_ALL_AUTH_RESOURCE_MAPPING);
            deleteMappingsStmt.setString(1, resourceType);
            deleteMappingsStmt.setString(2, resource);
            deleteMappingsStmt.execute();
        } catch (SQLException e) {
            throw new BrokerAuthServerException("Error occurred while deleting auth resource user groups.", e);
        } finally {
            close(deleteMappingsStmt);
        }
    }
}
