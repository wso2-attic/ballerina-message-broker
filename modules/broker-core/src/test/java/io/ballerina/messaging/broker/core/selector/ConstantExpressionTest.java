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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ConstantExpressionTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void testnullvalues() {

        ConstantExpression expr = ConstantExpression.createFromNumericDecimal(null);
        ConstantExpression expr1 = ConstantExpression.createFromNumericFloat(null);
        ConstantExpression expr2 = ConstantExpression.createFromNumericHex(null);
        ConstantExpression expr3 = ConstantExpression.createFromNumericOctal(null);
    }

    @Test(dataProvider = "decimal-positive-filter-strings")
    public void testdecimalvalues(String value, String expect) throws Exception {

        ConstantExpression expr = ConstantExpression.createFromNumericDecimal(value);
        Object actual = expr.evaluate("value");
        long expected = Long.parseLong(expect);
        Assert.assertEquals(actual, expected, "Values are not equal");
        double expected1 = Double.parseDouble(expect);
        int expected2 = Integer.parseInt(expect);
        Assert.assertNotEquals(actual, expected1, "values are equal");
        Assert.assertNotEquals(actual, expected2, "values are equal");
    }

    @Test(dataProvider = "hex-positive-filter-strings")
    public void testNumericHexvalues(String value, String expect) throws Exception {

        ConstantExpression expr = ConstantExpression.createFromNumericHex(value);
        Object actual = expr.evaluate("value");
        long expected = Long.parseLong(expect);
        Assert.assertEquals(actual, expected, "values are not equal");
        long expected1 = Long.parseLong(value);
        double expected2 = Double.parseDouble(value);
        int expected3 = Integer.parseInt(value);
        Assert.assertNotEquals(actual, expected1, "values are equal");
        Assert.assertNotEquals(actual, expected2, "values are equal");
        Assert.assertNotEquals(actual, expected3, "values are equal");
    }

    @Test(dataProvider = "octal-positive-filter-strings")
    public void testNumericOctalvalues(String value, String expect) throws Exception {

        ConstantExpression expr = ConstantExpression.createFromNumericOctal(value);
        Object actual = expr.evaluate("value");
        long expected = Long.parseLong(expect);
        Assert.assertEquals(actual, expected, "values are not equal");
        long expected1 = Long.parseLong(value);
        double expected2 = Double.parseDouble(value);
        int expected3 = Integer.parseInt(value);
        Assert.assertNotEquals(actual, expected1, "values are equal");
        Assert.assertNotEquals(actual, expected2, "values are equal");
        Assert.assertNotEquals(actual, expected3, "values are equal");
    }

    @Test(dataProvider = "float-positive-filter-strings")
    public void testNumericFloatvalues(String value, String expect) throws Exception {

        ConstantExpression expr = ConstantExpression.createFromNumericFloat(value);
        Object actual = expr.evaluate("value");
        double expected = Double.parseDouble(expect);
        Assert.assertEquals(actual, expected, "values are not equal");
        float expected1 = Float.parseFloat(expect);
        Assert.assertNotEquals(actual, expected1, "values are equal");

    }

    @DataProvider(name = "decimal-positive-filter-strings")
    public Object[][] decimalpositiveFilterStrings() {

        return new String[][]{
                new String[]{"100", "100"},
                new String[]{"10000", "10000"},
                new String[]{"10l", "10"},
                new String[]{"1000l", "1000"},
        };
    }

    @DataProvider(name = "hex-positive-filter-strings")
    public Object[][] hexpositiveFilterStrings() {

        return new String[][]{
                new String[]{"5678", "120"},
        };
    }

    @DataProvider(name = "octal-positive-filter-strings")
    public Object[][] octalpositiveFilterStrings() {

        return new String[][]{
                new String[]{"1234", "668"},
        };
    }

    @DataProvider(name = "float-positive-filter-strings")
    public Object[][] floatpositiveFilterStrings() {

        return new String[][]{
                new String[]{"134.096", "134.096"},
        };
    }
}


