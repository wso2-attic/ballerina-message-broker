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

package io.ballerina.messaging.broker.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Represents fast topic matching algorithm with inverted bitmaps.
 */
public class FastTopicMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastTopicMatcher.class);

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
    private final List<String[]> subscribedTopicConstituentsMap;

    /**
     * Set of all the bit maps.
     */
    private final List<Map<String, BitSet>> constituentTables;

    public FastTopicMatcher() {
        subscribedTopicList = new ArrayList<>();
        constituentTables = new ArrayList<>();
        subscribedTopicConstituentsMap = new ArrayList<>();
    }

    public void add(String topicPattern) {
        if (!subscribedTopicList.contains(topicPattern)) {
            subscribedTopicList.add(topicPattern);
            String[] constituents = topicPattern.split(Pattern.quote(DELIMITER), -1);
            subscribedTopicConstituentsMap.add(subscribedTopicList.size() - 1, constituents);

            for (int constituentIndex = 0; constituentIndex < constituents.length; constituentIndex++) {
                String constituent = constituents[constituentIndex];

                Map<String, BitSet> constituentTable;
                if (constituentIndex < constituentTables.size()) {
                    constituentTable = constituentTables.get(constituentIndex);
                } else {
                    constituentTable = newTable(constituentIndex);
                    constituentTables.add(constituentTable);
                }
                addRow(constituent, constituentTable, constituentIndex);
            }
            addColumns(constituents);
        }
    }

    /**
     * Add columns for all the constituent tables corresponding to a specific subscription pattern.
     *
     * @param constituents constituent list of the subscription pattern.
     */
    private void addColumns(String[] constituents) {
        int colNum = subscribedTopicList.size() - 1;

        for (int tableIndex = 0; tableIndex < constituentTables.size(); tableIndex++) {
            Map<String, BitSet> table = constituentTables.get(tableIndex);
            String constituent;
            if (tableIndex < constituents.length) {
                constituent = constituents[tableIndex];
            } else {
                constituent = constituents[constituents.length - 1];
                if (!MULTIPLE_WORD_WILDCARD.equals(constituent)) {
                    constituent = NULL_CONSTITUENT;
                }
            }
            for (Map.Entry<String, BitSet> entry : table.entrySet()) {
                if (entry.getKey().equals(NULL_CONSTITUENT)) {
                    if (MULTIPLE_WORD_WILDCARD.equals(constituent) || NULL_CONSTITUENT.equals(constituent)) {
                        entry.getValue().set(colNum);
                    }
                } else {
                    if (entry.getKey().equals(constituent) || MULTIPLE_WORD_WILDCARD.equals(constituent)
                            || SINGLE_WORD_WILDCARD.equals(constituent)) {
                        entry.getValue().set(colNum);
                    }
                }
            }
        }
    }

    private void addRow(String constituent, Map<String, BitSet> constituentTable, int tableIndex) {

        BitSet bitSet;
        if (MULTIPLE_WORD_WILDCARD.equals(constituent) || SINGLE_WORD_WILDCARD.equals(constituent)) {
            bitSet = constituentTable.get(OTHER_CONSTITUENT);
        } else {
            bitSet = constituentTable.computeIfAbsent(constituent, k -> new BitSet());
        }

        for (int colNum = 0; colNum < subscribedTopicList.size(); colNum++) {
            String[] subscriberConstituents = subscribedTopicConstituentsMap.get(colNum);
            String columnConstituent;
            if (tableIndex < subscriberConstituents.length) {
                columnConstituent = subscriberConstituents[tableIndex];
            } else {
                columnConstituent = subscriberConstituents[subscriberConstituents.length - 1];
                if (columnConstituent.equals(MULTIPLE_WORD_WILDCARD)) {
                    bitSet.set(colNum);
                }
            }

            if (columnConstituent.equals(constituent) || SINGLE_WORD_WILDCARD.equals(columnConstituent)
                    || MULTIPLE_WORD_WILDCARD.equals(columnConstituent)) {
                bitSet.set(colNum);
            }
        }

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
                if (SINGLE_WORD_WILDCARD.equals(constituent)) {
                    otherBitSet.set(i);
                } else if (MULTIPLE_WORD_WILDCARD.equals(constituent)) {
                    otherBitSet.set(i);
                    nullBitSet.set(i);
                }
            } else {
                if (MULTIPLE_WORD_WILDCARD.equals(constituents[constituents.length - 1])) {
                    otherBitSet.set(i);
                    nullBitSet.set(i);
                } else {
                    nullBitSet.set(i); // There is no constituent hence null constituent is true
                }
            }
        }
        constituentTable.put(NULL_CONSTITUENT, nullBitSet);
        constituentTable.put(OTHER_CONSTITUENT, otherBitSet);
        return constituentTable;
    }

    public void remove(String topicPattern) {

        int removeIndex = subscribedTopicList.indexOf(topicPattern);
        if (removeIndex == -1) {
            LOGGER.debug("Topic pattern {} not found.", topicPattern);
            return;
        }
        subscribedTopicList.remove(removeIndex);

        for (Map<String, BitSet> table : constituentTables) {
            for (Map.Entry<String, BitSet> entry : table.entrySet()) {
                BitSet bitSet = entry.getValue();
                for (int bitIndex = removeIndex; bitIndex < subscribedTopicList.size(); bitIndex++) {
                    bitSet.set(bitIndex, bitSet.get(bitIndex + 1));
                }
                bitSet.clear(subscribedTopicList.size());
            }
        }

        subscribedTopicConstituentsMap.remove(removeIndex);
    }

    /**
     * Method to retrieve matching bindings.
     *
     * @param topicName                 the topic name to match against
     * @param matchedPatternsConsumer   the consumer implementation accepting matches
     */
    public void matchingBindings(String topicName, Consumer<String> matchedPatternsConsumer) {

        if (topicName.isEmpty() || constituentTables.isEmpty() || subscribedTopicList.isEmpty()) {
            return;
        }

        BitSet matchedBitSet = new BitSet(subscribedTopicList.size());
        matchedBitSet.flip(0, subscribedTopicList.size());

        String[] constituents = topicName.split(Pattern.quote(DELIMITER), -1);
        int nextSetBit = -1;
        for (int tableIndex = 0; tableIndex < constituents.length; tableIndex++) {
            if (tableIndex < constituentTables.size()) {
                Map<String, BitSet> table = constituentTables.get(tableIndex);
                BitSet bitSet = table.get(constituents[tableIndex]);
                if (Objects.isNull(bitSet)) {
                    bitSet = table.get(OTHER_CONSTITUENT);
                }
                matchedBitSet.and(bitSet);
            } else {
                Map<String, BitSet> lastTable = constituentTables.get(constituentTables.size() - 1);
                BitSet nullBitSet = lastTable.get(NULL_CONSTITUENT);
                matchedBitSet.and(nullBitSet);
                break;
            }

            nextSetBit = matchedBitSet.nextSetBit(0);
            if (nextSetBit == -1) {
                break;
            }
        }

        if (nextSetBit > -1 && constituents.length < constituentTables.size()) {
            Map<String, BitSet> table = constituentTables.get(constituents.length);
            matchedBitSet.and(table.get(NULL_CONSTITUENT));
        }

        nextSetBit = matchedBitSet.nextSetBit(0);
        while (nextSetBit > -1) {
            String subscribedPattern = subscribedTopicList.get(nextSetBit);
            matchedPatternsConsumer.accept(subscribedPattern);
            nextSetBit = matchedBitSet.nextSetBit(nextSetBit + 1);
        }

    }
}
