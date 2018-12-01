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

    public ConstantExpression (Object value) {
        this.value = value;
    }

    @Override
    public Object evaluate (Object object) {
        return value;
    }

    public static ConstantExpression createFromNumericDecimal (String text) {
        if (text.endsWith("l") || text.endsWith("L")) {
            text = text.substring(0, text.length() - 1);
        }
        Number value;
        try {
            value = Long.valueOf(text);
        } catch (NumberFormatException var4) {
            value = new BigDecimal(text);
        }

        long l = value.longValue();
        if (Integer.MIN_VALUE <= l && l <= Integer.MAX_VALUE) {
            value = value.intValue();
        }
        return new ConstantExpression(value);
    }

    public static ConstantExpression createFromNumericHex (String text) {
        Number value = Long.parseLong(text.substring(2), 16);
        long l = value.longValue();
        if (Integer.MIN_VALUE <= l && l <= Integer.MAX_VALUE) {
            value = value.intValue();
        }
        return new ConstantExpression(value);
    }

    public static ConstantExpression createFromNumericOctal (String text) {
        Number value = Long.parseLong(text, 8);
        long l = value.longValue();
        if (Integer.MIN_VALUE <= l && l <= Integer.MAX_VALUE) {
            value = value.intValue();
        }
        return new ConstantExpression(value);
    }

    public static ConstantExpression createFromNumericFloat (String text) {
        Number value = Double.valueOf(text);
        return new ConstantExpression(value);
    }
}
