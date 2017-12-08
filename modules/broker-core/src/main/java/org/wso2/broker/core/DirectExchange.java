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
 * AMQP direct exchange implementation.
 */
final class DirectExchange implements Exchange {

    private final String name;

    private final BindingsRegistry bindingsRegistry;

    DirectExchange(String name) {
        this.name = name;
        this.bindingsRegistry = new BindingsRegistry();
    }

    public String getName() {
        return name;
    }

    public Exchange.Type getType() {
        return Type.DIRECT;
    }

    public void bind(QueueHandler queueHandler, String routingKey) {
        bindingsRegistry.bind(queueHandler, routingKey);
    }

    public void unbind(String queueName, String routingKey) {
        bindingsRegistry.unbind(queueName, routingKey);
    }

    public Set<Binding> getBindingsForRoute(String routingKey) {
        return bindingsRegistry.getBindingsForRoute(routingKey);
    }

    /**
     * Whether there are any bindings for the exchange.
     */
    public boolean isUnused() {
        return bindingsRegistry.isEmpty();
    }
}
