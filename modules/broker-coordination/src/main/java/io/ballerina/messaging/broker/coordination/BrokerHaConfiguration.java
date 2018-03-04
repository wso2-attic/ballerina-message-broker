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

package io.ballerina.messaging.broker.coordination;

import io.ballerina.messaging.broker.coordination.rdbms.RdbmsHaStrategy;

import java.util.Collections;
import java.util.Map;

/**
 * Configuration for HA.
 */
public class BrokerHaConfiguration {

    /**
     * Namespace used in the config file.
     */
    public static final String NAMESPACE = "ballerina.broker.failover";

    private boolean enabled = false;

    /**
     * Default to RDBMS coordination based HA strategy if a strategy is not specified.
     */
    private String strategy = RdbmsHaStrategy.class.getCanonicalName();

    private Map<String, String> options = Collections.emptyMap();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "Failover [enabled=" + enabled
                + ", strategy=" + strategy + "]";
    }

}
