package io.ballerina.messaging.broker.core.selector;


import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GreaterThanExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("200"); // decimalvalue
    ConstantExpression c2 = ConstantExpression.createFromNumericHex("5678"); // hexdecimal value
    ConstantExpression c3 = ConstantExpression.createFromNumericFloat("120"); // floating values
    Expression c4 = new ConstantExpression("value");
    @Test
    private void testlessThan_positive() {
        GreaterThanExpression equal = new GreaterThanExpression(c1, c2); //two different values
        boolean actualvalue = equal.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test
    private void testlessThan_negative() {
        GreaterThanExpression equal1 = new GreaterThanExpression(c2, c3); //two equal values
        GreaterThanExpression equal2 = new GreaterThanExpression(c4, c3); // string expression value
        boolean actualvalue1 = equal1.evaluate(metadata);
        boolean actualvalue2 = equal2.evaluate(metadata);
        boolean expectedvalue1 = false;
        Assert.assertEquals(actualvalue1, expectedvalue1, "values are not equal");
        Assert.assertEquals(actualvalue2, expectedvalue1, "values are not equal");

    }
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () throws Exception {
        GreaterThanExpression equal = new GreaterThanExpression(null, c1);
        equal.evaluate(metadata);
        GreaterThanExpression equal1 = new GreaterThanExpression(c2, null);
        equal1.evaluate(metadata);
        GreaterThanExpression equal2 = new GreaterThanExpression(null, null);
        equal2.evaluate(metadata);
    }


}
