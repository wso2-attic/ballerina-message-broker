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
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
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
 * Implements functionality required to manipulate auth resources in the storage.
 */
class AuthResourceCrudOperationsDao extends BaseDao {

    AuthResourceCrudOperationsDao(DataSource dataSource) {
        super(dataSource);
    }

    void persistUserGroupMappings(Connection connection, String resourceType, String resource,
                                  Map<String, Set<String>> userGroupsMapping) throws SQLException {
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
        } finally {
            close(insertMappingsStmt);
        }
    }

    void deleteUserGroupMappings(Connection connection, String resourceType, String resource) throws SQLException {
        PreparedStatement deleteMappingsStmt = null;
        try {
            deleteMappingsStmt = connection.prepareStatement(RdbmsConstants.PS_DELETE_ALL_AUTH_RESOURCE_MAPPING);
            deleteMappingsStmt.setString(1, resourceType);
            deleteMappingsStmt.setString(2, resource);
            deleteMappingsStmt.execute();
        } finally {
            close(deleteMappingsStmt);
        }
    }

    boolean updateOwner(Connection connection, String resourceType,
                                String resourceName, String newOwner) throws SQLException {
        PreparedStatement updateResourceOwnerStmt = null;
        try {
            updateResourceOwnerStmt = connection.prepareStatement(RdbmsConstants.PS_UPDATE_AUTH_RESOURCE_OWNER);
            updateResourceOwnerStmt.setString(1, newOwner);
            updateResourceOwnerStmt.setString(2, resourceType);
            updateResourceOwnerStmt.setString(3, resourceName);
            int updateRows = updateResourceOwnerStmt.executeUpdate();

            return updateRows != 0;
        } finally {
            close(updateResourceOwnerStmt);
        }
    }

    public void storeResource(Connection connection, AuthResource authResource) throws AuthServerException {
        PreparedStatement insertAuthResourceStmt = null;
        try {
            insertAuthResourceStmt = connection.prepareStatement(RdbmsConstants.PS_INSERT_AUTH_RESOURCE);
            insertAuthResourceStmt.setString(1, authResource.getResourceType());
            insertAuthResourceStmt.setString(2, authResource.getResourceName());
            insertAuthResourceStmt.setString(3, authResource.getOwner());
            insertAuthResourceStmt.execute();

        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while persisting resource.", e);
        } finally {
            close(insertAuthResourceStmt);
        }
    }

    public boolean deleteResource(Connection connection, String resourceType, String resource)
            throws AuthServerException {
        PreparedStatement deleteAuthResourceStmt = null;
        try {
            deleteAuthResourceStmt = connection.prepareStatement(RdbmsConstants.PS_DELETE_AUTH_RESOURCE);
            deleteAuthResourceStmt.setString(1, resourceType);
            deleteAuthResourceStmt.setString(2, resource);
            int affectedRows = deleteAuthResourceStmt.executeUpdate();
            return affectedRows != 0;
        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while deleting resource.", e);
        } finally {
            close(deleteAuthResourceStmt);
        }
    }

    public boolean addGroups(Connection connection, String resourceType, String resourceName,
                             String action, List<String> groups) throws AuthServerException {
        PreparedStatement insertMappingsStmt = null;
        try {
            insertMappingsStmt = connection.prepareStatement(RdbmsConstants.PS_INSERT_AUTH_RESOURCE_MAPPING);
            for (String group : groups) {
                insertMappingsStmt.setString(1, action);
                insertMappingsStmt.setString(2, group);
                insertMappingsStmt.setString(3, resourceType);
                insertMappingsStmt.setString(4, resourceName);
                insertMappingsStmt.addBatch();
            }
            int[] updateRows = insertMappingsStmt.executeBatch();

            return updateRows.length > 0;
        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while persisting groups.", e);
        } finally {
            close(insertMappingsStmt);
        }
    }

    public boolean removeGroup(Connection connection, String resourceType, String resourceName,
                               String action, String group) throws AuthServerException {
        PreparedStatement insertMappingsStmt = null;
        try {
            insertMappingsStmt = connection.prepareStatement(RdbmsConstants.PS_DELETE_AUTH_RESOURCE_MAPPING);
            insertMappingsStmt.setString(1, resourceType);
            insertMappingsStmt.setString(2, resourceName);
            insertMappingsStmt.setString(3, action);
            insertMappingsStmt.setString(4, group);

            int updateRows = insertMappingsStmt.executeUpdate();

            return updateRows != 0;
        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while persisting resource.", e);
        } finally {
            close(insertMappingsStmt);
        }
    }

    public AuthResource read(Connection connection, String resourceType, String resourceName)
            throws AuthServerException {
        Map<String, Set<String>> actionUserGroupMap = new HashMap<>();
        String ownerId = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
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
            throw new AuthServerException("Error occurred while retrieving auth resource for resource group : " +
                                                  resourceType + " and resource : " +
                                                  resourceName, e);
        } finally {
            close(statement);
            close(resultSet);
        }
    }

    public List<AuthResource> readAll(Connection connection, String resourceType, String ownerId)
            throws AuthServerException {
        Map<String, AuthResource> resourceMap = new HashMap<>();
        String resourceName;
        String action;
        String userGroup;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
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
            return new ArrayList<>(resourceMap.values());
        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while retrieving auth data for resource group : " +
                                                  resourceType, e);
        } finally {
            close(resultSet);
            close(statement);
        }
    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<AuthResource> readAll(Connection connection, String resourceType, String action,
                                      String ownerId, List<String> userGroups) throws AuthServerException {
        Map<String, AuthResource> resourceMap = new HashMap<>();
        String userGroupsList = getSQLFormattedIdList(userGroups.size());
        String resourceName;
        String userGroup;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(
                    "SELECT r.RESOURCE_NAME, rm.USER_GROUP_ID FROM MB_AUTH_RESOURCE_MAPPING rm "
                                              + "RIGHT JOIN ( SELECT RESOURCE_NAME, RESOURCE_ID, OWNER_ID "
                                              + "FROM MB_AUTH_RESOURCE WHERE RESOURCE_TYPE =  ?) as r "
                                              + "ON r.RESOURCE_ID = rm.RESOURCE_ID WHERE r.OWNER_ID = ? OR "
                                              + "( rm.RESOURCE_ACTION = ? AND rm.USER_GROUP_ID IN (" + userGroupsList
                                              + "))");
            statement.setString(1, resourceType);
            statement.setString(2, ownerId);
            statement.setString(3, action);
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
            return new ArrayList<>(resourceMap.values());
        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while retrieving auth data for resource group : " +
                                                  resourceType, e);
        } finally {
            close(resultSet);
            close(statement);
        }
    }

    public boolean isExists(Connection connection, String resourceType, String resource) throws AuthServerException {
        String resourceId = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(RdbmsConstants.PS_SELECT_AUTH_RESOURCE);
            statement.setString(1, resourceType);
            statement.setString(2, resource);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resourceId = resultSet.getString(1);
            }
            return Objects.nonNull(resourceId);
        } catch (SQLException e) {
            throw new AuthServerException("Error occurred while retrieving existence of resource for resource "
                                                  + "group : " + resourceType + " and resource : " + resource, e);
        } finally {
            close(resultSet);
            close(statement);
        }
    }

}
