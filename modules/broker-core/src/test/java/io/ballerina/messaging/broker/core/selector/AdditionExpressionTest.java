package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;



public class AdditionExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    @Test
    private void testadiition() {
        ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89");
        ConstantExpression c2 = ConstantExpression.createFromNumericFloat("7.9");
        ConstantExpression c3 = ConstantExpression.createFromNumericHex("676");
        ConstantExpression c4 = ConstantExpression.createFromNumericFloat("67.5");
        AdditionExpression a = new AdditionExpression(c1 , c2); //long value and double value
        AdditionExpression a1 = new AdditionExpression(c1, c3); //two long values
        AdditionExpression a2 = new AdditionExpression(c2, c4); //two double values
        Object actualvalue = a.evaluate(metadata);
        double expectedvalue = 96.9;
        Assert.assertEquals(expectedvalue, actualvalue, "Objects not matching");
        Object actualvalue1 = a1.evaluate(metadata);
        long expectedvalue1 = 95;
        Assert.assertEquals(expectedvalue1, actualvalue1, "Objects not matching");
        Object actualvalue2 = a2.evaluate(metadata);
        double expectedvalue2 = 75.4;
        Assert.assertEquals(expectedvalue2, actualvalue2, "Objects not matching");


    }

    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("23");
       AdditionExpression a = new AdditionExpression(null , c2);
       AdditionExpression a1 = new AdditionExpression(null, null);
       AdditionExpression a2 = new AdditionExpression(c2, null);
       a.evaluate(metadata);
       a1.evaluate(metadata);
       a2.evaluate(metadata);
    }
    @Test(expectedExceptionsMessageRegExp = "value is not a number")
    private void testobjecttype() {
        Expression  expr = new JmsPropertyExpression("Myproperty");
        ConstantExpression c1 = new ConstantExpression("property");
        ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("789");
       AdditionExpression a = new AdditionExpression(c1, c2);
       AdditionExpression a1 = new AdditionExpression(expr, c1);
       AdditionExpression a2 = new AdditionExpression(c2, expr);
       a.evaluate(metadata);
       a1.evaluate(metadata);
       a2.evaluate(metadata);

    }
}

