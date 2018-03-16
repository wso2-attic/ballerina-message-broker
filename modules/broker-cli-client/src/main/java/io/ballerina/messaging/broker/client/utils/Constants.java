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
package io.ballerina.messaging.broker.client.utils;

/**
 * Constants of the Broker CLI Client.
 */
public class Constants {

    public static final String SYSTEM_PARAM_CLI_CLIENT_CONFIG_FILE = "client.cli.conf";
    public static final String DEFAULT_HOSTNAME = "127.0.0.1";
    public static final int DEFAULT_PORT = 9000;
    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PASSWORD = "admin";
    public static final String BROKER_CONNECTION_URL_SUFFIX = "/broker/v1.0/";
    public static final String QUEUES_URL_PARAM = "queues/";
    public static final String BINDINGS_URL_PARAM = "/bindings/";
    public static final String CONSUMERS_URL_PARAM = "/consumers/";
    public static final String EXCHANGES_URL_PARAM = "exchanges/";
    public static final String PERMISSIONS_ACTION_URL_PARAM = "/permissions/actions/";
    public static final String PERMISSIONS_OWNER_URL_PARAM = "/permissions/owner";
    public static final String PERMISSION_GROUP_URL_PARAM = "/groups";
    public static final String DEFAULT_CONFIG_FILE_PATH = "cli-config.yml";

    // Commands/resource types
    public static final String CMD_INIT = "init";
    public static final String CMD_LIST = "list";
    public static final String CMD_CREATE = "create";
    public static final String CMD_DELETE = "delete";
    public static final String CMD_GRANT = "grant";
    public static final String CMD_REVOKE = "revoke";
    public static final String CMD_TRANSFER = "transfer";

    public static final String CMD_EXCHANGE = "exchange";
    public static final String CMD_QUEUE = "queue";
    public static final String CMD_BINDING = "binding";
    public static final String CMD_CONSUMER = "consumer";

    public static final String BROKER_ERROR_MSG = "Error while invoking Brokers admin services";

    // http constants
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
}
