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

package io.ballerina.messaging.broker.amqp.codec;

/**
 * {@link ChannelException} is associated with failures that affect the current channel
 * but not other channels in the same connection.
 */
public class ChannelException extends Exception {

    public static final int NOT_ALLOWED = 530;

    public static final int NOT_FOUND = 404;

    public static final int PRECONDITION_FAILED = 406;

    public static final int ACCESS_REFUSED = 403;

    private final int replyCode;

    public ChannelException(int replyCode, String replyText) {
        super(replyText);
        this.replyCode = replyCode;
    }

    public int getReplyCode() {
        return replyCode;
    }
}
