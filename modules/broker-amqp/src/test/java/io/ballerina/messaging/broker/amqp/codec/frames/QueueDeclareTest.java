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

import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.Assert;
import org.testng.annotations.Test;

public class QueueDeclareTest {
    @Test
    public void testEncodeDecode() throws Exception {
        QueueDeclare testFrame = new QueueDeclare(1,
                                                  ShortString.parseString("queue"),
                                                  true,
                                                  true,
                                                  true,
                                                  true,
                                                  true,
                                                  FieldTable.EMPTY_TABLE);
        ByteBuf buf = Unpooled.buffer((int) testFrame.getMethodBodySize());
        testFrame.writeMethod(buf);
        QueueDeclare decodedFrame = (QueueDeclare) QueueDeclare.getFactory()
                                                               .newInstance(buf, 1, testFrame.getMethodBodySize());

        Assert.assertEquals(decodedFrame.getChannel(), testFrame.getChannel(), "Decoded frame's channel should match"
                + " the original frame's channel");
        Assert.assertEquals(decodedFrame.getQueue(), testFrame.getQueue(), "Decoded frame's queue should match"
                + " the original frame's queue");
        Assert.assertEquals(decodedFrame.isPassive(), testFrame.isPassive(), "Decoded frame's passive bit should match"
                + " the original frame's passive bit");
        Assert.assertEquals(decodedFrame.isDurable(), testFrame.isDurable(), "Decoded frame's durable bit should match"
                + " the original frame's durable bit");
        Assert.assertEquals(decodedFrame.isExclusive(), testFrame.isExclusive(), "Decoded frame's exclusive bit "
                + "should match the original frame's exclusive bit");
        Assert.assertEquals(decodedFrame.isAutoDelete(), testFrame.isAutoDelete(), "Decoded frame's auto-delete bit "
                + "should match the original frame's auto-delete bit");
        Assert.assertEquals(decodedFrame.isNoWait(), testFrame.isNoWait(), "Decoded frame's no-wait bit should match"
                + " the original frame's no-wait bit");
        Assert.assertEquals(decodedFrame.getArguments(), testFrame.getArguments(), "Decoded frame's arguments "
                + "should match the original frame's arguments");

    }

    @Test
    public void testEncodeDecodeWithFalseFlags() throws Exception {
        QueueDeclare testFrame = new QueueDeclare(1,
                                                  ShortString.parseString("queue"),
                                                  false,
                                                  false,
                                                  false,
                                                  false,
                                                  false,
                                                  FieldTable.EMPTY_TABLE);
        ByteBuf buf = Unpooled.buffer((int) testFrame.getMethodBodySize());
        testFrame.writeMethod(buf);
        QueueDeclare decodedFrame = (QueueDeclare) QueueDeclare.getFactory()
                                                               .newInstance(buf, 1, testFrame.getMethodBodySize());

        Assert.assertEquals(decodedFrame.getChannel(), testFrame.getChannel(), "Decoded frame's channel should match"
                + " the original frame's channel");
        Assert.assertEquals(decodedFrame.isPassive(), testFrame.isPassive(), "Decoded frame's passive bit should match"
                + " the original frame's passive bit");
        Assert.assertEquals(decodedFrame.isDurable(), testFrame.isDurable(), "Decoded frame's durable bit should match"
                + " the original frame's durable bit");
        Assert.assertEquals(decodedFrame.isExclusive(), testFrame.isExclusive(), "Decoded frame's exclusive bit "
                + "should match the original frame's exclusive bit");
        Assert.assertEquals(decodedFrame.isAutoDelete(), testFrame.isAutoDelete(), "Decoded frame's auto-delete bit "
                + "should match the original frame's auto-delete bit");
        Assert.assertEquals(decodedFrame.isNoWait(), testFrame.isNoWait(), "Decoded frame's no-wait bit should match"
                + " the original frame's no-wait bit");
        Assert.assertEquals(decodedFrame.getArguments(), testFrame.getArguments(), "Decoded frame's arguments "
                + "should match the original frame's arguments");

    }
}
