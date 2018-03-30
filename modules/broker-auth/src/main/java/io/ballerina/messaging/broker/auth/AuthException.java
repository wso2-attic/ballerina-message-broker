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
package io.ballerina.messaging.broker.auth;

import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAction;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceType;

import javax.security.auth.login.LoginException;

/**
 * This Exception class represents login failures.
 */
public class AuthException extends LoginException {

    private static final long serialVersionUID = 1414511165553802816L;

    public AuthException(String message) {
        super(message);
    }

    public AuthException(ResourceType resourceType, String resourceName, ResourceAction action) {
        super("Unauthorized action: " + action.toString() +  " on : " + resourceType.toString()
                      + " resourceName: " + resourceName);
    }


    public AuthException(String message, Throwable throwable) {
        super(message);
        initCause(throwable);
    }
}
