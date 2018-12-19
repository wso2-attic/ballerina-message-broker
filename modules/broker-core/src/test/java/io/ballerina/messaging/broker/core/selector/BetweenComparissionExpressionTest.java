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


public class BetweenComparissionExpressionTest {
    private Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    private ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89"); // long value
    private ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("8"); // long value
    private ConstantExpression c3 = ConstantExpression.createFromNumericFloat("7.9"); // double value

    @Test
    private void testBetweenExpressionpositive () throws Exception {
        BetweenComparissionExpression between = new BetweenComparissionExpression(c2, c3, c1);
        boolean actual = between.evaluate(metadata);
        boolean expected = true;
        Assert.assertEquals(actual, expected, "values are not equal");
    }
    @Test
    private void testBetweenExpressionnegative () throws Exception {
     BetweenComparissionExpression between = new BetweenComparissionExpression(c1, c2, c3);
     boolean actual = between.evaluate(metadata);
     boolean expected = false;
     Assert.assertEquals(actual, expected, "values are not equal");
    }

    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        BetweenComparissionExpression between = new BetweenComparissionExpression(null, null, null);
        between.evaluate(metadata);
        BetweenComparissionExpression between1 = new BetweenComparissionExpression(null, null, c1);
        between1.evaluate(metadata);
        BetweenComparissionExpression between2 = new BetweenComparissionExpression(c2, null, null);
        between2.evaluate(metadata);
        BetweenComparissionExpression between3 = new BetweenComparissionExpression(c2, null, c1);
        between3.evaluate(metadata);
    }

}

