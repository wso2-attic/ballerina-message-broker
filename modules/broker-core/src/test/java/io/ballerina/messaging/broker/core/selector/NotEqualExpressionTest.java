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
import org.testng.annotations.Test;

public class NotEqualExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    ConstantExpression expr1 = ConstantExpression.createFromNumericDecimal("120");
    ConstantExpression expr2 = ConstantExpression.createFromNumericHex("5678");
    ConstantExpression expr3 = ConstantExpression.createFromNumericFloat("150");
    Expression expr4 = new ConstantExpression("value");
    Expression expr5 = new ConstantExpression("value");
    @Test
    private void testNotEqualitypositive() {

        NotEqualExpression equal = new NotEqualExpression(expr1, expr3); // two numerical values

        boolean actualvalue = equal.evaluate(metadata);

        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");

    }
    @Test
    private void testNotEqualitynegative() {
        NotEqualExpression equal = new NotEqualExpression(expr1, expr2); // two numerical values
        NotEqualExpression equal1 = new NotEqualExpression(expr4, expr5); // two string values
        NotEqualExpression equal2 = new NotEqualExpression(expr1, expr4); // numerical and string value
        boolean actualvalue = equal.evaluate(metadata);
        boolean actualvalue1 = equal1.evaluate(metadata);
        boolean actualvalue2 = equal2.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
        Assert.assertEquals(actualvalue1, expectedvalue, "values are not equal");
        Assert.assertEquals(actualvalue2, expectedvalue, "values are not equal");
    }
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () throws Exception {
        NotEqualExpression equal = new NotEqualExpression(null, expr1);
        equal.evaluate(metadata);
        NotEqualExpression equal1 = new NotEqualExpression(expr2, null);
        equal1.evaluate(metadata);
        NotEqualExpression equal2 = new NotEqualExpression(null, null);
        equal2.evaluate(metadata);
    }
}
