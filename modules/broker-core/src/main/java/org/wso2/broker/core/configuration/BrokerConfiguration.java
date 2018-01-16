/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.core.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents configuration for broker.
 */
public class BrokerConfiguration {

    /**
     * Namespace used in config file
     */
    public static final String NAMESPACE = "org.wso2.broker";
    /**
     * Name of the configuration file.
     */
    public static final String BROKER_FILE_NAME = "broker.yaml";
    
    /**
     * system property to specify the path of the broker configuration file.
     */
    public static final String SYSTEM_PARAM_BROKER_CONFIG_FILE = "broker.config";

    private DataSourceConfiguration dataSource;

    private AuthenticationConfiguration authenticator;
    
    public DataSourceConfiguration getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSourceConfiguration dataSource) {
        this.dataSource = dataSource;
    }

    public AuthenticationConfiguration getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(AuthenticationConfiguration authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Represents a dataSource configuration for broker ( e.g. database)
     */
    public static class DataSourceConfiguration {

        private String url;

        private String databaseDriver;

        private String user;

        private String password;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDatabaseDriver() {
            return databaseDriver;
        }

        public void setDatabaseDriver(String databaseDriver) {
            this.databaseDriver = databaseDriver;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "DataSourceConfigurations [url=" + url + ", databaseDriver=" + databaseDriver + ", user=" + user
                    + ", password=" + password + "]";
        }

    }

    /**
     * Represents a authentication configuration for broker ( e.g. database)
     */
    public static class AuthenticationConfiguration {
        /**
         * Jaas login module class name
         */
        private String loginModule;

        /**
         * Jaas login module options
         */
        private Map<String, String> options = new HashMap<>();

        public String getLoginModule() {
            return loginModule;
        }

        public void setLoginModule(String loginModule) {
            this.loginModule = loginModule;
        }

        public Map<String, String> getOptions() {
            return options;
        }

        public void setOptions(Map<String, String> options) {
            this.options = options;
        }
    }

}
