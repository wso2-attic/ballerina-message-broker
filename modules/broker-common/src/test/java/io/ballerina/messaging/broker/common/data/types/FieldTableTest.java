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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FieldTableTest {

    private ShortString testKey = ShortString.parseString("key");
    private FieldValue testValue = FieldValue.parseLongInt(1);
    private FieldTable testFieldTable;

    @BeforeClass
    public void setup() {
        Map<ShortString, FieldValue> properties = new HashMap<>();
        properties.put(testKey, testValue);
        testFieldTable = new FieldTable(properties);
    }

    @Test
    public void testGetSize() throws Exception {
        Assert.assertEquals(testFieldTable.getSize(),
                            4 + testKey.getSize() + testValue.getSize(),
                            "Empty table should have size 4");
    }

    @Test
    public void testParse() throws Exception {
        ByteBuf buf = Unpooled.buffer((int) testFieldTable.getSize());
        testFieldTable.write(buf);

        FieldTable parsedTable = FieldTable.parse(buf);

        Assert.assertEquals(parsedTable, testFieldTable, "Encoding and decoding should result in the same object");
    }

    @Test
    public void testParseWithoutCalculatedSize() throws Exception {
        Map<ShortString, FieldValue> properties = new HashMap<>();
        properties.put(testKey, testValue);
        FieldTable newFieldTable = new FieldTable(properties);
        ByteBuf buf = Unpooled.buffer((int) testFieldTable.getSize());
        newFieldTable.write(buf);

        FieldTable parsedTable = FieldTable.parse(buf);

        Assert.assertEquals(parsedTable, testFieldTable, "Encoding and decoding should result in the same object");
    }

    @Test
    public void testHashCode() throws Exception {
        Map<ShortString, FieldValue> properties = new HashMap<>();
        properties.put(testKey, testValue);
        FieldTable similar = new FieldTable(properties);
        Assert.assertEquals(similar.hashCode(), testFieldTable.hashCode(), "Hashcode should match for similar "
                + "data");
    }


    @Test
    public void testEquals() throws Exception {
        FieldValue otherType = FieldValue.parseLongString("Test");
        Map<ShortString, FieldValue> properties = new HashMap<>();
        properties.put(testKey, testValue);
        FieldTable similar = new FieldTable(properties);

        Assert.assertTrue(testFieldTable.equals(testFieldTable),
                          "equals() should return true for similar objects");
        Assert.assertTrue(testFieldTable.equals(similar),
                          "equals() should return true for similar objects");

        Assert.assertFalse(testFieldTable.equals(new FieldTable(Collections.emptyMap())),
                           "equals() should return false for different objects");
        Assert.assertFalse(testFieldTable.equals(otherType), "equals() should return false for different objects");
    }

}
