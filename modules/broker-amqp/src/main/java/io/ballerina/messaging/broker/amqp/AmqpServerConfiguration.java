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

package io.ballerina.messaging.broker.amqp;

/**
 * Java bean class for AMQP server related configurations.
 */
public class AmqpServerConfiguration {
    /**
     * Namespace used in config file.
     */
    public static final String NAMESPACE = "ballerina.broker.transport.amqp";

    private String hostName = "localhost";

    private String maxRedeliveryCount = "5";

    private FlowDetails channelFlow = new FlowDetails();

    private NonSecureServerDetails plain = new NonSecureServerDetails();

    private SslServerDetails ssl = new SslServerDetails();

    /**
     * Getter for maxRedeliveryCount.
     */
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Getter for maxRedeliveryCount.
     */
    public String getMaxRedeliveryCount() {
        return maxRedeliveryCount;
    }

    public void setMaxRedeliveryCount(String maxRedeliveryCount) {
        this.maxRedeliveryCount = maxRedeliveryCount;
    }

    /**
     * Getter for channelFlow.
     */
    public FlowDetails getChannelFlow() {
        return channelFlow;
    }

    public void setChannelFlow(FlowDetails channelFlow) {
        this.channelFlow = channelFlow;
    }

    /**
     * Getter for tcp.
     */
    public NonSecureServerDetails getPlain() {
        return plain;
    }

    public void setPlain(NonSecureServerDetails tcp) {
        this.plain = tcp;
    }

    /**
     * Getter for ssl.
     */
    public SslServerDetails getSsl() {
        return ssl;
    }

    public void setSsl(SslServerDetails ssl) {
        this.ssl = ssl;
    }

    /**
     * Contains information related to AMQP channelFlow controlling limits.
     */
    public static class FlowDetails {
        private int lowLimit = 100;

        private int highLimit = 1000;

        /**
         * Getter for lowLimit.
         */
        public int getLowLimit() {
            return lowLimit;
        }

        public void setLowLimit(int lowLimit) {
            this.lowLimit = lowLimit;
        }

        /**
         * Getter for highLimit.
         */
        public int getHighLimit() {
            return highLimit;
        }

        public void setHighLimit(int highLimit) {
            this.highLimit = highLimit;
        }
    }

    /**
     * Contains information required to setup the non secure server socket.
     */
    public static class NonSecureServerDetails {
        private String port = "5672";

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

    }

    /**
     * Contains information required to setup the secured server socket.
     */
    public static class SslServerDetails {
        private boolean enabled = false;

        private String protocol = "TLS";

        private String port = "8672";

        private KeyStoreDetails keyStore = new KeyStoreDetails();

        private TrustStoreDetails trustStore = new TrustStoreDetails();

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        /**
         * Getter for keyStore.
         */
        public KeyStoreDetails getKeyStore() {
            return keyStore;
        }

        public void setKeyStore(KeyStoreDetails keyStore) {
            this.keyStore = keyStore;
        }

        /**
         * Getter for protocol.
         */
        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        /**
         * Getter for trustStore.
         */
        public TrustStoreDetails getTrustStore() {
            return trustStore;
        }

        public void setTrustStore(TrustStoreDetails trustStore) {
            this.trustStore = trustStore;
        }

        /**
         * Getter for enabled.
         */
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Contains keystore relate configurations.
     */
    public static class KeyStoreDetails {
        private String type = "JKS";

        private String location = "resources/security/keystore.jks";

        private String password = "ballerina";

        private String certType = "SunX509";

        /**
         * Getter for type.
         */
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        /**
         * Getter for location.
         */
        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        /**
         * Getter for password.
         */
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        /**
         * Getter for certType.
         */
        public String getCertType() {
            return certType;
        }

        public void setCertType(String certType) {
            this.certType = certType;
        }
    }

    /**
     * Contains keystore relate configurations.
     */
    public static class TrustStoreDetails {
        private String type = "JKS";

        private String location = "resources/security/client-truststore.jks";

        private String password = "ballerina";

        private String certType = "SunX509";

        /**
         * Getter for type.
         */
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        /**
         * Getter for location.
         */
        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        /**
         * Getter for password.
         */
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        /**
         * Getter for certType.
         */
        public String getCertType() {
            return certType;
        }

        public void setCertType(String certType) {
            this.certType = certType;
        }
    }
}
