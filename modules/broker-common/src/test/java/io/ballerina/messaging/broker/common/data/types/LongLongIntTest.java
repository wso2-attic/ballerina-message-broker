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

public class LongLongIntTest {

    private static final LongLongInt TEST_OBJECT = LongLongInt.parse(1L);

    private static final long DATA_VALUE = 1L;

    @Test
    public void testGetSize() {
        Assert.assertEquals(TEST_OBJECT.getSize(), 8, "Size of long-long-int should be 4");
    }

    @Test
    public void testEquals() {
        LongLongInt other = LongLongInt.parse(3);
        LongLongInt similar = LongLongInt.parse(DATA_VALUE);

        Assert.assertTrue(TEST_OBJECT.equals(TEST_OBJECT), "equals() should return true for similar objects");
        Assert.assertTrue(TEST_OBJECT.equals(similar), "equals() should return true for similar objects");

        Assert.assertFalse(TEST_OBJECT.equals(other), "equals() should return false for different objects");
        Assert.assertFalse(TEST_OBJECT.equals(DATA_VALUE), "equals() should return false for different objects");
    }

    @Test
    public void testHashCode() {
        LongLongInt similar = LongLongInt.parse(DATA_VALUE);
        Assert.assertEquals(similar.hashCode(), TEST_OBJECT.hashCode(), "Hashcode should match for similar data");
    }

    @Test
    public void testParse() {
        ByteBuf buf = Unpooled.buffer(8);
        TEST_OBJECT.write(buf);

        LongLongInt parsedObject = LongLongInt.parse(buf);
        Assert.assertEquals(parsedObject, TEST_OBJECT, "Encoding and decoding should result in the same object");
    }
}
