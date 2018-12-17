package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;


public class EqualityExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    ConstantExpression expr1 = ConstantExpression.createFromNumericDecimal("120");
    ConstantExpression expr2 = ConstantExpression.createFromNumericHex("5678");
    ConstantExpression expr3 = ConstantExpression.createFromNumericFloat("150");
    Expression expr4 = new ConstantExpression("value");
    Expression expr5 = new ConstantExpression("value");
    @Test
    private void testEqualitypositive() {

        EqualityExpression equal = new EqualityExpression(expr1, expr2); // two numerical values
        EqualityExpression equal1 = new EqualityExpression(expr4, expr5); // two string values
        boolean actualvalue = equal.evaluate(metadata);
        boolean actualvalue1 = equal1.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
        Assert.assertEquals(actualvalue1, expectedvalue, "values are not equal");

    }
    @Test
    private void testEqualitynegative() {
        EqualityExpression equal = new EqualityExpression(expr1, expr3); // two numerical values
        EqualityExpression equal1 = new EqualityExpression(expr1, expr4); // numerical and string value
        boolean actualvalue = equal.evaluate(metadata);
        boolean actualvalue1 = equal1.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
        Assert.assertEquals(actualvalue1, expectedvalue, "values are not equal");
    }


    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () throws Exception {
        EqualityExpression equal = new EqualityExpression(null, expr1);
        equal.evaluate(metadata);
        EqualityExpression equal1 = new EqualityExpression(expr2, null);
        equal1.evaluate(metadata);
        EqualityExpression equal2 = new EqualityExpression(null, null);
        equal2.evaluate(metadata);
    }


}
