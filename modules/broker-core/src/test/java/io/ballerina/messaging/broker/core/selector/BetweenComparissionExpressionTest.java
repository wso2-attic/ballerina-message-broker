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

public class BetweenComparissionExpressionTest {

    private Metadata metadata = new Metadata("queue1", "amq.topic", 0);

    @Test(dataProvider = "Expressions-values")
    public void testBetweenExpressionpositive(Expression value1, Expression value2, Expression value3)
            throws Exception {

        BetweenComparissionExpression between = new BetweenComparissionExpression(value2, value3, value1);
        boolean actual = between.evaluate(metadata);
        boolean expected = true;
        Assert.assertEquals(actual, expected, "values are not equal");
    }

    @Test(dataProvider = "Expressions-values")
    public void testBetweenExpressionnegative(Expression value1, Expression value2, Expression value3)
            throws Exception {

        BetweenComparissionExpression between = new BetweenComparissionExpression(value1, value2, value3);
        boolean actual = between.evaluate(metadata);
        boolean expected = false;
        Assert.assertEquals(actual, expected, "values are not equal");
    }

    @Test(expectedExceptions = NullPointerException.class, dataProvider = "Expressions-values")
    public void testnullobject(Expression value1, Expression value2, Expression value3) throws Exception {

        BetweenComparissionExpression between = new BetweenComparissionExpression(null, null, null);
        between.evaluate(metadata);
        BetweenComparissionExpression between1 = new BetweenComparissionExpression(null, null, value1);
        between1.evaluate(metadata);
        BetweenComparissionExpression between2 = new BetweenComparissionExpression(value2, null, null);
        between2.evaluate(metadata);
        BetweenComparissionExpression between3 = new BetweenComparissionExpression(value2, null, value1);
        between3.evaluate(metadata);
    }

    @DataProvider(name = "Expressions-values")
    public Object[][] expressions() {

        ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89"); // long value
        ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("8"); // long value
        ConstantExpression c3 = ConstantExpression.createFromNumericFloat("7.9"); // double value

        return new Expression[][]{
                new Expression[]{c1, c2, c3},

        };
    }

}

