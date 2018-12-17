package io.ballerina.messaging.broker.core.selector;


import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LessThanOrEqualExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("200");
    ConstantExpression c2 = ConstantExpression.createFromNumericHex("5678"); //decimal 120
    ConstantExpression c3 = ConstantExpression.createFromNumericFloat("120");
    Expression c4 = new ConstantExpression("value");
    @Test
    private void testlessThanoreual_positive() {
        LessThanOrEqualExpression value = new LessThanOrEqualExpression(c2, c1); //two different values
        LessThanOrEqualExpression value1 = new LessThanOrEqualExpression(c2, c3); //two equal values
        boolean actualvalue = value.evaluate(metadata);
        boolean actualvalue1 = value1.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
        Assert.assertEquals(actualvalue1, expectedvalue, "values are not equal");
    }
    @Test
    private void testlessThanorequal_negative() {
        LessThanOrEqualExpression value1 = new LessThanOrEqualExpression(c4, c3); // string expression value
        LessThanOrEqualExpression value2 = new LessThanOrEqualExpression(c1, c2);
        boolean actualvalue1 = value1.evaluate(metadata);
        boolean actualvalue2 = value2.evaluate(metadata);
        boolean expectedvalue1 = false;
        Assert.assertEquals(actualvalue1, expectedvalue1, "values are not equal");
        Assert.assertEquals(actualvalue2, expectedvalue1, "values are not equal");

    }
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () throws Exception {
        LessThanOrEqualExpression equal = new LessThanOrEqualExpression(null, c1);
        equal.evaluate(metadata);
        LessThanOrEqualExpression equal1 = new LessThanOrEqualExpression(c2, null);
        equal1.evaluate(metadata);
        LessThanOrEqualExpression equal2 = new LessThanOrEqualExpression(null, null);
        equal2.evaluate(metadata);
    }
}
