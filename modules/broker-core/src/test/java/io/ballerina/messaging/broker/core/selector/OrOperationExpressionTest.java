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

public class OrOperationExpressionTest {
    private Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    private ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89");
    private ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("89");
    private ConstantExpression c3 = ConstantExpression.createFromNumericFloat("99");
    private BooleanExpression equal = new EqualityExpression(c1, c2);
    private BooleanExpression equal1 = new EqualityExpression(c1, c2);
    private BooleanExpression equal2 = new EqualityExpression(c1, c3);
    private BooleanExpression equal3 = new EqualityExpression(c2, c3);
    @Test
    private void testOrExpressionpositive () throws Exception {
        OrOperationExpression expr = new OrOperationExpression(equal, equal1);
        OrOperationExpression expr1 = new OrOperationExpression(equal1, equal2);
        boolean actualvalue = expr.evaluate(metadata);
        boolean actualvalue1 = expr1.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
        Assert.assertEquals(actualvalue1, expectedvalue, "values are not equal");
    }
    @Test
    private void testOrExpressionnegative () throws Exception {
        OrOperationExpression expr = new OrOperationExpression(equal2, equal3);
        boolean actualvalue = expr.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }
    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        OrOperationExpression a = new OrOperationExpression(null, equal);
        OrOperationExpression a1 = new OrOperationExpression(null, null);
        OrOperationExpression a2 = new OrOperationExpression(equal1, null);
        a.evaluate(metadata);
        a1.evaluate(metadata);
        a2.evaluate(metadata);
    }
}
