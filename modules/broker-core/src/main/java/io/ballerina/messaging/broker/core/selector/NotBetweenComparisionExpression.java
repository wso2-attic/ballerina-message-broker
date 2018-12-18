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
 * Implementation of a boolean expression.This class is doing a comparison operation between the left with other two
 * values provided.if left value is between other two values it evaluate to a boolean value.
 * please refer the ![jms-selector-guide](../docs/user/jms-selector-guide.md).
 */
public class NotBetweenComparisionExpression implements BooleanExpression {

    private final Expression<Metadata> left;
    private final Expression<Metadata> value1;
    private final Expression<Metadata> value2;

    public NotBetweenComparisionExpression (Expression left, Expression value1, Expression value2) {
        this.left = left;
        this.value1 = value1;
        this.value2 = value2;
    }
    @Override
    public boolean evaluate (Metadata metadata) {
        Object leftValue = left.evaluate(metadata);
        Object firstValue = value1.evaluate(metadata);
        Object secondValue = value2.evaluate(metadata);

        if (leftValue instanceof Number && firstValue instanceof Number && secondValue instanceof Number) {
            if ((leftValue instanceof Long) && (firstValue instanceof Long) && (secondValue instanceof Long)) {
                    long l = ((Number) leftValue).longValue();
                    long l1 = ((Number) firstValue).longValue();
                    long l2 = ((Number) secondValue).longValue();
                    return ((leftValue != firstValue) && (l <= l1)) || ((leftValue != secondValue) && (l >= l2));
                }
            if ((leftValue instanceof Double) || (firstValue instanceof Double) || (secondValue instanceof Double)) {
                double l = ((Number) leftValue).doubleValue();
                double l1 = ((Number) firstValue).doubleValue();
                double l2 = ((Number) secondValue).doubleValue();
                return ((leftValue != firstValue) && (l <= l1)) || ((leftValue != secondValue) && (l >= l2));
            }
        }
        return false;
    }
}
