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
package io.ballerina.messaging.broker.auth.authorization.enums;

/**
 * Enum used to represent resource auth scopes.
 */
public enum ResourceAuthScope {

    EXCHANGES_CREATE("exchanges:create"),
    EXCHANGES_DELETE("exchanges:delete"),
    EXCHANGES_GET("exchanges:get"),
    EXCHANGES_PUBLISH("exchanges:publish"),
    QUEUES_CREATE("queues:create"),
    QUEUES_DELETE("queues:delete"),
    QUEUES_GET("queues:get"),
    QUEUES_CONSUME("queues:consume"),
    RESOURCE_GRANT_PERMISSION("resources:grant"),
    SCOPES_UPDATE("scopes:update"),
    SCOPES_GET("scopes:get");

    private String name;

    ResourceAuthScope(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
