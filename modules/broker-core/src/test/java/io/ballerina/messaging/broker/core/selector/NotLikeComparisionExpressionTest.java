package io.ballerina.messaging.broker.core.selector;


import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class NotLikeComparisionExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    Expression c4 = new ConstantExpression("property");

    @Test(dataProvider = "positive-filter-strings")
    private void testNotikeComparision_positive(String filter) {
        NotLikeComparisionExpression value = new NotLikeComparisionExpression(c4, filter, null);
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }

    @Test(dataProvider = "positive-filter-strings-for-escape")
    private void testNotLikeComparision_positive_with_escape(String filter) {
        Expression c5 = new ConstantExpression(filter);
        NotLikeComparisionExpression value = new NotLikeComparisionExpression(c5, "\\_%", "\\");
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test(dataProvider = "negative-filter-strings")
    private void testNotLikeComparision_negative(String filter) {
        NotLikeComparisionExpression value = new NotLikeComparisionExpression(c4, filter, null);
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test(dataProvider = "negative-filter-strings-for-escape")
    private void testNotLikeComparision_negative_with_escape(String filter) {
        Expression c5 = new ConstantExpression(filter);
        NotLikeComparisionExpression value = new NotLikeComparisionExpression(c5, "\\_%", "\\");
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
                "value",
                "V_ALUE",
                "val__ue",
                "12344",
                "123.89_",
                "00_00_00",
                "vAlU__e",
                "12ad_fd234",
        };
    }
    @DataProvider(name = "negative-filter-strings")
    public Object[] negativeFilterStrings() {
        return new String[] {
                "p%",
                "pr%",
                "pro%",
                "prope%",
                "%perty",
                "p%operty",
                "pr%rty",
                "pro%y",
                "pr%y",
                "%rty",
                "%y",
                "%ope%",
                "%p%",
                "p%p%",
                "pr_perty",
                "_roperty",
                "p___erty",
                "p______y",
                "p_______",
                "___p____",
                "p__p___y",
                "p____r__"
        };
    }
    @DataProvider(name = "negative-filter-strings-for-escape")
    public Object[] negativeFilterStringsforescape() {
        return new String[] {
                "_property",
                "_PROPERTY",
                "__property",
                "_1234567",
                "_123.89",
                "_000000",
                "_pRoPeRtY",
                "_12adfd234",
        };
    }
}
