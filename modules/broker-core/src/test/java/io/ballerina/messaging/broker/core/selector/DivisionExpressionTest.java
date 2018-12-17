package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DivisionExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    @Test
    private void testdivision() {

        ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89");
        ConstantExpression c2 = ConstantExpression.createFromNumericFloat("7.9");
        ConstantExpression c3 = ConstantExpression.createFromNumericHex("676");
        ConstantExpression c4 = ConstantExpression.createFromNumericFloat("67.5");
        DivisionExpression a = new DivisionExpression(c1 , c2); //long value and double value
        DivisionExpression a1 = new DivisionExpression(c1, c3); //two long values
        DivisionExpression a2 = new DivisionExpression(c2, c4); //two double values
        Object actualvalue = a.evaluate(metadata);
        double expectedvalue = 11.265822784810126;
        Assert.assertEquals(expectedvalue, actualvalue, "Objects not matching");
        Object actualvalue1 = a1.evaluate(metadata);
        long expectedvalue1 = 14;
        Assert.assertEquals(expectedvalue1, actualvalue1, "Objects not matching");
        Object actualvalue2 = a2.evaluate(metadata);
        double expectedvalue2 = 0.11703703703703704;
        Assert.assertEquals(expectedvalue2, actualvalue2, "Objects not matching");
    }

    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("23");
        DivisionExpression a = new DivisionExpression(null , c2);
        DivisionExpression a1 = new DivisionExpression(null, null);
        DivisionExpression a2 = new DivisionExpression(c2, null);
        a.evaluate(metadata);
        a1.evaluate(metadata);
        a2.evaluate(metadata);
    }
    @Test(expectedExceptionsMessageRegExp = "value is not a number")
    private void testobjecttype() {
        Expression  expr = new JmsPropertyExpression("Myproperty");
        ConstantExpression c1 = new ConstantExpression("property");
        ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("789");
        DivisionExpression a = new DivisionExpression(c1, c2);
        DivisionExpression a1 = new DivisionExpression(expr, c1);
        DivisionExpression a2 = new DivisionExpression(c2, expr);
        Object value = a.evaluate(metadata);
        Object value1 = a1.evaluate(metadata);
        Object value2 = a2.evaluate(metadata);

    }

}

