package io.ballerina.messaging.broker.core.selector;


import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IsNullExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    Expression c1 = new ConstantExpression("Element");
    Expression c2 = ConstantExpression.createFromNumericDecimal("1000");
    Expression c3 = ConstantExpression.createFromNumericFloat("39.9");
    Expression c4 = ConstantExpression.createFromNumericHex("7856");
    Expression c5 = ConstantExpression.createFromNumericOctal("1653");
    @Test
    private void testisnullpositive() {

        IsNullExpression value = new IsNullExpression(null);
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test(dataProvider = "Expressions-negative")
    private void testisnullnegative(Expression string) {
        IsNullExpression value = new IsNullExpression(string);
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @DataProvider(name = "Expressions-negative")
    public Object[] negative () {
        return new Expression[]{
               c1, c2 , c3 , c4, c5
        };

    }
}
