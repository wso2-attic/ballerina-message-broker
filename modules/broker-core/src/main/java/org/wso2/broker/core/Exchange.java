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
interface Exchange {

    enum Type {
        DIRECT("direct"),
        TOPIC("topic");

        String typeName;

        Type(String name) {
            typeName = name;
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

    String getName();

    Type getType();

    void bind(Queue queue, String routingKey);

    void unbind(Queue queue, String routingKey);

    Set<Queue> getQueuesForRoute(String routingKey);

    /**
     * Whether there are any bindings for the exchange.
     * TODO This is only used in tests. Should we have such methods?
     */
    boolean isUnused();

}
