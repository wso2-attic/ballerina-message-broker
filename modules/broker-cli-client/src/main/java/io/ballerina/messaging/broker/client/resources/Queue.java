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

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of queue in the broker.
 */
public class Queue {

    public static final String NAME = "name";
    public static final String AUTO_DELETE = "autoDelete";
    public static final String DURABLE = "durable";
    public static final String CONSUMER_COUNT = "consumerCount";
    public static final String CAPACITY = "capacity";
    public static final String SIZE = "size";

    // required params

    private String name;

    private boolean autoDelete;

    private boolean durable;

    // additional params (sets by the broker)

    private int capacity;

    private int consumerCount;

    private int size;

    private List<Permission> permissions = new ArrayList<>();

    public Queue(String name, boolean autoDelete, boolean durable) {
        this.name = name;
        this.autoDelete = autoDelete;
        this.durable = durable;
    }

    public String getName() {
        return name;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public boolean isDurable() {
        return durable;
    }

    public int getConsumerCount() {
        return consumerCount;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSize() {
        return size;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public String getAsJsonString() {
        return "{" + NAME + ":" + name + "," + AUTO_DELETE + ":" + autoDelete + "," + DURABLE + ":" + durable + "}";
    }
}
