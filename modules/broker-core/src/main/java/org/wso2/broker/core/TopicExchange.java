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

package org.wso2.broker.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.common.data.types.FieldTable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * AMQP topic exchange implementation.
 */
final class TopicExchange implements Exchange {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicExchange.class);

    private final String exchangeName;

    private final BindingsRegistry bindingsRegistry;

    private final FastTopicMatcher fastTopicMatcher;

    private final ReadWriteLock lock;

    TopicExchange(String exchangeName) {
        this.exchangeName = exchangeName;
        this.bindingsRegistry = new BindingsRegistry();
        fastTopicMatcher = new FastTopicMatcher();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public String getName() {
        return exchangeName;
    }

    @Override
    public Type getType() {
        return Type.TOPIC;
    }

    @Override
    public void bind(Queue queue, String routingPattern, FieldTable arguments) throws BrokerException {
        lock.writeLock().lock();
        try {
            // TODO even though we put a binding with routing pattern we never query using that.
            // Therefore we can get rid of this bind call
            bindingsRegistry.bind(queue, routingPattern, arguments);
            fastTopicMatcher.add(routingPattern);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void unbind(Queue queue, String routingPattern) {
        lock.writeLock().lock();
        try {
            bindingsRegistry.unbind(queue, routingPattern);
            fastTopicMatcher.remove(routingPattern);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public BindingSet getBindingsForRoute(String routingKey) {
        if (routingKey.isEmpty()) {
            return BindingSet.emptySet();
        }

        lock.readLock().lock();
        try {
            return fastTopicMatcher.matchingBindings(routingKey);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isUnused() {
        return bindingsRegistry.isEmpty();
    }

    /**
     * Represents fast topic matching algorithm with inverted bitmaps.
     */
    private class FastTopicMatcher {

        private static final String DELIMITER = ".";

        /**
         * Constituent name to represent that a constituent is not available at this location.
         */
        private static final String NULL_CONSTITUENT = "%null%";

        /**
         * Constituent name to represent any constituent except a wildcard.
         */
        private static final String OTHER_CONSTITUENT = "%other%";

        private static final String SINGLE_WORD_WILDCARD = "*";

        private static final String MULTIPLE_WORD_WILDCARD = "#";

        /**
         * Topic names in the BitMaps.
         */
        private final List<String> subscribedTopicList;

        /**
         * Subscribed topics broken down into constituents and indexed.
         */
        private final Map<Integer, String[]> subscribedTopicConstituentsMap;

        /**
         * Set of all the bit maps.
         */
        private final List<Map<String, BitSet>> constituentTables;

        FastTopicMatcher() {
            subscribedTopicList = new ArrayList<>();
            constituentTables = new ArrayList<>();
            subscribedTopicConstituentsMap = new HashMap<>();
        }

        void add(String topicPattern) {
            if (!subscribedTopicList.contains(topicPattern)) {
                subscribedTopicList.add(topicPattern);
                String[] constituents = topicPattern.split(Pattern.quote(DELIMITER), -1);
                subscribedTopicConstituentsMap.put(subscribedTopicList.size() - 1, constituents);

                for (int constituentIndex = 0; constituentIndex < constituents.length; constituentIndex++) {
                    String constituent = constituents[constituentIndex];

                    Map<String, BitSet> constituentTable;
                    if ((constituentIndex + 1) > constituentTables.size()) {
                        constituentTable = newTable(constituentIndex);
                        constituentTables.add(constituentTable);
                    } else {
                        constituentTable = constituentTables.get(constituentIndex);
                    }

                    if (!constituentTable.containsKey(constituent)) {
                        addRow(constituent, constituentTable, constituentIndex);
                    }
                }

                addColumn(constituents, subscribedTopicList.size() - 1);
            }
        }

        private void addColumn(String[] constituents, int newColumnIndex) {
            for (int tableIndex = 0; tableIndex < constituentTables.size(); tableIndex++) {
                Map<String, BitSet> table = constituentTables.get(tableIndex);
                String currentConstituent;
                if (tableIndex < constituents.length) {
                    currentConstituent = constituents[tableIndex];
                    if (MULTIPLE_WORD_WILDCARD.equals(currentConstituent)) {
                        currentConstituent = OTHER_CONSTITUENT;
                    }
                } else {
                    if (MULTIPLE_WORD_WILDCARD.equals(constituents[constituents.length - 1])) {
                        currentConstituent = OTHER_CONSTITUENT;
                    } else {
                        currentConstituent = NULL_CONSTITUENT;
                    }
                }

                for (Map.Entry<String, BitSet> entry : table.entrySet()) {
                    if (entry.getKey().equals(currentConstituent) ||
                            MULTIPLE_WORD_WILDCARD.equals(currentConstituent)) {
                        entry.getValue().set(newColumnIndex);
                    }
                }
            }
        }

        private void addRow(String constituent, Map<String, BitSet> constituentBitMap, int constituentIndex) {

            if (SINGLE_WORD_WILDCARD.equals(constituent) || MULTIPLE_WORD_WILDCARD.equals(constituent)) {
                return;
            }

            BitSet newRow = new BitSet(subscribedTopicList.size());
            for (int i = 0; i < subscribedTopicConstituentsMap.size(); i++) {
                String[] subscribedConstituents = subscribedTopicConstituentsMap.get(i);

                if (constituentIndex < subscribedConstituents.length) {
                    String subscribedConstituent = subscribedConstituents[constituentIndex];
                    if (subscribedConstituent.equals(constituent) || MULTIPLE_WORD_WILDCARD.equals(constituent)
                            || SINGLE_WORD_WILDCARD.equals(constituent)) {
                        newRow.set(i);
                    } else {
                        newRow.set(i, false);
                    }
                } else {
                    if (MULTIPLE_WORD_WILDCARD.equals(subscribedConstituents[subscribedConstituents.length - 1])) {
                        newRow.set(i);
                    } else {
                        newRow.set(i, false);
                    }
                }
            }
            constituentBitMap.put(constituent, newRow);
        }

        /**
         * Creates a new constituent table with null and other constituent values for all the subscriptions.
         *
         * @param constituentIndex index of the constituentTable
         * @return returns the new constituentTable
         */
        private Map<String, BitSet> newTable(int constituentIndex) {
            HashMap<String, BitSet> constituentTable = new HashMap<>();
            BitSet nullBitSet = new BitSet(subscribedTopicList.size());
            BitSet otherBitSet = new BitSet(subscribedTopicList.size());

            for (int i = 0; i < subscribedTopicList.size(); i++) {
                String[] constituents = subscribedTopicConstituentsMap.get(i);

                if (constituentIndex < constituents.length) {
                    String constituent = constituents[constituentIndex];
                    if (MULTIPLE_WORD_WILDCARD.equals(constituent) || SINGLE_WORD_WILDCARD.equals(constituent)) {
                        otherBitSet.set(i);
                    }
                } else {
                    nullBitSet.set(i); // there is no constituent hence null constituent is true

                    if (MULTIPLE_WORD_WILDCARD.equals(constituents[constituents.length - 1])) {
                        otherBitSet.set(i);
                    }
                }
            }
            constituentTable.put(NULL_CONSTITUENT, nullBitSet);
            constituentTable.put(OTHER_CONSTITUENT, otherBitSet);
            return constituentTable;
        }

        void remove(String topicPattern) {

            int removeIndex = subscribedTopicList.indexOf(topicPattern);
            if (removeIndex == -1) {
                LOGGER.debug("Topic pattern {} not found.", topicPattern);
                return;
            }

            for (Map<String, BitSet> table : constituentTables) {
                for (Map.Entry<String, BitSet> entry : table.entrySet()) {
                    BitSet newBitSet = new BitSet(subscribedTopicList.size() - 1);
                    for (int bitIndex = 0; bitIndex < subscribedTopicList.size(); bitIndex++) {
                        if (bitIndex == removeIndex) {
                            continue; // skip the removed bit index
                        }

                        if (entry.getValue().get(bitIndex)) {
                            newBitSet.set(bitIndex);
                        }
                    }
                    table.put(entry.getKey(), newBitSet);
                }
            }
            subscribedTopicList.remove(removeIndex);
            subscribedTopicConstituentsMap.remove(removeIndex);
        }

        BindingSet matchingBindings(String topicName) {

            if (topicName.isEmpty()) {
                return BindingSet.emptySet();
            }

            BitSet matchedBitSet = new BitSet(subscribedTopicList.size());
            matchedBitSet.flip(0, subscribedTopicList.size());

            String[] constituents = topicName.split(Pattern.quote(DELIMITER), -1);

            if (constituents.length > constituentTables.size()) {
                for (int newTableIndex = constituentTables.size(); newTableIndex < constituents.length;
                     newTableIndex++) {
                    Map<String, BitSet> table = newTable(newTableIndex);
                    constituentTables.add(table);
                }
            }

            int nextSetBit = -1;
            for (int tableIndex = 0; tableIndex < constituentTables.size(); tableIndex++) {

                if (tableIndex < constituents.length) {

                    String constituent = constituents[tableIndex];
                    Map<String, BitSet> table = constituentTables.get(tableIndex);

                    if (null == table) {
                        Map<String, BitSet> lastTable = constituentTables.get(constituentTables.size() - 1);
                        BitSet bitSet = lastTable.get(OTHER_CONSTITUENT);
                        matchedBitSet.and(bitSet);
                    } else {
                        BitSet bitSet = table.get(constituent);
                        if (bitSet == null) {
                            bitSet = table.get(OTHER_CONSTITUENT);
                        }
                        matchedBitSet.and(bitSet);
                    }
                } else {
                    // Match the NULL bit set of the table after the constituents. No need to match all the
                    // tables after matching the first NULL constituent
                    Map<String, BitSet> table = constituentTables.get(constituents.length);
                    BitSet bitSet = table.get(NULL_CONSTITUENT);
                    matchedBitSet.and(bitSet);
                    nextSetBit = matchedBitSet.nextSetBit(0);
                    break;
                }

                // If nothing matches we can safely assume nothing will match
                nextSetBit = matchedBitSet.nextSetBit(0);
                if (nextSetBit == -1) {
                    break;
                }
            }

            // If there are more constituents check the last bit map tables other constituent for matching topics
            if (constituentTables.size() < constituents.length) {
                Map<String, BitSet> lastTable = constituentTables.get(constituentTables.size() - 1);
                BitSet otherBitSet = lastTable.get(OTHER_CONSTITUENT);
                matchedBitSet.and(otherBitSet);
                nextSetBit = matchedBitSet.nextSetBit(0);
            }

            BindingSet matchedBindingSet = new BindingSet();
            while (nextSetBit > -1) {
                String subscribedQueue = subscribedTopicList.get(nextSetBit);
                BindingSet bindingSet = bindingsRegistry.getBindingsForRoute(subscribedQueue);
                matchedBindingSet.add(bindingSet);
                nextSetBit = matchedBitSet.nextSetBit(nextSetBit + 1);
            }

            return matchedBindingSet;
        }
    }
}
