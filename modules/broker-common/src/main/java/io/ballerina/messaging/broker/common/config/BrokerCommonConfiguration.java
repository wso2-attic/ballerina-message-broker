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

package io.ballerina.messaging.broker.common.config;

/**
 * Configuration class which hold the configurations shared by multiple modules.
 */
public class BrokerCommonConfiguration {
    /**
     * Namespace used in config file.
     */
    public static final String NAMESPACE = "ballerina.broker";

    private boolean enableInMemoryMode = false;

    private DataSourceConfiguration dataSource;

    /**
     * Getter for enableInMemoryMode.
     */
    public boolean getEnableInMemoryMode() {
        return enableInMemoryMode;
    }

    public void setEnableInMemoryMode(boolean enableInMemoryMode) {
        this.enableInMemoryMode = enableInMemoryMode;
    }

    public DataSourceConfiguration getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSourceConfiguration dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Represents a dataSource configuration for broker ( e.g. database).
     */
    public static class DataSourceConfiguration {

        private String url = "jdbc:h2:./database/MB_DB";

        private String databaseDriver = "org.h2.Driver";

        private String user = "ballerina";

        private String password = "ballerina";

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
}
