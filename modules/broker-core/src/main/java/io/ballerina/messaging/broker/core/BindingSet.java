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

import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldValue;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Set of {@link Binding} objects organised into bindings with filters and without filters.
 */
public class BindingSet {

    private static final BindingSet EMPTY_SET = new EmptyBindingSet();

    public static BindingSet emptySet() {
        return EMPTY_SET;
    }

    private final Map<Queue, Binding> filteredQueueBindings;

    private final Map<Queue, Binding> unfilteredQueueBindings;


    BindingSet() {
        filteredQueueBindings = new ConcurrentHashMap<>();
        unfilteredQueueBindings = new ConcurrentHashMap<>();
    }

    boolean add(Binding binding) throws ValidationException {

        Binding existingBinding = validateBinding(binding);

        if (Objects.isNull(existingBinding)) {
            Map<Queue, Binding> queueBindingMap;
            FieldValue selectorValue = binding.getArgument(Binding.JMS_SELECTOR_ARGUMENT);
            if (Objects.nonNull(selectorValue) && !selectorValue.getValue().toString().isEmpty()) {
                queueBindingMap = filteredQueueBindings;
            } else {
                queueBindingMap = unfilteredQueueBindings;
            }
            queueBindingMap.put(binding.getQueue(), binding);
            return true;
        }
        return false;

    }

    private Binding validateBinding(Binding binding) throws ValidationException {
        Binding existingBinding = unfilteredQueueBindings.get(binding.getQueue());
        if (Objects.isNull(existingBinding)) {
            existingBinding = filteredQueueBindings.get(binding.getQueue());
        }

        if (Objects.nonNull(existingBinding) && !existingBinding.equals(binding)) {
            throw new ValidationException("Similar binding with different arguments already exist.");
        }

        return existingBinding;
    }

    void add(BindingSet bindingSet) {
        bindingSet.filteredQueueBindings.forEach(filteredQueueBindings::put);
        bindingSet.unfilteredQueueBindings.forEach(unfilteredQueueBindings::put);
    }

    public void remove(Queue queue) {
        Binding binding = filteredQueueBindings.remove(queue);
        if (Objects.isNull(binding)) {
            binding = unfilteredQueueBindings.remove(queue);
        }

        if (Objects.nonNull(binding)) {
            queue.getQueueHandler().removeBinding(binding);
        }
    }

    public Collection<Binding> getUnfilteredBindings() {
        return unfilteredQueueBindings.values();
    }

    boolean isEmpty() {
        return filteredQueueBindings.isEmpty() && unfilteredQueueBindings.isEmpty();
    }

    public Collection<Binding> getFilteredBindings() {
        return filteredQueueBindings.values();
    }

    /**
     * Empty binding set implementation.
     */
    private static class EmptyBindingSet extends BindingSet {

        private EmptyBindingSet() {
            super();
        }

        @Override
        boolean add(Binding binding) {
            throw new UnsupportedOperationException("Cannot modify Unmodifiable binding set.");
        }

        @Override
        void add(BindingSet bindingSet) {
            throw new UnsupportedOperationException("Cannot modify Unmodifiable binding set.");
        }

        @Override
        public void remove(Queue queue) {
            throw new UnsupportedOperationException("Cannot modify Unmodifiable binding set.");
        }

        @Override
        boolean isEmpty() {
            return true;
        }
    }
}
