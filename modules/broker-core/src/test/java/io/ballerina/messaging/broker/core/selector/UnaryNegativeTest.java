package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnaryNegativeTest {
        private Metadata metadata = new Metadata("queue1", "amq.topic", 0);
        private ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89");
        private ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("89");
        private ConstantExpression c3 = ConstantExpression.createFromNumericFloat("99");
        private ConstantExpression c4 = ConstantExpression.createFromNumericOctal("3456");

    @Test(dataProvider = "positive-Expressions-values")
    private void testUnaryNegative_positive (Expression values) throws Exception {
        UnaryNegative expr = new UnaryNegative(values);
        Object actualvalue = expr.evaluate(metadata);
        long expectedvalue = -120;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
        UnaryNegative expr1 = new UnaryNegative(ConstantExpression.createFromNumericFloat("120"));
        Object actualvalue1 = expr1.evaluate(metadata);
        double expectedvalue1 = -120;
        Assert.assertEquals(actualvalue1, expectedvalue1,  "values are not equal");
    }
    @Test(expectedExceptionsMessageRegExp = "value is not a number", dataProvider = "negative-Expressions-values")
    private void testUnaryNegative_negative (Expression values) throws Exception {
        UnaryNegative expr = new UnaryNegative(values);
        expr.evaluate(metadata);
    }
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        UnaryNegative a = new UnaryNegative(null);
        a.evaluate(metadata);
    }
    @DataProvider(name = "positive-Expressions-values")
    public Object[] postive () {
        return new Expression[]{
       ConstantExpression.createFromNumericDecimal("120"),
       ConstantExpression.createFromNumericHex("5678"),
       ConstantExpression.createFromNumericOctal("170"),
        };
    }
      @DataProvider(name = "negative-Expressions-values")
       public Object[] negative () {
        return new Expression[]{
              new ConstantExpression("value"),
              new ConstantExpression(" "),
        };
    }
}
