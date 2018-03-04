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

package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope.dao.impl;

/**
 * Constants related to auth scope persistence.
 */
final class RdbmsConstants {

    private RdbmsConstants() {
    }

    // Broker scope related sql queries
    static final String PS_INSERT_AUTH_SCOPE_GROUPS =
            "INSERT INTO MB_AUTH_SCOPE_MAPPING (SCOPE_ID, USER_GROUP_ID) "
                    + "SELECT SCOPE_ID , ? FROM MB_AUTH_SCOPE WHERE SCOPE_NAME = ? ";

    static final String PS_DELETE_ALL_AUTH_SCOPE_GROUPS =
            "DELETE FROM MB_AUTH_SCOPE_MAPPING "
                    + "WHERE SCOPE_ID IN ( SELECT SCOPE_ID FROM MB_AUTH_SCOPE WHERE SCOPE_NAME = ?) ";

    static final String PS_SELECT_AUTH_SCOPE =
            "SELECT s.SCOPE_NAME, sm.USER_GROUP_ID FROM MB_AUTH_SCOPE_MAPPING sm "
                    + "RIGHT JOIN ( SELECT SCOPE_ID, SCOPE_NAME FROM MB_AUTH_SCOPE WHERE SCOPE_NAME = ?) s "
                    + "ON s.SCOPE_ID = sm.SCOPE_ID";

    static final String PS_SELECT_ALL_AUTH_SCOPES =
            "SELECT s.SCOPE_NAME, sm.USER_GROUP_ID "
                    + "FROM MB_AUTH_SCOPE_MAPPING sm "
                    + "RIGHT JOIN MB_AUTH_SCOPE s ON s.SCOPE_ID = sm.SCOPE_ID";
}
