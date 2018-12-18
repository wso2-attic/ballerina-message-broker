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

public class AndOperationExpressionTest {
    private Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    private ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89");
    private ConstantExpression c2 = ConstantExpression.createFromNumericFloat("89");
    private ConstantExpression c3 = ConstantExpression.createFromNumericHex("6723");
    private BooleanExpression equal = new EqualityExpression(c1, c2);
    private BooleanExpression equal1 = new EqualityExpression(c1, c2);
     private BooleanExpression equal2 = new EqualityExpression(c1, c3);
    private BooleanExpression equal3 = new EqualityExpression(c2, c3);
    @Test
    private void testAndExpressionpositive () throws Exception {
        AndOperationExpression andOP = new AndOperationExpression(equal, equal1);
        boolean actual1 = andOP.evaluate(metadata);
        boolean expected1 = true;
        Assert.assertEquals(actual1, expected1, "values are not equal");
    }
    @Test
    private void testAndExpressionnegative () throws Exception {
        AndOperationExpression andOp = new AndOperationExpression(equal1, equal2);
        AndOperationExpression andOP1 = new AndOperationExpression(equal2, equal3);
        boolean actualvalue = andOp.evaluate(metadata);
        boolean actualvalue1 = andOP1.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
        Assert.assertEquals(actualvalue1, expectedvalue, "values are not equal");
    }
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        AndOperationExpression a = new AndOperationExpression(null, equal);
        AndOperationExpression a1 = new AndOperationExpression(null, null);
        AndOperationExpression a2 = new AndOperationExpression(equal1, null);
        a.evaluate(metadata);
        a1.evaluate(metadata);
        a2.evaluate(metadata);

    }

}
