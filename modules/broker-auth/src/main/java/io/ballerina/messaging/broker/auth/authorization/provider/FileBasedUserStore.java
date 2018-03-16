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
import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
import io.ballerina.messaging.broker.auth.authentication.AuthResult;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.auth.user.config.UserConfig;
import io.ballerina.messaging.broker.auth.user.config.UsersFile;
import io.ballerina.messaging.broker.auth.user.dto.User;
import io.ballerina.messaging.broker.common.StartupContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements @{@link UserStore} to connect to file based user store.
 */
public class FileBasedUserStore implements UserStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedUserStore.class);

    /**
     * Store the map of userRegistry.
     */
    private static Map<String, User> userRegistry = new ConcurrentHashMap<>();

    @Override
    public void initialize(StartupContext startupContext, Map<String, String> properties) throws Exception {
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
     * @param username    userName
     * @param credentials Credentials
     * @return Authentication result
     * @throws AuthException Exception throws when authentication failed.
     */
    @Override
    public AuthResult authenticate(String username, char... credentials) throws AuthException {
        User user;
        if (Objects.isNull(username)) {
            throw new AuthException("Username cannot be null.");
        } else if (Objects.isNull(user = userRegistry.get(username))) {
            throw new AuthException("User not found for the given username.");
        } else {
            if (Arrays.equals(credentials, user.getPassword())) {
                return new AuthResult(true, username);
            } else {
                throw new AuthException("Password did not match with the configured user");
            }
        }
    }

    @Override
    public boolean isUserExists(String username) {
        return Objects.nonNull(userRegistry.get(username));
    }

    /**
     * Retrieve the list of userRegistry for given username.
     *
     * @param userName user name
     * @return List of roles
     */
    @Override
    public Set<String> getUserGroupsList(String userName) {
        User user = userRegistry.get(userName);
        if (user != null) {
            return user.getRoles();
        }
        return Collections.emptySet();
    }
}
