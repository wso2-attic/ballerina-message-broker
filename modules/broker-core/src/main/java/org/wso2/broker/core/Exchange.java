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

import org.wso2.broker.common.data.types.FieldTable;
import org.wso2.broker.core.store.dao.BindingDao;
import org.wso2.broker.core.store.dao.NoOpBindingDao;

/**
 * Represents an Exchange for the broker.
 */
public abstract class Exchange {

    /**
     * Supported exchange types by the broker
     */
    public enum Type {
        DIRECT("direct"),
        TOPIC("topic");

        String typeName;

        Type(String name) {
            typeName = name;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static Type from(String typeString) {

            if (typeString.equals(DIRECT.typeName)) {
                return DIRECT;
            } else if (typeString.equals(TOPIC.typeName)) {
                return TOPIC;
            } else {
                throw new IllegalArgumentException("unknown exchange type: " + typeString);
            }
        }

    }

    private final int hashCode;

    private final String name;

    private final Type type;

    private final BindingDao bindingDao;

    private final BindingsRegistry bindingsRegistry;

    protected Exchange(String name, Type type, BindingDao bindingDao) {
        this.name = name;
        this.type = type;
        hashCode = (name + type.toString()).hashCode();
        this.bindingDao = bindingDao;
        this.bindingsRegistry = new BindingsRegistry(this, bindingDao);
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public BindingDao getBindingDao() {
        return bindingDao;
    }

    void bind(Queue queue, String routingKey, FieldTable arguments) throws BrokerException {
        bindingsRegistry.bind(queue, routingKey, arguments);
    }

    void unbind(Queue queue, String routingKey) throws BrokerException {
        bindingsRegistry.unbind(queue, routingKey);
    }

    BindingSet getBindingsForRoute(String routingKey) {
        return bindingsRegistry.getBindingsForRoute(routingKey);
    }

    BindingsRegistry getBindingsRegistry() {
        return bindingsRegistry;
    }

    /**
     * Whether there are any bindings for the exchange.
     * TODO This is only used in tests. Should we have such methods?
     */
    boolean isUnused() {
        return bindingsRegistry.isEmpty();
    }

    public boolean isDurable() {
        return !(bindingDao instanceof NoOpBindingDao);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj instanceof Exchange) &&
                getName().equals(((Exchange) obj).getName()) && getType() == ((Exchange) obj).getType()) {
            return true;
        }
        return false;
    }

    void retrieveBindingsFromDb(QueueRegistry queueRegistry) throws BrokerException {
        bindingsRegistry.retrieveAllBindingsForExchange(queueRegistry);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
