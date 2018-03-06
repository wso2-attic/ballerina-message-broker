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

import io.ballerina.messaging.broker.amqp.AmqpServerConfiguration;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
import io.ballerina.messaging.broker.auth.authentication.authenticator.JaasAuthenticator;
import io.ballerina.messaging.broker.auth.authentication.jaas.UserStoreLoginModule;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerCommonConfiguration;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.rest.config.RestServerConfiguration;
import org.wso2.carbon.config.ConfigurationException;

import java.net.URL;
import java.util.HashMap;

/**
 * Contains shared helper method used across the integration test suite.
 */
public class TestUtils {

    public static StartupContext initStartupContext(String port, String sslPort, String hostname, String restPort)
            throws ConfigurationException {
        StartupContext startupContext = new StartupContext();
        TestConfigProvider configProvider = new TestConfigProvider();

        BrokerCommonConfiguration commonConfig = new BrokerCommonConfiguration();
        configProvider.registerConfigurationObject(BrokerCommonConfiguration.NAMESPACE,
                                                   commonConfig);

        BrokerCoreConfiguration brokerCoreConfiguration = new BrokerCoreConfiguration();
        brokerCoreConfiguration.setDurableQueueInMemoryCacheLimit("1000");
        configProvider.registerConfigurationObject(BrokerCoreConfiguration.NAMESPACE, brokerCoreConfiguration);

        AmqpServerConfiguration serverConfiguration = new AmqpServerConfiguration();
        serverConfiguration.setHostName(hostname);
        serverConfiguration.getPlain().setPort(port);
        serverConfiguration.getSsl().setEnabled(true);
        serverConfiguration.getSsl().setPort(sslPort);
        serverConfiguration.getSsl().getKeyStore().setLocation(TestConstants.KEYSTORE_LOCATION);
        serverConfiguration.getSsl().getKeyStore().setPassword(TestConstants.KEYSTORE_PASSWORD);
        serverConfiguration.getSsl().getTrustStore().setLocation(TestConstants.TRUST_STORE_LOCATION);
        serverConfiguration.getSsl().getTrustStore().setPassword(TestConstants.TRUST_STORE_PASSWORD);
        configProvider.registerConfigurationObject(AmqpServerConfiguration.NAMESPACE, serverConfiguration);

        RestServerConfiguration restConfig = new RestServerConfiguration();
        restConfig.getPlain().setPort(restPort);
        configProvider.registerConfigurationObject(RestServerConfiguration.NAMESPACE, restConfig);
        startupContext.registerService(BrokerConfigProvider.class, configProvider);

        // Auth configurations
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        URL resource = classLoader.getResource(BrokerAuthConstants.USERS_FILE_NAME);
        if (resource != null) {
            System.setProperty(BrokerAuthConstants.SYSTEM_PARAM_USERS_CONFIG, resource.getFile());
        }


        BrokerAuthConfiguration brokerAuthConfiguration = new BrokerAuthConfiguration();
        BrokerAuthConfiguration.AuthenticationConfiguration authenticationConfiguration =
                new BrokerAuthConfiguration.AuthenticationConfiguration();
        BrokerAuthConfiguration.AuthenticatorConfiguration authenticatorConfiguration =
                new BrokerAuthConfiguration.AuthenticatorConfiguration();
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(BrokerAuthConstants.CONFIG_PROPERTY_JAAS_LOGIN_MODULE,
                       UserStoreLoginModule.class.getCanonicalName());
        authenticatorConfiguration.setClassName(JaasAuthenticator.class.getCanonicalName());
        authenticatorConfiguration.setProperties(properties);
        authenticationConfiguration.setAuthenticator(authenticatorConfiguration);
        brokerAuthConfiguration.setAuthentication(authenticationConfiguration);
        configProvider.registerConfigurationObject(BrokerAuthConfiguration.NAMESPACE, brokerAuthConfiguration);

        return startupContext;
    }
}
