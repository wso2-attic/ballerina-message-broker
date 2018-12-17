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

import io.ballerina.messaging.broker.core.Metadata;

import java.math.BigDecimal;

/**
 * Implementation of a expression. Here we evaluate the value of a unary expressions
 */
public class UnaryNegative implements Expression<Metadata> {

    private static final BigDecimal BD_LONG_MIN_VALUE = BigDecimal.valueOf(Long.MIN_VALUE);
    private final Expression<Metadata> left;

    public UnaryNegative (Expression left) {

        this.left = left;
    }
    @Override
    public Object evaluate (Metadata metadata) {
        Object leftvalue = left.evaluate(metadata);
        if (leftvalue == null) {
            return null;
        }
        if (leftvalue instanceof Number) {
            return UnaryNegative.negate((Number) leftvalue);
        }
        return new Exception("value is not a number");
    }
    private static Number negate (Number left) {
        Class clazz = left.getClass();
        if (clazz == Integer.class) {
            return -left.longValue();
        } else if (clazz == Long.class) {
            return -left.longValue();
        } else if (clazz == Float.class) {
            return -left.doubleValue();
        } else if (clazz == Double.class) {
            return -left.doubleValue();
        } else if (clazz == BigDecimal.class) {
            // We ussually get a big deciamal when we have Long.MIN_VALUE constant in the
            // Selector.  Long.MIN_VALUE is too big to store in a Long as a positive so we store it
            // as a Big decimal.  But it gets Negated right away.. to here we try to covert it back
            // to a Long.
            BigDecimal bd = (BigDecimal) left;
            bd = bd.negate();
            if (UnaryNegative.BD_LONG_MIN_VALUE.compareTo(bd) == 0) {
                return Long.MIN_VALUE;
            }
            return bd;
        } else {
            throw new RuntimeException("Don't know how to negate: " + left);
        }
    }
}
