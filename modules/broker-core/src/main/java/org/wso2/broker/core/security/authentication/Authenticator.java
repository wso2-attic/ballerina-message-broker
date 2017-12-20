/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.broker.core.security.authentication;

import org.wso2.broker.core.security.authentication.exception.BrokerAuthenticationException;

import javax.security.auth.callback.CallbackHandler;

/**
 * Represents common interface to define security strategy.
 */
public interface Authenticator {

    /**
     * Method to authenticate given user
     *
     * @param callbackHandler Callback handler contain the security data. Services need to make requests for
     *                        different types of information by passing individual Callbacks
     * @return security success or not
     */
    boolean authenticate(CallbackHandler callbackHandler) throws BrokerAuthenticationException;

}
