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

import com.google.gson.JsonObject;

/**
 * Representation of binding in the broker.
 */
public class Binding {

    public static final String BINDING_PATTERN = "bindingPattern";
    public static final String EXCHANGE_NAME = "exchangeName";
    public static final String QUEUE_NAME = "queueName";
    public static final String FILTER_EXPRESSION = "filterExpression";

    private String queueName;

    private String bindingPattern;

    private String exchangeName;

    private String filterExpression;

    public Binding(String queueName, String bindingPattern, String exchangeName, String filterExpression) {
        this.queueName = queueName;
        this.bindingPattern = bindingPattern;
        this.exchangeName = exchangeName;
        this.filterExpression = filterExpression;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getBindingPattern() {
        return bindingPattern;
    }

    public void setBindingPattern(String bindingPattern) {
        this.bindingPattern = bindingPattern;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
    }

    public String getAsJsonString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(BINDING_PATTERN, bindingPattern);
        jsonObject.addProperty(EXCHANGE_NAME, exchangeName);
        jsonObject.addProperty(FILTER_EXPRESSION, filterExpression);
        return jsonObject.toString();
    }
}
