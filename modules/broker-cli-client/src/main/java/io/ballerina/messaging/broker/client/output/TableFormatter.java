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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.messaging.broker.client.output;

import io.ballerina.messaging.broker.client.resources.Binding;
import io.ballerina.messaging.broker.client.resources.Consumer;
import io.ballerina.messaging.broker.client.resources.Exchange;
import io.ballerina.messaging.broker.client.resources.Logger;
import io.ballerina.messaging.broker.client.resources.Permission;
import io.ballerina.messaging.broker.client.resources.Queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Print backend responses into tables. This is used for displaying results of 'list' commands.
 */
public class TableFormatter implements ResponseFormatter {

    private static final int TABLE_PADDING = 2;
    private static final String COLUMN_SEPARATOR_BEGIN = "| ";
    private static final String COLUMN_SEPARATOR_END = " |";
    private static final String CORNER_SIGN_BEGIN = "+ ";
    private static final String CORNER_SIGN_END = " +";
    private static final String BLOCK_SEPARATOR = "-";

    /**
     * Name of this formatter class. This will be used when displaying help logs.
     */
    private static final String FORMATTER_NAME = "table";

    @Override
    public void printExchanges(Exchange[] exchanges) {
        if (exchanges.length == 0) {
            return;
        }

        ArrayList<String[]> tempExchanges = new ArrayList<>();

        for (Exchange exchange : exchanges) {
            tempExchanges.add(new String[]{exchange.getName(), exchange.getType(),
                                           Boolean.toString(exchange.isDurable()), exchange.getOwner()});
        }

        printTable(new String[]{Exchange.NAME_TAG, Exchange.TYPE_TAG, Exchange.DURABLE_TAG, Exchange.OWNER_TAG},
                   tempExchanges);

    }

    @Override
    public void printExchange(Exchange exchange) {
        int maxFieldLength = Exchange.DURABLE_TAG.length() + 1;
        String printTemplate = "%-" + maxFieldLength + "s: %s\n";

        OUT_STREAM.printf(printTemplate, Exchange.NAME_TAG, exchange.getName());
        OUT_STREAM.printf(printTemplate, Exchange.TYPE_TAG, exchange.getType());
        OUT_STREAM.printf(printTemplate, Queue.DURABLE_TAG, exchange.isDurable());
        OUT_STREAM.printf(printTemplate, Queue.OWNER_TAG, exchange.getOwner());

        OUT_STREAM.println("\nPermissions");
        OUT_STREAM.println("===========");

        for (Permission permission : exchange.getPermissions()) {
            String permissionsList = permission.getUserGroups()
                                               .stream()
                                               .map(String::toString)
                                               .collect(Collectors.joining(","));
            OUT_STREAM.println(permission.getAction() + ": " + permissionsList);

        }
    }

    @Override
    public void printQueues(Queue[] queues) {
        if (queues.length == 0) {
            return;
        }
        ArrayList<String[]> tempQueues = new ArrayList<>();

        for (Queue queue : queues) {
            tempQueues.add(new String[]{queue.getName(), Integer.toString(queue.getConsumerCount()),
                                        Integer.toString(queue.getCapacity()), Integer.toString(queue.getSize()),
                                        Boolean.toString(queue.isDurable()), Boolean.toString(queue.isAutoDelete()),
                                        queue.getOwner()});
        }

        printTable(new String[]{Queue.NAME_TAG, Queue.CONSUMER_COUNT_TAG, Queue.CAPACITY_TAG, Queue.SIZE_TAG,
                                Queue.DURABLE_TAG, Queue.AUTO_DELETE_TAG, Queue.OWNER_TAG},
                   tempQueues);

    }

    @Override
    public void printLoggers(Logger[] loggers) {
        if (loggers.length == 0) {
            OUT_STREAM.println("Not found");
            return;
        }
        int maxLoggerNameLength = Arrays.stream(loggers)
                                        .mapToInt(logger -> logger.getName().length())
                                        .max()
                                        .getAsInt();

        int maxLoggerNameColumnSize = Math.max(maxLoggerNameLength, Logger.NAME_TAG.length());

        String printTemplate = "%-2s%-" + (maxLoggerNameColumnSize + TABLE_PADDING) + "s%-2s%-15s%-2s\n";


        StringBuffer loggerNameDivider = new StringBuffer();
        for (int i = 0; i < maxLoggerNameColumnSize + TABLE_PADDING; i++) {
            loggerNameDivider.append(Logger.BLOCK_SEPERATOR);
        }

        OUT_STREAM.printf(printTemplate, Logger.CORNER_SIGN_BEGIN, loggerNameDivider.toString(),
                          Logger.CORNER_SIGN_BEGIN, Logger.BLOCK_SEPERATOR_FOR_LEVEL, Logger.CORNER_SIGN_END);
        OUT_STREAM.printf(printTemplate, Logger.COLUMN_SEPERATOR_BEGIN, Logger.NAME_TAG, Logger
                .COLUMN_SEPERATOR_BEGIN, Logger.LEVEL_TAG, Logger.COLUMN_SEPERATOR_END);
        OUT_STREAM.printf(printTemplate, Logger.CORNER_SIGN_BEGIN, loggerNameDivider.toString(),
                          Logger.CORNER_SIGN_BEGIN, Logger.BLOCK_SEPERATOR_FOR_LEVEL, Logger.CORNER_SIGN_END);
        for (Logger logger : loggers) {
            OUT_STREAM.printf(printTemplate, Logger.COLUMN_SEPERATOR_BEGIN, logger.getName(), Logger
                    .COLUMN_SEPERATOR_BEGIN, logger
                    .getLevel(), Logger.COLUMN_SEPERATOR_END);
        }
        OUT_STREAM.printf(printTemplate, Logger.CORNER_SIGN_BEGIN, loggerNameDivider.toString(),
                          Logger.CORNER_SIGN_BEGIN, Logger.BLOCK_SEPERATOR_FOR_LEVEL, Logger.CORNER_SIGN_END);
    }

