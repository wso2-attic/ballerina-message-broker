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

package io.ballerina.messaging.broker.core.store.dao.impl;

/**
 * Constants related to message store.
 */
final class RDBMSConstants {

    private RDBMSConstants() {
    }

    static final String PS_INSERT_METADATA =
            "INSERT INTO MB_METADATA (MESSAGE_ID, EXCHANGE_NAME, ROUTING_KEY, CONTENT_LENGTH, MESSAGE_METADATA) "
                    + "VALUES(?, ?, ?, ?, ?)";

    static final String PS_INSERT_CONTENT =
            "INSERT INTO MB_CONTENT (MESSAGE_ID, CONTENT_OFFSET, MESSAGE_CONTENT) VALUES(?, ?, ?)";

    public static final String PS_DELETE_FROM_QUEUE = "" +
            "DELETE FROM MB_QUEUE_MAPPING WHERE MESSAGE_ID=? AND QUEUE_NAME=?";

    public static final String PS_INSERT_INTO_QUEUE =
            "INSERT INTO MB_QUEUE_MAPPING (MESSAGE_ID, QUEUE_NAME) VALUES(?,?)";

    public static final String PS_INSERT_QUEUE =
            "INSERT INTO MB_QUEUE_METADATA (QUEUE_NAME, QUEUE_ARGUMENTS) VALUES(?,?)";

    public static final String PS_DELETE_QUEUE =
            "DELETE FROM MB_QUEUE_METADATA WHERE QUEUE_NAME=?";

    public static final String SELECT_ALL_QUEUES =
            "SELECT QUEUE_NAME, QUEUE_ARGUMENTS FROM MB_QUEUE_METADATA";

    public static final String PS_INSERT_BINDING =
            "INSERT INTO MB_BINDING (EXCHANGE_NAME, QUEUE_NAME, ROUTING_KEY, ARGUMENTS) VALUES(?, ?, ?, ?)";

    public static final String PS_DELETE_BINDING =
            "DELETE FROM MB_BINDING WHERE EXCHANGE_NAME=? AND QUEUE_NAME=? AND ROUTING_KEY=?";

    public static final String PS_SELECT_BINDINGS_FOR_EXCHANGE =
            "SELECT QUEUE_NAME, ROUTING_KEY, ARGUMENTS FROM MB_BINDING WHERE EXCHANGE_NAME=?";

    public static final String PS_INSERT_EXCHANGE =
            "INSERT INTO MB_EXCHANGE (EXCHANGE_NAME, EXCHANGE_TYPE) VALUES(?, ?)";

    public static final String PS_DELETE_EXCHANGE =
            "DELETE FROM MB_EXCHANGE WHERE EXCHANGE_NAME=? AND EXCHANGE_TYPE=?";

    public static final String SELECT_ALL_EXCHANGES =
            "SELECT EXCHANGE_NAME, EXCHANGE_TYPE FROM MB_EXCHANGE";

    public static final String PS_DELETE_MESSAGE = "DELETE FROM MB_METADATA WHERE MESSAGE_ID=?";

    public static final String PS_SELECT_MESSAGES_FOR_QUEUE =
            "SELECT MB_QUEUE_MAPPING.MESSAGE_ID, QUEUE_NAME "
                    + "FROM (SELECT MESSAGE_ID FROM MB_QUEUE_MAPPING WHERE QUEUE_NAME=?) AS QUEUE_MESSAGES "
                    + "INNER JOIN MB_QUEUE_MAPPING "
                    + "ON QUEUE_MESSAGES.MESSAGE_ID=MB_QUEUE_MAPPING.MESSAGE_ID "
                    + "ORDER BY QUEUE_MESSAGES.MESSAGE_ID";
}
