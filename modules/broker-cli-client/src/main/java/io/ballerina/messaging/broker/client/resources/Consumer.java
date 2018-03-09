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

/**
 * Representation of consumer in a broker queue.
 */
public class Consumer {

    public static final String CONSUMER_ID = "id";
    public static final String IS_EXCLUSIVE = "isExclusive";
    public static final String FLOW_ENABLED = "flowEnabled";

    private String queueName;

    private int id;

    private boolean isExclusive;

    private boolean flowEnabled;

    public Consumer(String queueName, int id, boolean isExclusive, boolean flowEnabled) {
        this.queueName = queueName;
        this.id = id;
        this.isExclusive = isExclusive;
        this.flowEnabled = flowEnabled;
    }

    public String getQueueName() {
        return queueName;
    }

    public int getId() {
        return id;
    }

    public boolean isExclusive() {
        return isExclusive;
    }

    public boolean isFlowEnabled() {
        return flowEnabled;
    }
}