    @Override
    public void printQueue(Queue queue) {
        int maxFieldLength = Queue.CONSUMER_COUNT_TAG.length() + 1;
        String printTemplate = "%-" + maxFieldLength + "s: %s\n";

        OUT_STREAM.printf(printTemplate, Queue.NAME_TAG, queue.getName());
        OUT_STREAM.printf(printTemplate, Queue.CONSUMER_COUNT_TAG, queue.getConsumerCount());
        OUT_STREAM.printf(printTemplate, Queue.CAPACITY_TAG, queue.getCapacity());
        OUT_STREAM.printf(printTemplate, Queue.SIZE_TAG, queue.getSize());
        OUT_STREAM.printf(printTemplate, Queue.DURABLE_TAG, queue.isDurable());
        OUT_STREAM.printf(printTemplate, Queue.AUTO_DELETE_TAG, queue.isAutoDelete());
        OUT_STREAM.printf(printTemplate, Queue.OWNER_TAG, queue.getOwner());

        OUT_STREAM.println("\nPermissions");
        OUT_STREAM.println("===========");

        for (Permission permission : queue.getPermissions()) {
            String permissionsList = permission.getUserGroups()
                                               .stream()
                                               .map(String::toString)
                                               .collect(Collectors.joining(","));
            OUT_STREAM.println(permission.getAction() + ": " + permissionsList);

        }
    }

    @Override
    public void printExchangeBindings(Binding[] bindings) {
        if (bindings.length == 0) {
            return;
        }

        ArrayList<String[]> tempBindings = new ArrayList<>();

        for (Binding binding : bindings) {
            tempBindings.add(new String[]{binding.getQueueName(), binding.getBindingPattern()});
        }

        printTable(new String[]{Binding.QUEUE_NAME, Binding.BINDING_PATTERN}, tempBindings);
    }

    @Override
    public void printConsumers(Consumer[] consumers) {
        if (consumers.length == 0) {
            return;
        }

        ArrayList<String[]> tempConsumers = new ArrayList<>();

        for (Consumer consumer : consumers) {
            tempConsumers.add(new String[]{Integer.toString(consumer.getId()),
                                           Boolean.toString(consumer.isExclusive()),
                                           Boolean.toString(consumer.isFlowEnabled())});
        }

        printTable(new String[]{Consumer.CONSUMER_ID, Consumer.IS_EXCLUSIVE, Consumer.FLOW_ENABLED}, tempConsumers);

    }

    @Override
    public String toString() {
        return FORMATTER_NAME;
    }


    /**
     * This methods print given set of values in a table format.
     *
     * @param titles titles of each column of the table
     * @param rows   values in rows
     */
    private void printTable(String[] titles, List<String[]> rows) {
        if (titles.length == 0) {
            throw new IllegalArgumentException("Column titles are empty.");
        } else if (rows.isEmpty()) {
            throw new IllegalArgumentException("Table rows are empty.");
        } else if (rows.stream().anyMatch(row -> row.length != titles.length)) {
            throw new IllegalArgumentException("Number of columns in the table rows do not match with table header.");
        } else {
            //calculating maximum column length
            Integer[] columnWidths = new Integer[titles.length];
            IntStream.range(0, titles.length).forEach(column -> columnWidths[column] = titles[column].length());
            rows.forEach(row -> IntStream.range(0, row.length)
                                         .filter(column -> row[column] != null)
                                         .forEach(column -> columnWidths[column] = Math.max(columnWidths[column],
                                                                                            row[column].length())));

            //building print template
            StringBuilder printTemplateBuilder = new StringBuilder("%-" + TABLE_PADDING + "s");
            Arrays.stream(columnWidths)
                  .forEach(columnWidth -> printTemplateBuilder.append("%-").append(columnWidth).append("s%-")
                                                              .append(TABLE_PADDING).append("s"));
            printTemplateBuilder.append("\n");
            String printTemplate = printTemplateBuilder.toString();

            //building header and separators of the table
            ArrayList<String> headerArguments = new ArrayList<>();
            ArrayList<String> separatorArguments = new ArrayList<>();
            Arrays.stream(titles).forEach(column -> headerArguments.addAll(Arrays.asList(COLUMN_SEPARATOR_BEGIN,
                                                                                         column)));
            headerArguments.add(COLUMN_SEPARATOR_END);
            for (int columnWidth : columnWidths) {
                separatorArguments.add(CORNER_SIGN_BEGIN);
                StringBuilder divider = new StringBuilder();
                for (int i = 0; i < columnWidth; i++) {
                    divider.append(BLOCK_SEPARATOR);
                }
                separatorArguments.add(divider.toString());
            }
            separatorArguments.add(CORNER_SIGN_END);

            //printing the table
            OUT_STREAM.printf(printTemplate, (Object[]) separatorArguments.toArray());
            OUT_STREAM.printf(printTemplate, (Object[]) headerArguments.toArray());
            OUT_STREAM.printf(printTemplate, (Object[]) separatorArguments.toArray());

            for (String[] row : rows) {
                ArrayList<String> rowCommands = new ArrayList<>();
                for (String column : row) {
                    rowCommands.add(COLUMN_SEPARATOR_BEGIN);
                    rowCommands.add(column);
                }
                rowCommands.add(COLUMN_SEPARATOR_END);
                OUT_STREAM.printf(printTemplate, (Object[]) rowCommands.toArray());
            }
            OUT_STREAM.printf(printTemplate, (Object[]) separatorArguments.toArray());
        }
    }
}
