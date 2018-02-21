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

package io.ballerina.messaging.broker.amqp.codec.frames;

import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test queue delete frame for write operation.
 */
public class QueueDeleteTest {

    @Test (dataProvider = "QueueDeleteParams")
    public void testWriteMethod(String name, boolean ifUnused, boolean ifEmpty, boolean noWait) throws Exception {
        ShortString queueName = ShortString.parseString(name);
        QueueDelete frame = new QueueDelete(1, queueName, ifUnused, ifEmpty, noWait);

        long expectedSize = 2L + queueName.getSize() + 1L;

        Assert.assertEquals(frame.getMethodBodySize(), expectedSize, "Expected frame size mismatch");

        ByteBuf buffer = Unpooled.buffer((int) expectedSize);
        frame.writeMethod(buffer);

        buffer.skipBytes(2);
        Assert.assertEquals(ShortString.parse(buffer), queueName);
        short flags = buffer.readByte();

        Assert.assertEquals(ifUnused, (flags & 0x1) == 0x1);
        Assert.assertEquals(ifEmpty, (flags & 0x2) == 0x2);
        Assert.assertEquals(noWait, (flags & 0x4) == 0x4);

    }

    @DataProvider(name = "QueueDeleteParams")
    public static Object[][] queueDeleteParams() {
        return new Object[][] {
                {"myQueue", true, false, true},
                {"queue2", true, false, true},
                {"requestQueue", true, true, false},
                {"responseQueue", false, true, true},
        };
    }
}
