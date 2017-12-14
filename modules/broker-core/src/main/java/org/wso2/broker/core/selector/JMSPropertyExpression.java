/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.core.selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.common.data.types.FieldValue;
import org.wso2.broker.common.data.types.LongInt;
import org.wso2.broker.common.data.types.ShortString;
import org.wso2.broker.core.Metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * JMS message properties and headers related expression.
 */
public class JMSPropertyExpression implements Expression<Metadata> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMSPropertyExpression.class);

    private static final Map<String, Expression<Metadata>> JMS_PROPERTY_EXPRESSIONS = new HashMap<>();

    private final String name;

    private final Expression<Metadata> jmsPropertyExpression;

    public JMSPropertyExpression(String name) {
        this.name = name;
        this.jmsPropertyExpression = JMS_PROPERTY_EXPRESSIONS.get(name);
    }

    static {
        JMS_PROPERTY_EXPRESSIONS.put("JMSDestination", metadata -> null);
        JMS_PROPERTY_EXPRESSIONS.put("JMSCorrelationID", metadata -> metadata.getCorrelationId().toString());
        JMS_PROPERTY_EXPRESSIONS.put("JMSMessageID", metadata -> metadata.getMessageId().toString());
    }

    @Override
    public Object evaluate(Metadata metadata) {
        if (jmsPropertyExpression != null) {
            return jmsPropertyExpression.evaluate(metadata);
        } else {
            FieldValue header = metadata.getHeader(ShortString.parseString(name));
            if (header == null) {
                return null;
            }
            switch (header.getType()) {
                case LONG_STRING:
                case SHORT_STRING:
                    return header.getValue().toString();
                case LONG_INT:
                    return ((LongInt) header.getValue()).getInt();
                default:
                    return null;
                // TODO handle other data types
            }
        }
    }
}
