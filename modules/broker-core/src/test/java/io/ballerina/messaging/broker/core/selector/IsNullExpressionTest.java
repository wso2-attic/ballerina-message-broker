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

public class IsNullExpressionTest {

    Metadata metadata = new Metadata("queue1", "amq.topic", 0);
    Expression c1 = new ConstantExpression("Element");
    Expression c2 = ConstantExpression.createFromNumericDecimal("1000");
    Expression c3 = ConstantExpression.createFromNumericFloat("39.9");
    Expression c4 = ConstantExpression.createFromNumericHex("7856");
    Expression c5 = ConstantExpression.createFromNumericOctal("1653");

    @Test
    public void testisnullpositive() {

        IsNullExpression value = new IsNullExpression(null);
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }

    @Test(dataProvider = "Expressions-negative")
    public void testisnullnegative(Expression string) {

        IsNullExpression value = new IsNullExpression(string);
        boolean actualvalue = value.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }

    @DataProvider(name = "Expressions-negative")
    public Object[] negative() {

        return new Expression[]{
                c1, c2, c3, c4, c5
        };

    }
}
