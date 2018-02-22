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
 * This confirms to the client that the transaction branch is started or specify the error condition. The value of
 * this field may be one of the following constants:
 *
 *      xa-ok: Normal execution.
 *      xa-rbrollback: The broker marked the transaction branch rollback-only for an unspecified reason.
 *      xa-rbtimeout: The work represented by this transaction branch took too long.
 */
public enum XaResult {

    XA_OK(0),
    XA_RBROLLBACK(1),
    XA_RBTIMEOUT(2);

    private final int value;

    XaResult(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static XaResult get(int value) {
        switch (value) {
            case 0:
                return XA_OK;
            case 1:
                return XA_RBROLLBACK;
            case 2:
                return XA_RBTIMEOUT;
            default:
                throw new IllegalArgumentException(value + " not defined.");
        }
    }
}
