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
 * Constants of the Broker CLI Client
 */
public class Constants {

    public static final String SYSTEM_PARAM_CLI_CLIENT_CONFIG_FILE = "client.cli.conf";
    public static final String DEFAULT_HOSTNAME = "127.0.0.1";
    public static final int DEFAULT_PORT = 9000;
    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PASSWORD = "admin";
    public static final String BROKER_CONNECTION_URL_SUFFIX = "/broker/v1.0/";
    public static final String DEFAULT_CONFIG_FILE_PATH = "cli-config.yml";

    // Commands/resource types
    public static final String CMD_INIT = "init";
    public static final String CMD_LIST = "list";
    public static final String CMD_CREATE = "create";
    public static final String CMD_DELETE = "delete";

    public static final String CMD_EXCHANGE = "exchange";
}
