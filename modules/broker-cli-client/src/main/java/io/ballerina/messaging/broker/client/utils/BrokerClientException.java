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
package io.ballerina.messaging.broker.client.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * RuntimeException to throw when exception/error is caught inside the CLI client.
 */
public class BrokerClientException extends RuntimeException {
    private List<String> detailedMessages = new ArrayList<>();

    /**
     * Add a message to the exception instance.
     *
     * @param message error message.
     */
    public void addMessage(String message) {
        detailedMessages.add(message);
    }

    /**
     * Get all error messages of this exception.
     *
     * @return list of error messages.
     */
    public List<String> getMessages() {
        return detailedMessages;
    }
}
