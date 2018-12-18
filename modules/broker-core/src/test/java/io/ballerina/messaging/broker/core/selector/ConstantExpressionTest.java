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

import org.testng.Assert;
import org.testng.annotations.Test;


public class ConstantExpressionTest {
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullvalues () {
        ConstantExpression expr = ConstantExpression.createFromNumericDecimal(null);
        ConstantExpression expr1 = ConstantExpression.createFromNumericFloat(null);
        ConstantExpression expr2 = ConstantExpression.createFromNumericHex(null);
        ConstantExpression expr3 = ConstantExpression.createFromNumericOctal(null);
    }
    @Test
    private void testdecimalvalues () throws Exception {
        ConstantExpression expr = ConstantExpression.createFromNumericDecimal("100");
       Object actual = expr.evaluate("value");
       long expected = 100;
        Assert.assertEquals(actual, expected, "Values are not equal");
        double expected1 = 100;
        int expected2 = 100;
        Assert.assertNotEquals(actual, expected1, "values are equal");
        Assert.assertNotEquals(actual, expected2, "values are equal");
    }
    @Test
    private void testNumericHexvalues () throws Exception {
        ConstantExpression expr = ConstantExpression.createFromNumericHex("5678");
        Object actual = expr.evaluate("value");
        long expected = 120;
        Assert.assertEquals(actual, expected, "values are not equal");
        long expected1 = 5678;
        double expected2 = 120;
        int expected3 = 120;
        Assert.assertNotEquals(actual, expected1, "values are equal");
        Assert.assertNotEquals(actual, expected2, "values are equal");
        Assert.assertNotEquals(actual, expected3, "values are equal");
    }
    @Test
    private void testNumericOctalvalues () throws Exception {
        ConstantExpression expr = ConstantExpression.createFromNumericOctal("1234");
        Object actual = expr.evaluate("value");
        long expected = 668;
        Assert.assertEquals(actual, expected, "values are not equal");
        long expected1 = 1234;
        double expected2 = 1234;
        int expected3 = 1234;
        Assert.assertNotEquals(actual, expected1, "values are equal");
        Assert.assertNotEquals(actual, expected2, "values are equal");
        Assert.assertNotEquals(actual, expected3, "values are equal");
    }
    @Test
    private void testNumericFloatvalues () throws Exception {
        ConstantExpression expr = ConstantExpression.createFromNumericFloat("134.096");
        Object actual = expr.evaluate("value");
        double expected = 134.096;
        Assert.assertEquals(actual, expected, "values are not equal");
        float expected1 = (float) 134.096;
        Assert.assertNotEquals(actual, expected1, "values are equal");

    }
}


