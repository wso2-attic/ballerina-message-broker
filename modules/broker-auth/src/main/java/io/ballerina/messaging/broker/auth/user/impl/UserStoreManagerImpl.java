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
package io.ballerina.messaging.broker.auth.user.impl;

import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
import io.ballerina.messaging.broker.auth.BrokerAuthException;
import io.ballerina.messaging.broker.auth.user.UserStoreManager;
import io.ballerina.messaging.broker.auth.user.config.UserConfig;
import io.ballerina.messaging.broker.auth.user.config.UsersFile;
import io.ballerina.messaging.broker.auth.user.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements @{@link UserStoreManager} to provide file based user store manager.
 */
public class UserStoreManagerImpl implements UserStoreManager {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UserStoreManagerImpl.class);

    /**
     * Store the map of userRegistry.
     */
    private static Map<String, User> userRegistry = new ConcurrentHashMap<>();

    public UserStoreManagerImpl() throws ConfigurationException {
        Path usersYamlFile;
        String usersFilePath = System.getProperty(BrokerAuthConstants.SYSTEM_PARAM_USERS_CONFIG);
        if (usersFilePath == null || usersFilePath.trim().isEmpty()) {
            // use current path.
            usersYamlFile = Paths.get("", BrokerAuthConstants.USERS_FILE_NAME).toAbsolutePath();
        } else {
            usersYamlFile = Paths.get(usersFilePath).toAbsolutePath();
        }
        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(usersYamlFile, null);
        UsersFile usersFile = configProvider
                .getConfigurationObject(BrokerAuthConstants.USERS_CONFIG_NAMESPACE, UsersFile.class);
        if (usersFile != null) {
            List<UserConfig> usersList = usersFile.getUserConfigs();
            for (UserConfig userConfig : usersList) {
                if (userConfig != null && userConfig.getUsername() != null) {
                    userRegistry.put(userConfig.getUsername(), new User(userConfig.getUsername(),
                                                                        userConfig.getPassword().toCharArray(),
                                                                        new HashSet<>(userConfig.getRoles())));
                } else {
                    LOGGER.error("User or username can not be null");
                }
            }
        }
    }

    /**
     * Authenticate given user with credentials.
     *
     * @param userName    userName
     * @param credentials Credentials
     * @return Authentication result
     * @throws BrokerAuthException Exception throws when authentication failed.
     */
    @Override
    public boolean authenticate(String userName, char... credentials) throws BrokerAuthException {
        User user;
        if (Objects.isNull(userName)) {
            throw new BrokerAuthException("Username cannot be null.");
        } else if (Objects.isNull(user = userRegistry.get(userName))) {
            throw new BrokerAuthException("User not found for the given username.");
        } else {
            if (credentials != null && Arrays.equals(credentials, user.getPassword())) {
                return true;
            } else {
                throw new BrokerAuthException("Password did not match with the configured user");
            }
        }
    }

    /**
     * Retrieve the list of userRegistry for given username.
     *
     * @param userName user name
     * @return List of roles
     */
    @Override
    public Set<String> getUserRoleList(String userName) {
        User user = userRegistry.get(userName);
        if (user != null) {
            return user.getRoles();
        }
        return null;
    }
}
