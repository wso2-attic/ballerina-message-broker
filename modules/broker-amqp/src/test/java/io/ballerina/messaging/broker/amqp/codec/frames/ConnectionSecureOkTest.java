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

import io.ballerina.messaging.broker.common.data.types.LongString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConnectionSecureOkTest {

    @Test
    public void testEncodeDecode() throws Exception {
        ConnectionSecureOk testFrame = new ConnectionSecureOk(1, LongString.parse(new byte[0]), null);
        ByteBuf buf = Unpooled.buffer((int) testFrame.getMethodBodySize());
        testFrame.writeMethod(buf);
        ConnectionSecureOk decodedFrame = (ConnectionSecureOk) ConnectionSecureOk.getFactory(null)
                .newInstance(buf, 1, testFrame.getMethodBodySize());

        Assert.assertEquals(decodedFrame.getChannel(), testFrame.getChannel(),
                "Decoded frame's channel should match" + " the original frame's channel");
        Assert.assertEquals(decodedFrame.getMethodBodySize(), testFrame.getMethodBodySize(),
                "Decoded frame's method " + "size should match the original frame's method size");
        Assert.assertEquals(decodedFrame.getPayloadSize(), testFrame.getPayloadSize(),
                "Decoded frame's payload " + "size should match the original frame's payload size");
    }
}
