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

package io.ballerina.messaging.broker.common.data.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LongStringTest {

    private static final String DEFAULT_DATA_STRING = "TestString";
    private static final LongString DEFAULT_TEST_OBJECT = LongString.parseString(DEFAULT_DATA_STRING);

    @Test
    public void testGetSize() throws Exception {
        Assert.assertEquals(DEFAULT_TEST_OBJECT.getSize(), DEFAULT_DATA_STRING.length() + 4, "getSize should return "
                + "required byte array size");
    }

    @Test
    public void testParse() throws Exception {
        ByteBuf buf = Unpooled.buffer(DEFAULT_DATA_STRING.length() + 4);
        DEFAULT_TEST_OBJECT.write(buf);

        LongString parsedObject = LongString.parse(buf);
        Assert.assertEquals(parsedObject, DEFAULT_TEST_OBJECT, "Encoding and decoding should match to same object");
    }

    @Test
    public void testToString() throws Exception {
        Assert.assertEquals(DEFAULT_TEST_OBJECT.toString(), DEFAULT_DATA_STRING, "toString() should match the data "
                + "string used to create it");
    }

    @Test
    public void testEquals() throws Exception {
        LongString other = LongString.parseString("different string");
        LongString similar = LongString.parseString(DEFAULT_DATA_STRING);

        Assert.assertTrue(DEFAULT_TEST_OBJECT.equals(DEFAULT_TEST_OBJECT),
                          "equals() should return true for similar objects");
        Assert.assertTrue(DEFAULT_TEST_OBJECT.equals(similar),
                          "equals() should return true for similar objects");

        Assert.assertFalse(DEFAULT_TEST_OBJECT.equals(other), "equals() should return false for different objects");
        Assert.assertFalse(DEFAULT_TEST_OBJECT.equals(DEFAULT_DATA_STRING),
                           "equals() should return false for different objects");
    }

    @Test
    public void testHashCode() throws Exception {
        LongString similar = LongString.parseString(DEFAULT_DATA_STRING);
        Assert.assertEquals(similar.hashCode(), DEFAULT_TEST_OBJECT.hashCode(), "Hashcode should match for similar "
                + "data");
    }

    @Test(expectedExceptions = Exception.class)
    public void testInvalidBuffer() throws Exception {
        ByteBuf buf = Unpooled.buffer(DEFAULT_DATA_STRING.length() + 4);
        buf.writeLong(-2);

        LongString.parse(buf);
    }

}
