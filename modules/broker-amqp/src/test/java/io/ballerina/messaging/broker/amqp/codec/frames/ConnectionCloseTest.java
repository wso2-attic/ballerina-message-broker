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

import io.ballerina.messaging.broker.amqp.codec.ConnectionException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConnectionCloseTest {

    @Test
    public void testEncodeDecode() throws Exception {
        ConnectionClose testFrame
                = ConnectionClose.getInstance((short) 20,
                                              (short) 10,
                                              new ConnectionException(ConnectionException.CHANNEL_ERROR,
                                                                      "Test exception"));
        ByteBuf buf = Unpooled.buffer((int) testFrame.getMethodBodySize());
        testFrame.writeMethod(buf);
        ConnectionClose decodedFrame = (ConnectionClose) ConnectionClose.getFactory()
                                                               .newInstance(buf, 1, testFrame.getMethodBodySize());

        Assert.assertEquals(decodedFrame.getChannel(), testFrame.getChannel(), "Decoded frame's channel should match"
                + " the original frame's channel");
        Assert.assertEquals(decodedFrame.getClassId(), testFrame.getClassId(), "Decoded frame's class id should "
                + "match the original frame's class id");
        Assert.assertEquals(decodedFrame.getMethodId(), testFrame.getMethodId(), "Decoded frame's method id  "
                + "should match the original frame's method id");
        Assert.assertEquals(decodedFrame.getReplyCode(), testFrame.getReplyCode(), "Decoded frame's reply code "
                + "should match the original frame's mandatory bit");
        Assert.assertEquals(decodedFrame.getReplyText(), testFrame.getReplyText(), "Decoded frame's reply text "
                + "should match the original frame's immediate bit");
    }

}
