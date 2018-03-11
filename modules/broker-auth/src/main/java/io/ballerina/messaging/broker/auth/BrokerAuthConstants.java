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
package io.ballerina.messaging.broker.auth;

/**
 * Constants related to broker auth.
 */
public final class BrokerAuthConstants {

    private BrokerAuthConstants() {
    }

    /*
    Broker security provider related constants
     */
    public static final String SASL_SERVER_FACTORY_PREFIX = "SaslServerFactory.";

    public static final String BROKER_SECURITY_PROVIDER_INFO = "Provider for registry AMQP SASL server factories";

    public static final double BROKER_SECURITY_PROVIDER_VERSION = 1.0;
    // broker authenticator instance
    public static final String PROPERTY_AUTHENTICATOR_INSTANCE = "broker.authenticator";
    // user store connector property name
    public static final String CONFIG_PROPERTY_JAAS_LOGIN_MODULE = "loginModule";
    // System property to specify the path of the JaaS config file.
    public static final String SYSTEM_PARAM_JAAS_CONFIG = "java.security.auth.login.config";
    // Broker Jaas auth config
    public static final String BROKER_SECURITY_CONFIG = "BrokerSecurityConfig";

    /*
    User store Manager related constants
   */
    // Name of the users configuration file.
    public static final String USERS_FILE_NAME = "users.yaml";
    // System property to specify the path of the users config file.
    public static final String SYSTEM_PARAM_USERS_CONFIG = "broker.users.config";
    // users configuration namespace in users config file.
    public static final String USERS_CONFIG_NAMESPACE = "wso2.broker.users";
    // user manager property name
    public static final String PROPERTY_USER_STORE_CONNECTOR = "broker.user.store.connector";
    // user manager authorization id
    public static final String AUTHENTICATION_ID = "AuthenticationId";
}
