package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;


public class NotOperationExpressionTest {
    private Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    private ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89");
    private ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("89");
    private ConstantExpression c3 = ConstantExpression.createFromNumericFloat("99");
    private BooleanExpression equal = new EqualityExpression(c1, c2); // true
    private BooleanExpression equal3 = new EqualityExpression(c2, c3); // false
    @Test
    private void testOrExpressionpositive () throws Exception {
        NotOperationExpression expr = new NotOperationExpression(equal3);
        boolean actualvalue = expr.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test
    private void testOrExpressionnegative () throws Exception {
        NotOperationExpression expr = new NotOperationExpression(equal);
        boolean actualvalue = expr.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        NotOperationExpression a = new NotOperationExpression(null);
        a.evaluate(metadata);

    }
}

