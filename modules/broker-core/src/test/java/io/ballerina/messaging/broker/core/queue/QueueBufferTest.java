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

package io.ballerina.messaging.broker.core.queue;

import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Metadata;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class QueueBufferTest {

    private Metadata mockMetadata;
    private QueueBuffer.MessageReader messageReader;

    @BeforeClass
    public void setUp() {
        mockMetadata = new Metadata("queue1", "amq.direct", 0);
        messageReader = (buffer, message) -> {
            message.setMetadata(mockMetadata);
            buffer.markMessageFilled(message);
        };
    }

    @Test
    public void testAdd() {
        QueueBuffer queueBuffer = new QueueBuffer(10, 0, messageReader);
        for (int i = 0; i < 10; i++) {
            Message message = new Message(i + 1, mockMetadata);
            queueBuffer.add(message);
            Assert.assertNotNull(message.getMetadata(), "Message data should not be cleared until the in-memory "
                    + "limit is reached");
        }

        for (int i = 0; i < 3; i++) {
            Message message = new Message(i + 1, mockMetadata);
            queueBuffer.add(message);
            Assert.assertNull(message.getMetadata(), "Message data should be cleared when the queue limit is reached");
        }
    }

    @Test
    public void testBareAdd() {
        QueueBuffer queueBuffer = new QueueBuffer(10, 0, messageReader);
        for (int i = 0; i < 12; i++) {
            Message message = new Message(i + 1, null);
            queueBuffer.addBareMessage(message);
        }

        for (int i = 0; i < 12; i++) {
            Message message = queueBuffer.getFirstDeliverable();
            Assert.assertNotNull(message.getMetadata(), "Messages returned from #getFirstDeliverable() should never "
                    + "be empty");
            queueBuffer.remove(message.getInternalId());
        }
    }

    @Test
    public void testIndelibleAdd() {
        QueueBuffer queueBuffer = new QueueBuffer(5, 10, messageReader);
        for (int i = 0; i < 10; i++) {
            Message message = new Message(i + 1, mockMetadata);
            queueBuffer.addIndelibleMessage(message);
        }

        for (int i = 0; i < 10; i++) {
            Message message = queueBuffer.getFirstDeliverable();
            Assert.assertNotNull(message.getMetadata(), "Messages returned from #getFirstDeliverable() should never "
                    + "be empty");
            queueBuffer.remove(message.getInternalId());
        }
    }

    @Test
    public void testIndelibleAddBeyondLimit() {
        QueueBuffer queueBuffer = new QueueBuffer(5, 10, messageReader);
        for (int i = 0; i < 10; i++) {
            Message message = new Message(i + 1, mockMetadata);
            queueBuffer.addIndelibleMessage(message);
        }

        Message extraMessage = new Message(11, mockMetadata);
        Assert.assertFalse(queueBuffer.addIndelibleMessage(extraMessage),
                           "Messages should not be accepted beyond limit");

        for (int i = 0; i < 10; i++) {
            Message message = queueBuffer.getFirstDeliverable();
            Assert.assertNotNull(message.getMetadata(), "Messages returned from #getFirstDeliverable() should never "
                    + "be empty");
            queueBuffer.remove(message.getInternalId());
        }
    }

    @Test
    public void testSize() {
        QueueBuffer queueBuffer = new QueueBuffer(10, 0, messageReader);
        for (int i = 0; i < 12; i++) {
            Message message = new Message(i + 1, mockMetadata);
            queueBuffer.add(message);
        }

        Assert.assertEquals(queueBuffer.size(), 12, "Message size should match the number of added items");
    }

    @Test
    public void testGetFirstDeliverable() {
        QueueBuffer queueBuffer = new QueueBuffer(10, 0, messageReader);
        for (int i = 0; i < 12; i++) {
            Message message = new Message(i + 1, mockMetadata);
            queueBuffer.add(message);
        }

        for (int i = 0; i < 12; i++) {
            Message message = queueBuffer.getFirstDeliverable();
            Assert.assertNotNull(message.getMetadata(), "Messages returned from #getFirstDeliverable() should never "
                    + "be empty");
            queueBuffer.remove(message.getInternalId());
        }

        Assert.assertEquals(queueBuffer.size(), 0, "Buffer size should be 0 after removing all messages");
    }
}
