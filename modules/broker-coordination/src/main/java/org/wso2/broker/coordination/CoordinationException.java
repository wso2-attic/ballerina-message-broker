/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.broker.coordination;

/**
 * Represents an exception of the broker occurring during coordination.
 */
public class CoordinationException extends Exception {

    private String errorCode;

    public CoordinationException(String message) {
        super(message);
    }

    public CoordinationException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public CoordinationException(String message, String errorCode, Throwable throwable) {
        this(message, throwable);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
