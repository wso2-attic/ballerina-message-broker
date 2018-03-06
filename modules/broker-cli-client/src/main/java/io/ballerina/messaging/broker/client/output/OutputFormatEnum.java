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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.messaging.broker.client.output;

import io.ballerina.messaging.broker.client.utils.BrokerClientException;

import java.util.Arrays;
import java.util.Locale;

/**
 * Enum to hold output formatter types.
 */
public enum OutputFormatEnum {
    CSV, TABLE;

    /**
     * Convert String to enum. Throw BrokerClientException for invalid inputs.
     *
     * @param outputTypeText output formatter type in text.
     * @return matching enum for the output type.
     */
    public static OutputFormatEnum fromString(String outputTypeText) {

        for (OutputFormatEnum output : OutputFormatEnum.values()) {
            if (output.toString().equalsIgnoreCase(outputTypeText)) {
                return output;
            }
        }

        BrokerClientException exception = new BrokerClientException();
        exception.addMessage("invalid output formatter type provided: " + outputTypeText);
        exception.addMessage(
                "valid output formatter types: " + Arrays.toString(OutputFormatEnum.values()).toLowerCase(Locale.US));
        throw exception;
    }
}
