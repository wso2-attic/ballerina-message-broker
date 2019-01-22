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

public class NotOperationExpressionTest {

    private Metadata metadata = new Metadata("queue1", "amq.topic", 0);
    private ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89");
    private ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("89");
    private ConstantExpression c3 = ConstantExpression.createFromNumericFloat("99");
    private BooleanExpression equal = new EqualityExpression(c1, c2); // true
    private BooleanExpression equal3 = new EqualityExpression(c2, c3); // false

    @Test
    public void testOrExpressionpositive() throws Exception {

        NotOperationExpression expr = new NotOperationExpression(equal3);
        boolean actualvalue = expr.evaluate(metadata);
        boolean expectedvalue = true;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }

    @Test
    public void testOrExpressionnegative() throws Exception {

        NotOperationExpression expr = new NotOperationExpression(equal);
        boolean actualvalue = expr.evaluate(metadata);
        boolean expectedvalue = false;
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equal");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testnullobject() {

        NotOperationExpression a = new NotOperationExpression(null);
        a.evaluate(metadata);

    }
}

