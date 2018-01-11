/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.broker.core.security.authentication.util;

/**
 * Constants related to broker security
 */
public class BrokerSecurityConstants {

    private BrokerSecurityConstants() {
    }

    /*
    Broker security provider related constants
     */
    public static final String SASL_SERVER_FACTORY_PREFIX = "SaslServerFactory.";
    public static final String BROKER_SECURITY_PROVIDER_INFO = "Provider for registry AMQP SASL server factories";
    public static final double BROKER_SECURITY_PROVIDER_VERSION = 1.0;
    // The name for the amq Java Cryptography Architecture (JCA) provider. This will be used to register Sasl servers
    public static final String PROVIDER_NAME = "AMQSASLProvider";
    // System property to specify the path of the JaaS config file.
    public static final String SYSTEM_PARAM_JAAS_CONFIG = "java.security.auth.login.config";

    /*
    User store Manager related constants
   */
    // Name of the users configuration file.
    public static final String USERS_FILE_NAME = "users.yaml";
    // System property to specify the path of the users config file.
    public static final String SYSTEM_PARAM_USERS_CONFIG = "broker.users.config";
    // users configuration namespace in users config file.
    public static final String USERS_CONFIG_NAMESPACE = "broker.users";
}
