package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;


public class NotBetweenComparisionExpressionTest {
    private Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    private ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89"); // long value
    private ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("8"); // long value
    private ConstantExpression c3 = ConstantExpression.createFromNumericFloat("7.9"); // double value
    @Test
    private void testNotBetweenExpressionpositive () throws Exception {
        NotBetweenComparisionExpression between = new NotBetweenComparisionExpression(c1, c2, c3);
        boolean actual = between.evaluate(metadata);
        boolean expected = true;
        Assert.assertEquals(actual, expected, "values are not equal");
    }
    @Test
    private void testNotBetweenExpressionnegative () throws Exception {
        NotBetweenComparisionExpression between = new NotBetweenComparisionExpression(c2, c3, c1);
        boolean actual = between.evaluate(metadata);
        boolean expected = false;
        Assert.assertEquals(actual, expected, "values are not equal");
    }
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        NotBetweenComparisionExpression between = new NotBetweenComparisionExpression(null, null, null);
        between.evaluate(metadata);
        NotBetweenComparisionExpression between1 = new NotBetweenComparisionExpression(null, null, c1);
        between1.evaluate(metadata);
        NotBetweenComparisionExpression between2 = new NotBetweenComparisionExpression(c2, null, null);
        between2.evaluate(metadata);
        NotBetweenComparisionExpression between3 = new NotBetweenComparisionExpression(c2, null, c1);
        between3.evaluate(metadata);
    }
}
