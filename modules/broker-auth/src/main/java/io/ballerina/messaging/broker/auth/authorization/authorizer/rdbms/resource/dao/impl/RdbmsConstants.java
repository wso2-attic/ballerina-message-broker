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

/**
 * Constants related to auth resource store database.
 */
final class RdbmsConstants {

    private RdbmsConstants() {
    }

    // Broker resource related sql queries
    static final String PS_INSERT_AUTH_RESOURCE =
            "INSERT INTO MB_AUTH_RESOURCE "
                    + "(RESOURCE_TYPE, RESOURCE_NAME, OWNER_ID) VALUES (?, ?, ?)";

    static final String PS_UPDATE_AUTH_RESOURCE_OWNER =
            "UPDATE MB_AUTH_RESOURCE SET OWNER_ID = ? "
                    + "WHERE RESOURCE_TYPE = ? AND RESOURCE_NAME= ?";

    static final String PS_DELETE_AUTH_RESOURCE =
            "DELETE FROM MB_AUTH_RESOURCE "
                    + "WHERE RESOURCE_TYPE = ? AND RESOURCE_NAME= ?";

    static final String PS_INSERT_AUTH_RESOURCE_MAPPING =
            "INSERT INTO MB_AUTH_RESOURCE_MAPPING ( RESOURCE_ID, RESOURCE_ACTION, USER_GROUP_ID) "
                    + "SELECT RESOURCE_ID, ?, ? FROM MB_AUTH_RESOURCE "
                    + "WHERE RESOURCE_TYPE = ? AND RESOURCE_NAME = ?";

    static final String PS_DELETE_ALL_AUTH_RESOURCE_MAPPING =
            "DELETE FROM MB_AUTH_RESOURCE_MAPPING "
                    + "WHERE RESOURCE_ID IN "
                    + "( SELECT RESOURCE_ID FROM MB_AUTH_RESOURCE WHERE RESOURCE_TYPE = ? AND RESOURCE_NAME = ? )";

    static final String PS_DELETE_AUTH_RESOURCE_MAPPING =
            "DELETE FROM MB_AUTH_RESOURCE_MAPPING "
                    + "WHERE RESOURCE_ID IN "
                    + "( SELECT RESOURCE_ID FROM MB_AUTH_RESOURCE WHERE RESOURCE_TYPE = ? AND RESOURCE_NAME = ? ) AND"
                    + " RESOURCE_ACTION = ? AND USER_GROUP_ID = ?";

    static final String PS_SELECT_AUTH_RESOURCE =
            "SELECT RESOURCE_ID FROM MB_AUTH_RESOURCE WHERE RESOURCE_TYPE = ? AND RESOURCE_NAME = ?";

    static final String PS_SELECT_AUTH_RESOURCE_MAPPING =
            "SELECT r.OWNER_ID, rm.RESOURCE_ACTION, rm.USER_GROUP_ID FROM MB_AUTH_RESOURCE_MAPPING rm "
                    + "RIGHT JOIN ( SELECT OWNER_ID,RESOURCE_ID FROM MB_AUTH_RESOURCE "
                    + "WHERE RESOURCE_TYPE = ? AND RESOURCE_NAME = ?) as r "
                    + "ON r.RESOURCE_ID = rm.RESOURCE_ID";

    static final String PS_SELECT_ALL_AUTH_RESOURCE_MAPPING_BY_TYPE_OWNER =
            "SELECT r.RESOURCE_NAME, rm.RESOURCE_ACTION, rm.USER_GROUP_ID FROM MB_AUTH_RESOURCE_MAPPING rm "
                    + "RIGHT JOIN ( SELECT RESOURCE_NAME, RESOURCE_ID  FROM MB_AUTH_RESOURCE "
                    + "WHERE RESOURCE_TYPE = ? AND OWNER_ID = ? ) r ON r.RESOURCE_ID = rm.RESOURCE_ID ";
}
