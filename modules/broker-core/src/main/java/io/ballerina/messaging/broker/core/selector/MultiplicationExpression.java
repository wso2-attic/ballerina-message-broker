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



/**
 * Implementation of a expression. Here we calculate multiplication of two expressions and evaluate to a object value.
 */
public class MultiplicationExpression implements Expression<Metadata> {

    private final Expression<Metadata> left;
    private final Expression<Metadata> right;
    private static final int LONG = 1;
    private static final int DOUBLE = 2;

    public MultiplicationExpression (Expression left , Expression right) {
        this.left = left;
        this.right = right;
    }
    @Override
    public Object evaluate(Metadata metadata) {
        Object leftValue = left.evaluate(metadata);
        Object rightValue = right.evaluate(metadata);

        if (leftValue instanceof Number && rightValue instanceof Number) {
            switch (numberType((Number) leftValue, (Number) rightValue)) {

                case MultiplicationExpression.DOUBLE:
                    return ((Number) leftValue).doubleValue() * ((Number) rightValue).doubleValue();
                case MultiplicationExpression.LONG:
                    return ((Number) leftValue).longValue() * ((Number) rightValue).longValue();
            }
        }
        return null;
    }

    private int numberType (Number left, Number right) {
        if (left instanceof Double || right instanceof Double) {
            return MultiplicationExpression.DOUBLE;
        } else if ((left instanceof Long) || (right instanceof Long)) {
            return MultiplicationExpression.LONG;
        }
        return 0;
    }
}
