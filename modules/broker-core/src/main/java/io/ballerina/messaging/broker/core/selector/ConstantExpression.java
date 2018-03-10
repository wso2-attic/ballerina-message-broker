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

package io.ballerina.messaging.broker.core.selector;

import java.math.BigDecimal;

/**
 * Represents a constant value in an expression.
 */
public class ConstantExpression implements Expression<Object> {

    private final Object value;

    public ConstantExpression(Object value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Object object) {
        return value;
    }

    public static ConstantExpression createFromNumericInteger(String text) {

        if (text.endsWith("l") || text.endsWith("L")) {
            text = text.substring(0, text.length() - 1);
        }

        Number value;
        try {
            value = Long.valueOf(text);
        } catch (NumberFormatException e) {
            value = new BigDecimal(text);
        }

        if (value.intValue() < Integer.MAX_VALUE && value.intValue() > Integer.MIN_VALUE) {
            value = value.intValue();
        }

        return new ConstantExpression(value);
    }
}
