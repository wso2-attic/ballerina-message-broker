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

package io.ballerina.messaging.broker.amqp.codec.frames;

/**
 * Frame used when a faulty AMQP frame is received.
 * TODO maybe we can replace this with netty pipeline exception handling
 */
public class AmqpBadMessage {
    private final Throwable cause;
    private final String reason;

    public AmqpBadMessage(Throwable cause) {
        if (cause == null) {
            throw new NullPointerException("cause");
        }
        this.cause = cause;
        reason = cause.getMessage();
    }

    public AmqpBadMessage(String reason, Throwable cause) {
        if (cause == null) {
            throw new NullPointerException("cause");
        }
        this.cause = cause;
        this.reason = reason;
    }

    /**
     * Getter  method for cause.
     * @return cause
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Getter method for reason.
     * @return reason
     */
    public String getReason() {
        return reason;
    }
}
