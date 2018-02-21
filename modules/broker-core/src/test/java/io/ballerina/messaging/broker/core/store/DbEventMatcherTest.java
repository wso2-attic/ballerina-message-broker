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

package io.ballerina.messaging.broker.core.store;

import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Metadata;
import io.ballerina.messaging.broker.core.store.DbOperation.DbOpType;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.messaging.broker.core.store.DbOperation.DbOpType.DELETE_MESSAGE;
import static io.ballerina.messaging.broker.core.store.DbOperation.DbOpType.DETACH_MSG_FROM_QUEUE;
import static io.ballerina.messaging.broker.core.store.DbOperation.DbOpType.INSERT_MESSAGE;
import static io.ballerina.messaging.broker.core.store.DbOperation.DbOpType.NO_OP;

public class DbEventMatcherTest {

    /**
     * Tests are written assuming the {@link DbEventMatcher} is running in ring buffer of size 6.
     */
    private static final int BUFFER_SIZE = 6;

    private DbEventMatcher dbEventMatcher;

    private List<DbOperation> operationsList = new ArrayList<>();

    @BeforeMethod
    public void setUp() {
        dbEventMatcher = new DbEventMatcher(BUFFER_SIZE);
    }

    @Test(dataProvider = "testDbOperationData")
    public void testInsertsCancelOut(TestOperationRequest[] testOperationsArray) {

        for (int i = 0; i < testOperationsArray.length; i++) {
            operationsList.add(DbOperation.getFactory().newInstance());
            TestOperationRequest request = testOperationsArray[i];
            if (request.beforeType == INSERT_MESSAGE) {
                operationsList.get(i).insertMessage(new Message(request.messageId,
                                                                new Metadata("queue1", "amq.direct", 0)));
            } else if (request.beforeType == DELETE_MESSAGE) {
                operationsList.get(i).deleteMessage(request.messageId);
            } else {
                operationsList.get(i).detachFromQueue("queue1", request.messageId);
            }
        }

        for (int i = 0; i < testOperationsArray.length; i++) {
            dbEventMatcher.onEvent(operationsList.get(i), i, false);
        }

        for (int i = 0; i < testOperationsArray.length; i++) {
            Assert.assertTrue(operationsList.get(i).acquireForPersisting(),
                    "Must be able to acquire for persisting.");
            Assert.assertEquals(operationsList.get(i).getType(), testOperationsArray[i].afterType,
                    "Operation type after operation doesn't match for operation " + (i + 1) + ".");
        }
    }

    @AfterMethod
    public void tearDown() {
        operationsList.clear();
    }

    @DataProvider(name = "testDbOperationData")
    public static Object[] insertCancelOutData() {
        Object[][][] data = new Object[][][]{
                {
                        // {<before_event>, <message_id>, <after_event>}
                        // Simple insert delete cancel out.
                        {INSERT_MESSAGE, 1, NO_OP},
                        {INSERT_MESSAGE, 2, NO_OP},
                        {INSERT_MESSAGE, 3, NO_OP},

                        {DELETE_MESSAGE, 1, NO_OP},
                        {DELETE_MESSAGE, 2, NO_OP},
                        {DELETE_MESSAGE, 3, NO_OP},
                },
                {
                        // Test with missing cancel out operations
                        {INSERT_MESSAGE, 1, NO_OP},
                        {INSERT_MESSAGE, 2, INSERT_MESSAGE},
                        {INSERT_MESSAGE, 3, NO_OP},

                        {DELETE_MESSAGE, 1, NO_OP},
                        {DELETE_MESSAGE, 3, NO_OP},
                },
                {
                        // Test with disruptor wrap
                        {INSERT_MESSAGE, 1, INSERT_MESSAGE},
                        {DETACH_MSG_FROM_QUEUE, 1, NO_OP},
                        {INSERT_MESSAGE, 2, INSERT_MESSAGE},
                        {INSERT_MESSAGE, 3, INSERT_MESSAGE},
                        {INSERT_MESSAGE, 4, INSERT_MESSAGE},
                        {DETACH_MSG_FROM_QUEUE, 3, NO_OP}, // Wrap the ring buffer at this point

                        {DELETE_MESSAGE, 1, DELETE_MESSAGE},
                        {INSERT_MESSAGE, 5, NO_OP},
                        {DELETE_MESSAGE, 2, DELETE_MESSAGE},
                        {DELETE_MESSAGE, 3, DELETE_MESSAGE},
                        {DELETE_MESSAGE, 4, DELETE_MESSAGE},
                        {INSERT_MESSAGE, 6, INSERT_MESSAGE},

                        {DELETE_MESSAGE, 5, NO_OP},
                }
        };

        return createDataProviderArray(data);
    }

    private static Object[][] createDataProviderArray(Object[][][] data) {
        Object[][] dataProviderArray = new Object[data.length][];
        for (int i = 0; i < data.length; i++) {
            dataProviderArray[i] = new TestOperationRequest[data[i].length];
            for (int j = 0; j < data[i].length; j++) {
                dataProviderArray[i][j] = new TestOperationRequest(
                        (DbOpType) data[i][j][0], (int) data[i][j][1], (DbOpType) data[i][j][2]);
            }
        }
        return dataProviderArray;
    }

    private static class TestOperationRequest {

        private DbOpType beforeType;

        private DbOpType afterType;

        private long messageId;

        TestOperationRequest(DbOpType beforeType, long messageId, DbOpType afterType) {
            this.beforeType = beforeType;
            this.afterType = afterType;
            this.messageId = messageId;
        }
    }
}
