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

package io.ballerina.messaging.broker.core.transaction;

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.authorization.AuthorizationHandler;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAction;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAuthScope;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceType;
import io.ballerina.messaging.broker.core.BrokerAuthException;
import io.ballerina.messaging.broker.core.BrokerAuthNotFoundException;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Message;

import javax.security.auth.Subject;

/**
 * Forward specific transaction operations
 */
public class SecureBrokerTransaction extends ForwardingBrokerTransaction {

    /**
     * Username entity
     */
    private final Subject subject;
    /**
     * The @{@link AuthorizationHandler} to handle authorization.
     */
    private final AuthorizationHandler authHandler;

    public SecureBrokerTransaction(BrokerTransaction brokerTransaction,
                                   Subject subject, AuthorizationHandler authHandler) {
        super(brokerTransaction);
        this.subject = subject;
        this.authHandler = authHandler;
    }

    @Override
    public void enqueue(Message message) throws BrokerException {
        try {
            authHandler.handle(ResourceAuthScope.EXCHANGES_PUBLISH, ResourceType.EXCHANGE,
                    message.getMetadata().getExchangeName(), ResourceAction.PUBLISH, subject);
            super.enqueue(message);
        } catch (AuthException e) {
            throw new BrokerAuthException(e.getMessage(), e);
        } catch (AuthNotFoundException e) {
            throw new BrokerAuthNotFoundException(e.getMessage(), e);
        }
    }
}
