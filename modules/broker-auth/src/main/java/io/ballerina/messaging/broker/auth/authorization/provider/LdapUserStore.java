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
package io.ballerina.messaging.broker.auth.authorization.provider;

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.auth.ldap.LdapAuthHandler;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.naming.NamingException;

/**
 * This class implements @{@link UserStore} to provide a ldap based user store.
 */
public class LdapUserStore implements UserStore {

    private LdapAuthHandler ldapAuthHandler;

    @Override
    public void initialize(StartupContext startupContext, Map<String, String> properties) throws Exception {

        BrokerConfigProvider configProvider = startupContext.getService(BrokerConfigProvider.class);
        BrokerAuthConfiguration brokerAuthConfiguration = configProvider.getConfigurationObject(
                BrokerAuthConfiguration.NAMESPACE, BrokerAuthConfiguration.class);
        BrokerAuthConfiguration.LdapConfiguration ldapConfiguration = brokerAuthConfiguration.getAuthorization()
                .getUserStore().getLdap();

        ldapAuthHandler = new LdapAuthHandler(ldapConfiguration);
    }

    @Override
    public boolean isUserExists(String username) throws AuthException {

        boolean exists = false;
        try {
            if (Objects.nonNull(ldapAuthHandler.searchDN(username))) {
                exists = true;
            }
        } catch (NamingException e) {
            throw new AuthException("Error while searching Username: " + username, e);
        }

        return exists;
    }

    @Override
    public Set<String> getUserGroupsList(String username) throws AuthException {

        String dn;
        try {
            dn = ldapAuthHandler.searchDN(username);
        } catch (NamingException e) {
            throw new AuthException("Error while searching Username: " + username, e);
        }
        try {
            return ldapAuthHandler.getUserGroups(dn);
        } catch (NamingException e) {
            throw new AuthException("Error while fetching groups for Username: " + username, e);
        }
    }
}
