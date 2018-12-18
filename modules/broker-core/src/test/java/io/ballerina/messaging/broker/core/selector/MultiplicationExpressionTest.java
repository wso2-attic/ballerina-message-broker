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

public class MultiplicationExpressionTest {
    Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
    @Test
    private void testMultiplication() {

        ConstantExpression c1 = ConstantExpression.createFromNumericDecimal("89");
        ConstantExpression c2 = ConstantExpression.createFromNumericFloat("7.9");
        ConstantExpression c3 = ConstantExpression.createFromNumericHex("676");
        ConstantExpression c4 = ConstantExpression.createFromNumericFloat("67.5");
        MultiplicationExpression a = new MultiplicationExpression(c1 , c2); //long value and double value
        MultiplicationExpression a1 = new MultiplicationExpression(c1, c3); //two long values
        MultiplicationExpression a2 = new MultiplicationExpression(c2, c4); //two double values
        Object actualvalue = a.evaluate(metadata);
        double expectedvalue = 703.1;
        Assert.assertEquals(expectedvalue, actualvalue, "Objects not matching");
        Object actualvalue1 = a1.evaluate(metadata);
        long expectedvalue1 = 534;
        Assert.assertEquals(expectedvalue1, actualvalue1, "Objects not matching");
        Object actualvalue2 = a2.evaluate(metadata);
        double expectedvalue2 = 533.25;
        Assert.assertEquals(expectedvalue2, actualvalue2, "Objects not matching");
    }

    @Test(expectedExceptions = NullPointerException.class)
    private void testnullobject () {
        ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("23");
        MultiplicationExpression a = new MultiplicationExpression(null , c2);
        MultiplicationExpression a1 = new MultiplicationExpression(null, null);
        MultiplicationExpression a2 = new MultiplicationExpression(c2, null);
        a.evaluate(metadata);
        a1.evaluate(metadata);
        a2.evaluate(metadata);
    }
    @Test(expectedExceptionsMessageRegExp = "value is not a number")
    private void testobjecttype() {
        Expression  expr = new JmsPropertyExpression("Myproperty");
        ConstantExpression c1 = new ConstantExpression("property");
        ConstantExpression c2 = ConstantExpression.createFromNumericDecimal("789");
        MultiplicationExpression a = new MultiplicationExpression(c1, c2);
        MultiplicationExpression a1 = new MultiplicationExpression(expr, c1);
        MultiplicationExpression a2 = new MultiplicationExpression(c2, expr);
        Object value = a.evaluate(metadata);
        Object value1 = a1.evaluate(metadata);
        Object value2 = a2.evaluate(metadata);

    }

}
