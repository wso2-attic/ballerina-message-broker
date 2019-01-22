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

public class AdditionExpressionTest {

    Metadata metadata = new Metadata("queue1", "amq.topic", 0);

    @Test(dataProvider = "positive-Expressions-values")
    public void testadiition(Expression value1, Expression value2) throws Exception {

        AdditionExpression a = new AdditionExpression(value1, value2); //long value and double value
        Object actualvalue = a.evaluate(metadata);
        double expectedvalue = 96.9;
        Assert.assertEquals(expectedvalue, actualvalue);

    }

    @Test(expectedExceptions = NullPointerException.class, dataProvider = "positive-Expressions-values")
    public void testnullobject(Expression value1, Expression value2) throws Exception {

        AdditionExpression a = new AdditionExpression(null, value1);
        AdditionExpression a1 = new AdditionExpression(null, null);
        AdditionExpression a2 = new AdditionExpression(value2, null);
        a.evaluate(metadata);
        a1.evaluate(metadata);
        a2.evaluate(metadata);
    }

    @Test(expectedExceptionsMessageRegExp = "value is not a number", dataProvider = "negative-Expressions-values")
    public void testobjecttype(Expression value1, Expression value2) throws Exception {

        AdditionExpression a = new AdditionExpression(value1, value2);
        a.evaluate(metadata);

    }

    @DataProvider(name = "positive-Expressions-values")
    public Object[][] postive() {
        ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89");
        ConstantExpression c2 = ConstantExpression.createFromNumericFloat("7.9");
        return new Expression[][]{
                new Expression[]
                        {c1, c2},
        };
    }

    @DataProvider(name = "negative-Expressions-values")
    public Object[][] negative() {

        Expression expr = new JmsPropertyExpression("Myproperty");
        ConstantExpression c1 = new ConstantExpression("property");
        ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("789");
        return new Expression[][]{
                new Expression[]{expr, c1},
                new Expression[]{c1, c2},
                new Expression[]{expr, c1},
        };
    }
}

