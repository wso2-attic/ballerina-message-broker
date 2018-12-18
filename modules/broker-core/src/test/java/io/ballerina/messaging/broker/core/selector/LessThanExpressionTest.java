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

public class LessThanExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("200");
    ConstantExpression c2 = ConstantExpression.createFromNumericHex("5678");
    ConstantExpression c3 = ConstantExpression.createFromNumericFloat("120");
    Expression c4 = new ConstantExpression("value");
    @Test
    private void testLessThan_positive() {
        LessThanExpression equal = new LessThanExpression(c2, c1); //two different values
        boolean actualvalue = equal.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test
    private void testLessThan_negative() {
        LessThanExpression equal1 = new LessThanExpression(c2, c3); //two equal values
        LessThanExpression equal2 = new LessThanExpression(c4, c3); // string expression value
        boolean actualvalue1 = equal1.evaluate(metadata);
        boolean actualvalue2 = equal2.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue1, expectedvalue, "values are not equal");
        Assert.assertEquals(actualvalue2, expectedvalue, "values are not equal");

    }
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () throws Exception {
        LessThanExpression equal = new LessThanExpression(null, c1);
        equal.evaluate(metadata);
        LessThanExpression equal1 = new LessThanExpression(c2, null);
        equal1.evaluate(metadata);
        LessThanExpression equal2 = new LessThanExpression(null, null);
        equal2.evaluate(metadata);
    }
}
