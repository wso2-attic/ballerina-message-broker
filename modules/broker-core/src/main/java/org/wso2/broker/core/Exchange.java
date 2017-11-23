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

package org.wso2.broker.core;

import java.util.Set;

/**
 * Represents an Exchange for the broker.
 */
class Exchange {

    /**
     * Represents the Type of the exchange.
     */
    public enum Type {
        DIRECT
    }

    private final String name;

    private final Type type;

    private final BindingsRegistry bindingsRegistry;

    Exchange(String name, Type type) {
        this.name = name;
        this.type = type;
        this.bindingsRegistry = new BindingsRegistry();
    }

    String getName() {
        return name;
    }

    Type getType() {
        return type;
    }

    void bind(QueueHandler queueHandler, String routingKey) {
        bindingsRegistry.bind(queueHandler, routingKey);
    }

    void unbind(String queueName, String routingKey) {
        bindingsRegistry.unbind(queueName, routingKey);
    }

    Set<Binding> getBindingsForRoute(String routingKey) {
        return bindingsRegistry.getBindingsForRoute(routingKey);
    }

    /**
     * Whether there are any bindings for the exchange.
     */
    boolean isUnused() {
        return bindingsRegistry.isEmpty();
    }

}
