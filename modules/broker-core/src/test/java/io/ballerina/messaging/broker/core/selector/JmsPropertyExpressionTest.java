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

import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class JmsPropertyExpressionTest {

    @Test
    public void testJmsPropertyExpression() {

        Metadata metadata = new Metadata("queue1", "amq.topic", 0);
        ShortString first = Metadata.CORRELATION_ID;
        ShortString second = Metadata.MESSAGE_ID;
        Map<ShortString, FieldValue> properties = new HashMap<>();
        {
            properties.put(first, FieldValue.parseLongString("correlation-id"));
            properties.put(second, FieldValue.parseLongInt(56546));
        }
        FieldTable properties1 = new FieldTable(properties);
        metadata.setProperties(properties1);
        JmsPropertyExpression prop = new JmsPropertyExpression("JMSCorrelationID");
        Object actualvalue = prop.evaluate(metadata);
        Object expectedvalue = "correlation-id";
        Assert.assertEquals(actualvalue, expectedvalue, "values are not equals");
        JmsPropertyExpression prop1 = new JmsPropertyExpression("JMSMessageID");
        Object actualvalue1 = prop1.evaluate(metadata);
        Object expectedvalue1 = 56546;
        Assert.assertEquals(actualvalue1, expectedvalue1, "values are not equals");
    }
}
