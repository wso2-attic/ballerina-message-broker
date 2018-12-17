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


