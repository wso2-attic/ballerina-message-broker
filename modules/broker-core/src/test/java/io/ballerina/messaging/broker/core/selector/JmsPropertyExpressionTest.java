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
    private void testJmsPropertyExpression() {
        Metadata metadata =  new Metadata("queue1", "amq.topic", 0);
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
        Assert.assertEquals(actualvalue1 , expectedvalue1, "values are not equals");
    }
}
