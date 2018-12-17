package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;



public class SubtractionExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    @Test
    private void testSubtraction() {

        ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89");
        ConstantExpression c2 = ConstantExpression.createFromNumericFloat("7.9");
        ConstantExpression c3 = ConstantExpression.createFromNumericHex("676");
        ConstantExpression c4 = ConstantExpression.createFromNumericFloat("67.5");
        SubtractionExpression a = new SubtractionExpression(c1 , c2); //long value and double value
        SubtractionExpression a1 = new SubtractionExpression(c1, c3); //two long values
        SubtractionExpression a2 = new SubtractionExpression(c2, c4); //two double values
        Object actualvalue = a.evaluate(metadata);
        double expectedvalue = 81.1;
        Assert.assertEquals(expectedvalue, actualvalue, "Objects not matching");
        Object actualvalue1 = a1.evaluate(metadata);
        long expectedvalue1 = 83;
        Assert.assertEquals(expectedvalue1, actualvalue1, "Objects not matching");
        Object actualvalue2 = a2.evaluate(metadata);
        double expectedvalue2 = -59.6;
        Assert.assertEquals(expectedvalue2, actualvalue2, "Objects not matching");
    }

    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("23");
        SubtractionExpression a = new SubtractionExpression(null , c2);
        SubtractionExpression a1 = new SubtractionExpression(null, null);
        SubtractionExpression a2 = new SubtractionExpression(c2, null);
        a.evaluate(metadata);
        a1.evaluate(metadata);
        a2.evaluate(metadata);
    }
    @Test(expectedExceptionsMessageRegExp = "value is not a number")
    private void testobjecttype() {
        Expression  expr = new JmsPropertyExpression("Myproperty");
        ConstantExpression c1 = new ConstantExpression("property");
        ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("789");
        SubtractionExpression a = new SubtractionExpression(c1, c2);
        SubtractionExpression a1 = new SubtractionExpression(expr, c1);
        SubtractionExpression a2 = new SubtractionExpression(c2, expr);
        Object value = a.evaluate(metadata);
        Object value1 = a1.evaluate(metadata);
        Object value2 = a2.evaluate(metadata);

    }

}
