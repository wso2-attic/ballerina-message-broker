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
 * Enum used to represent resource actions.
 */
public enum ResourceAction {

    DELETE("delete"),
    GET("get"),
    CONSUME("consume"),
    PUBLISH("publish"),
    GRANT_PERMISSION("grantPermission");

    private String name;

    ResourceAction(String name) {
        this.name = name;
    }

    public static String getResourceAction(String action) throws Exception {
        switch (action) {
            case "delete":
                return DELETE.toString();
            case "get":
                return GET.toString();
            case "consume":
                return CONSUME.toString();
            case "publish":
                return PUBLISH.toString();
            case "grantPermission":
                return GRANT_PERMISSION.toString();
            default:
                throw new Exception("Unknown resource action : " + action);
        }
    }

    @Override
    public String toString() {
        return name;
    }

}
