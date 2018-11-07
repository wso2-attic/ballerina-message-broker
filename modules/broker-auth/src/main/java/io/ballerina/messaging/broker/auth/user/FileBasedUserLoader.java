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
package io.ballerina.messaging.broker.auth.user;

import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Load users from user config file.
 */
public class FileBasedUserLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedUserLoader.class.getName());

    public static Map<String, User> loadUsers() throws ConfigurationException {

        Map<String, User> users = new HashMap<>();
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
                    users.put(userConfig.getUsername(), new User(userConfig.getUsername(),
                                                                 userConfig.getPassword().toCharArray(),
                                                                 new HashSet<>(userConfig.getRoles())));
                } else {
                    LOGGER.error("User or username can not be null");
                }
            }
        }
        return users;
    }

}
