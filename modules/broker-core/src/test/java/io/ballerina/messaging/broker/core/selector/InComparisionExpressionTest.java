package io.ballerina.messaging.broker.core.selector;


import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.List;


public class InComparisionExpressionTest {
    Metadata metadata = new Metadata("queue1", "amq.topic", 0);
    String[] myStrings = new String[]{"Elem1", "Elem2", "Elem3"};
    List mylist = Arrays.asList(myStrings);
    Expression c1 = new ConstantExpression("Elem1");
    Expression c2 = new ConstantExpression("Elem2");
    Expression c3 = new ConstantExpression("Elem3");
    Expression c4 = new ConstantExpression("Elem4");
    Expression c5 = new ConstantExpression("Elem5");
    Expression c6 = new ConstantExpression("Elem6");
    ConstantExpression c7 = ConstantExpression.createFromNumericFloat("120");

    @Test(dataProvider = "Expressions-positive")
    private void testpositiveInComparision (Expression string) {
        InComparisionExpression incmp = new InComparisionExpression(string, mylist);
        boolean actualvalue = incmp.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test(dataProvider = "Expressions-negative")
    private void testnegativeInComparision (Expression string) {
        InComparisionExpression incmp = new InComparisionExpression(string, mylist);
        boolean actualvalue = incmp.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are equal");
    }
    @Test(expectedExceptions = NullPointerException.class, dataProvider = "Expressions-positive")
    private void testnullobject (Expression string) {
        InComparisionExpression incmp = new InComparisionExpression(null, mylist);
        boolean actualvalue = incmp.evaluate(metadata);
        InComparisionExpression incmp1 = new InComparisionExpression(string, null);
        boolean actualvalue1 = incmp1.evaluate(metadata);
    }

    @DataProvider(name = "Expressions-positive")
    public Object[] positive () {
        return new Expression[]{
                c1, c2, c3,
        };

    }
    @DataProvider(name = "Expressions-negative")
    public Object[] negative () {
        return new Expression[]{
                c4, c5, c6, c7
        };

    }

}

