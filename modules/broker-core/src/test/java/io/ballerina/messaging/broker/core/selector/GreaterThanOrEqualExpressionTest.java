package io.ballerina.messaging.broker.core.selector;


import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GreaterThanOrEqualExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("200");
    ConstantExpression c2 = ConstantExpression.createFromNumericHex("5678"); //decimal 120
    ConstantExpression c3 = ConstantExpression.createFromNumericFloat("120");
    Expression c4 = new ConstantExpression("value");
    @Test
    private void testGreaterThanoreual_positive() {
        GreaterThanOrEqualExpression value = new GreaterThanOrEqualExpression(c1, c2); //two different values
        GreaterThanOrEqualExpression value1 = new GreaterThanOrEqualExpression(c2, c3); //two equal values
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
        boolean actualvalue1 = value1.evaluate(metadata);
        boolean expectedvalue1 = true;
        Assert.assertEquals(actualvalue1, expectedvalue1, "values are not equal");
    }
    @Test
    private void testGreaterThanorequal_negative() {
        GreaterThanOrEqualExpression value1 = new GreaterThanOrEqualExpression(c4, c3); // string expression value
        GreaterThanOrEqualExpression value2 = new GreaterThanOrEqualExpression(c2, c1);
        boolean actualvalue1 = value1.evaluate(metadata);
        boolean actualvalue2 = value2.evaluate(metadata);
        boolean expectedvalue1 = false;
        Assert.assertEquals(actualvalue1, expectedvalue1, "values are not equal");
        Assert.assertEquals(actualvalue2, expectedvalue1, "values are not equal");

    }
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () throws Exception {
        GreaterThanOrEqualExpression equal = new GreaterThanOrEqualExpression(null, c1);
        equal.evaluate(metadata);
        GreaterThanOrEqualExpression equal1 = new GreaterThanOrEqualExpression(c2, null);
        equal1.evaluate(metadata);
        GreaterThanOrEqualExpression equal2 = new GreaterThanOrEqualExpression(null, null);
        equal2.evaluate(metadata);
    }
}

