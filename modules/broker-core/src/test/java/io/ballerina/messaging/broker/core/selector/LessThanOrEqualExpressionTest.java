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
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LessThanOrEqualExpressionTest {

    Metadata metadata = new Metadata("queue1", "amq.topic", 0);

    @Test(dataProvider = "positive-Expressions-values")
    public void testlessThanoreual_positive(Expression value, Expression value1) throws Exception {
        LessThanOrEqualExpression lseql = new LessThanOrEqualExpression(value, value1);
        boolean actualvalue = lseql.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }

    @Test(dataProvider = "negative-Expressions-values")
    public void testlessThanorequal_negative(Expression value, Expression value1) throws Exception {
        LessThanOrEqualExpression lseql = new LessThanOrEqualExpression(value, value1);
        boolean actualvalue1 = lseql.evaluate(metadata);
        boolean expectedvalue1 = false;
        Assert.assertEquals(actualvalue1, expectedvalue1, "values are not equal");
    }

    @Test(expectedExceptions = NullPointerException.class, dataProvider = "positive-Expressions-values")
    public void testnullobject(Expression value, Expression value1) throws Exception {
        LessThanOrEqualExpression lseql = new LessThanOrEqualExpression(null, value);
        lseql.evaluate(metadata);
        LessThanOrEqualExpression lseql1 = new LessThanOrEqualExpression(value1, null);
        lseql1.evaluate(metadata);
        LessThanOrEqualExpression lseql2 = new LessThanOrEqualExpression(null, null);
        lseql2.evaluate(metadata);
    }

    @DataProvider(name = "positive-Expressions-values")
    public Object[][] postive() {

        ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("200"); // decimalvalue
        ConstantExpression c2 = ConstantExpression.createFromNumericHex("5678"); // hexdecimal value
        ConstantExpression c3 = ConstantExpression.createFromNumericFloat("120"); // floating values
        Expression c4 = new ConstantExpression("value");
        return new Expression[][]{
                new Expression[]
                        {c2, c1},
                new Expression[]
                        {c2, c3},
        };
    }

    @DataProvider(name = "negative-Expressions-values")
    public Object[] negative() {

        ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("200"); // decimalvalue
        ConstantExpression c2 = ConstantExpression.createFromNumericHex("5678"); // hexdecimal value
        ConstantExpression c3 = ConstantExpression.createFromNumericFloat("120"); // floating values
        Expression c4 = new ConstantExpression("value");
        return new Expression[][]{
                new Expression[]
                        {c1, c2},
                new Expression[]
                        {c4, c3},
        };
    }
}
