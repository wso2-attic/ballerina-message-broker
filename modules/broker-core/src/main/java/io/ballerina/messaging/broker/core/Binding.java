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

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.common.data.types.LongString;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.selector.BooleanExpression;
import io.ballerina.messaging.broker.core.selector.generated.MessageFilter;

import java.util.Objects;

/**
 * Represents an binding object which binds a {@link Queue} to a given {@link Exchange}.
 */
public final class Binding {

    public static final ShortString JMS_SELECTOR_ARGUMENT = ShortString.parseString("x-filter-jms-selector");

    private final Queue queue;

    private final String bindingPattern;

    private final FieldTable arguments;

    private final BooleanExpression filterExpression;

    private final LongString filterString;

    Binding(Queue queue, String bindingPattern, FieldTable arguments) throws BrokerException {
        this.queue = queue;
        this.bindingPattern = bindingPattern;
        this.arguments = arguments;
        LongString selector = null;
        try {
            FieldValue fieldValue = arguments.getValue(JMS_SELECTOR_ARGUMENT);
            if (Objects.nonNull(fieldValue)
                    && fieldValue.getType() == FieldValue.Type.LONG_STRING
                    && !(selector = (LongString) fieldValue.getValue()).isEmpty()) {

                MessageFilter messageFilter = new MessageFilter(selector.toString());
                filterExpression = messageFilter.parse();
                filterString = selector;
            } else {
                filterExpression = null;
                filterString = null;
            }
        } catch (Exception e) {
            throw new BrokerException("Error parsing the message filter string [ " + selector + " ]", e);
        }

    }

    public Queue getQueue() {
        return queue;
    }

    public String getBindingPattern() {
        return bindingPattern;
    }

    public FieldValue getArgument(ShortString propertyName) {
        return arguments.getValue(propertyName);
    }

    public FieldTable getArguments() {
        return arguments;
    }

    BooleanExpression getFilterExpression() {
        return filterExpression;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Binding) {
            return bindingPattern.equals(((Binding) obj).bindingPattern)
                    && getQueue().equals(((Binding) obj).getQueue())
                    && (Objects.equals(filterString, ((Binding) obj).filterString));

        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bindingPattern, getQueue(), filterString);
    }

    @Override
    public String toString() {
        return "Binding{"
                + "queue=" + queue
                + ", bindingPattern='" + bindingPattern + '\''
                + '}';
    }
}
