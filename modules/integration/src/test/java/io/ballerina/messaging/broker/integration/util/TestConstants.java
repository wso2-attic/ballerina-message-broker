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

package io.ballerina.messaging.broker.integration.util;

/**
 * Test Constants class.
 */
public class TestConstants {
    public static final String KEYSTORE_LOCATION = "src/test/resources/security/keystore.jks";
    public static final String TRUST_STORE_LOCATION = "src/test/resources/security/client-truststore.jks";
    public static final String KEYSTORE_PASSWORD = "wso2carbon";
    public static final String TRUST_STORE_PASSWORD = "wso2carbon";
    public static final String CLI_CONFIG_SYSTEM_PROPERTY = "client.cli.conf";
    public static final String CLI_CONFIG_LOCATION = "src/test/resources/config/cli-config.yaml";
}
