/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
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

