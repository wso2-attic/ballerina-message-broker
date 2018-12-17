package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;


public class BetweenComparisionExpressionTest {
    private Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    private ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89"); // long value
    private ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("8"); // long value
    private ConstantExpression c3 = ConstantExpression.createFromNumericFloat("7.9"); // double value

    @Test
    private void testBetweenExpressionpositive () throws Exception {
        BetweenComparisionExpression between = new BetweenComparisionExpression(c2, c3, c1);
        boolean actual = between.evaluate(metadata);
        boolean expected = true;
        Assert.assertEquals(actual, expected, "values are not equal");
    }
    @Test
    private void testBetweenExpressionnegative () throws Exception {
     BetweenComparisionExpression between = new BetweenComparisionExpression(c1, c2, c3);
     boolean actual = between.evaluate(metadata);
     boolean expected = false;
     Assert.assertEquals(actual, expected, "values are not equal");
    }

    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        BetweenComparisionExpression between = new BetweenComparisionExpression(null, null, null);
        between.evaluate(metadata);
        BetweenComparisionExpression between1 = new BetweenComparisionExpression(null, null, c1);
        between1.evaluate(metadata);
        BetweenComparisionExpression between2 = new BetweenComparisionExpression(c2, null, null);
        between2.evaluate(metadata);
        BetweenComparisionExpression between3 = new BetweenComparisionExpression(c2, null, c1);
        between3.evaluate(metadata);
    }

}

