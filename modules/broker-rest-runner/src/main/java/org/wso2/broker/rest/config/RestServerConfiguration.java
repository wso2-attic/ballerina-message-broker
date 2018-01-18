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

package org.wso2.broker.rest.config;

/**
 * Java bean class for AMQP server related configurations.
 */
public class RestServerConfiguration {
    /**
     * Namespace used in config file
     */
    public static final String NAMESPACE = "org.wso2.broker";

    private String hostName = "localhost";

    private AdminServiceDetails adminService = new AdminServiceDetails();

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Getter for adminService
     */
    public AdminServiceDetails getAdminService() {
        return adminService;
    }

    public void setAdminService(AdminServiceDetails adminService) {
        this.adminService = adminService;
    }

    /**
     * Config class for admin server
     */
    public static class AdminServiceDetails {
        private NonSecureServerDetails plain = new NonSecureServerDetails();

        /**
         * Getter for tcp.
         */
        public NonSecureServerDetails getPlain() {
            return plain;
        }

        public void setPlain(NonSecureServerDetails plain) {
            this.plain = plain;
        }
    }

    /**
     * Contains information required to setup the non secure server socket.
     */
    public static class NonSecureServerDetails {
        private String port = "9000";

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

    }
}
