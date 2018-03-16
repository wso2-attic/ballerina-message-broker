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
package io.ballerina.messaging.broker.client.resources;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * Representation of the CLI Client configuration. This will be used to save and retrieve configuration information.
 */
public class Configuration {
    private String hostname;

    private int port = -1;

    private String username;

    private String password;

    public Configuration() {

    }

    public Configuration(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public static boolean validateConfiguration(Configuration configuration) {
        return Objects.nonNull(configuration.getHostname()) && configuration.getPort() != -1 && Objects
                .nonNull(configuration.getUsername());
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Combine username and password together and encode with base64.
     *
     * @return encoded text.
     */
    public String getEncodedCredentials() {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Build the url string using hostname and port.
     *
     * @return url string (protocol + host + port).
     */
    public String getUrl() {
        return "https://" + hostname + ":" + String.valueOf(port);
    }
}
