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

public class NotEqualExpressionTest {

    Metadata metadata = new Metadata("queue1", "amq.topic", 0);
    ConstantExpression expr1 = ConstantExpression.createFromNumericDecimal("120");
    ConstantExpression expr2 = ConstantExpression.createFromNumericHex("5678");
    ConstantExpression expr3 = ConstantExpression.createFromNumericFloat("150");
    Expression expr4 = new ConstantExpression("value");
    Expression expr5 = new ConstantExpression("value");

    @Test(dataProvider = "positive-Expressions-values")
    public void testEqualitypositive(Expression value, Expression value1) throws Exception {
        NotEqualExpression notequal = new NotEqualExpression(value, value1); // two numerical values
        boolean actualvalue = notequal.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");

    }

    @Test(dataProvider = "negative-Expressions-values")
    public void testEqualitynegative(Expression value, Expression value1) throws Exception {

        NotEqualExpression notequal = new NotEqualExpression(value, value1);
        boolean actualvalue = notequal.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test(expectedExceptions = NullPointerException.class, dataProvider = "positive-Expressions-values")
    public void testnullobject(Expression value, Expression value1) throws Exception {
        NotEqualExpression notequal = new NotEqualExpression(null, value);
        notequal.evaluate(metadata);
        NotEqualExpression notequal1 = new NotEqualExpression(value1, null);
        notequal1.evaluate(metadata);
        NotEqualExpression notequal2 = new NotEqualExpression(null, null);
        notequal2.evaluate(metadata);
    }
    @DataProvider(name = "positive-Expressions-values")
    public Object[][] postive() {

        ConstantExpression expr1 = ConstantExpression.createFromNumericDecimal("120");
        ConstantExpression expr2 = ConstantExpression.createFromNumericHex("5678");
        ConstantExpression expr3 = ConstantExpression.createFromNumericFloat("150");
        Expression expr4 = new ConstantExpression("value");
        Expression expr5 = new ConstantExpression("value");
        return new Expression[][]{
                new Expression[]
                        {expr1, expr3},
        };
    }

    @DataProvider(name = "negative-Expressions-values")
    public Object[] negative() {

        ConstantExpression expr1 = ConstantExpression.createFromNumericDecimal("120");
        ConstantExpression expr2 = ConstantExpression.createFromNumericHex("5678");
        ConstantExpression expr3 = ConstantExpression.createFromNumericFloat("150");
        Expression expr4 = new ConstantExpression("value");
        Expression expr5 = new ConstantExpression("value");
        return new Expression[][]{
                new Expression[]
                        {expr1, expr2},
                new Expression[]
                        {expr4, expr5},
                new Expression[]
                        {expr1, expr4},
        };
    }

}
