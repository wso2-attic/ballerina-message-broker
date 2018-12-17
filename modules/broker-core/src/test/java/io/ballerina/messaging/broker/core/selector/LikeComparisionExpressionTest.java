package io.ballerina.messaging.broker.core.selector;


import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LikeComparisionExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    Expression c4 = new ConstantExpression("value");

    @Test(dataProvider = "positive-filter-strings")
    private void testLikeComparision_positive(String filter) {
        LikeComparisionExpression value = new LikeComparisionExpression(c4, filter, null);
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }

    @Test(dataProvider = "positive-filter-strings-for-escape")
    private void testLikeComparision_positive_with_escape(String filter) {
        Expression c5 = new ConstantExpression(filter);
        LikeComparisionExpression value = new LikeComparisionExpression(c5, "\\_%", "\\");
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test(dataProvider = "negative-filter-strings")
    private void testLikeComparision_negative(String filter) {
        LikeComparisionExpression value = new LikeComparisionExpression(c4, filter, null);
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test(dataProvider = "negative-filter-strings-for-escape")
    private void testLikeComparision_negative_with_escape(String filter) {
        Expression c5 = new ConstantExpression(filter);
        LikeComparisionExpression value = new LikeComparisionExpression(c5, "\\_%", "\\");
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }

    @DataProvider(name = "positive-filter-strings")
    public Object[] positiveFilterStrings() {
        return new String[] {
                "v%",
                "va%",
                "val%",
                "valu%",
                "%alue",
                "v%lue",
                "va%ue",
                "val%e",
                "va%e",
                "%lue",
                "%e",
                "%lu%",
                "%l%",
                "v%l%",
                "v_lue",
                "_alue",
                "v__ue",
                "v___e",
                "v____",
                "__l__",
                "v_l_e",
                "v__u_"

        };
    }
    @DataProvider(name = "positive-filter-strings-for-escape")
    public Object[] positiveFilterStringsforescape() {
        return new String[] {
                "_value",
                "_VALUE",
                "__value",
                "_12344",
                "_123.89",
                "_000000",
                "_vAlUe",
                "_12adfd234",
        };
    }
    @DataProvider(name = "negative-filter-strings")
    public Object[] negativeFilterStrings() {
        return new String[] {
                "",
                "v\\%",
                "V%",
                "%aLue"

        };
    }
    @DataProvider(name = "negative-filter-strings-for-escape")
    public Object[] negativeFilterStringsforescape() {
        return new String[] {
                "",
                "value",
                "%_value"

        };
    }

}
