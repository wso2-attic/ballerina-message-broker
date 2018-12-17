package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;


public class OrOperationExpressionTest {
    private Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    private ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89");
    private ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("89");
    private ConstantExpression c3 = ConstantExpression.createFromNumericFloat("99");
    private BooleanExpression equal = new EqualityExpression(c1, c2);
    private BooleanExpression equal1 = new EqualityExpression(c1, c2);
    private BooleanExpression equal2 = new EqualityExpression(c1, c3);
    private BooleanExpression equal3 = new EqualityExpression(c2, c3);
    @Test
    private void testOrExpressionpositive () throws Exception {
        OrOperationExpression expr = new OrOperationExpression(equal, equal1);
        OrOperationExpression expr1 = new OrOperationExpression(equal1, equal2);
        boolean actualvalue = expr.evaluate(metadata);
        boolean actualvalue1 = expr1.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
        Assert.assertEquals(actualvalue1, expectedvalue, "values are not equal");
    }
    @Test
    private void testOrExpressionnegative () throws Exception {
        OrOperationExpression expr = new OrOperationExpression(equal2, equal3);
        boolean actualvalue = expr.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        OrOperationExpression a = new OrOperationExpression(null, equal);
        OrOperationExpression a1 = new OrOperationExpression(null, null);
        OrOperationExpression a2 = new OrOperationExpression(equal1, null);
        a.evaluate(metadata);
        a1.evaluate(metadata);
        a2.evaluate(metadata);
    }
}
