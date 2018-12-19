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

package io.ballerina.messaging.broker.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test chunk converter.
 */
public class ChunkConverterTest {

    @Test
    public void testConvertLarge() {
        ChunkConverter converter = new ChunkConverter(10);

        int chunkLength = 12;
        String stringData = RandomStringGenerator.randomString(chunkLength);
        List<ContentChunk> beforeList = createContentChunks(stringData, 1);

        List<ContentChunk> afterList = converter.convert(beforeList, chunkLength);
        Assert.assertEquals(afterList.size(), 2, "After converting there should be only two chunks");

        String convertedDataString = getString(afterList, chunkLength);
        Assert.assertEquals(convertedDataString, stringData, "Content should be equal after conversion");
    }

    @Test
    public void testConvertLargeMultiple() {
        ChunkConverter converter = new ChunkConverter(10);

        int chunkLength = 30;
        String stringData = RandomStringGenerator.randomString(chunkLength);
        List<ContentChunk> beforeList = createContentChunks(stringData, 2);

        List<ContentChunk> afterList = converter.convert(beforeList, chunkLength);
        Assert.assertEquals(afterList.size(), 3, "After converting there should be only three chunks");

        String convertedDataString = getString(afterList, chunkLength);
        Assert.assertEquals(convertedDataString, stringData, "Content should be equal after conversion");
    }

    @Test
    public void testConvertSmall() {
        ChunkConverter converter = new ChunkConverter(10);

        int chunkLength = 8;
        List<ContentChunk> beforeList = createContentChunk(chunkLength);

        List<ContentChunk> afterList = converter.convert(beforeList, chunkLength);
        Assert.assertEquals(afterList, beforeList, "Smaller chunks should not be converted");
    }

    @Test
    public void testConvertEmpty() {
        ChunkConverter converter = new ChunkConverter(10);

        int chunkLength = 8;
        List<ContentChunk> beforeList = new ArrayList<>();

        List<ContentChunk> afterList = converter.convert(beforeList, chunkLength);
        Assert.assertEquals(afterList, beforeList, "Empty chunk list should not be converted");
    }

    private String getString(List<ContentChunk> chunkList, int chunkLength) {
        ContentReader contentReader = new ContentReader(chunkList);
        ByteBuf convertedByteBuf = contentReader.getNextBytes(chunkLength);
        byte[] convertedContent = new byte[chunkLength];
        convertedByteBuf.getBytes(0, convertedContent);
        return new String(convertedContent, StandardCharsets.UTF_8);
    }

    private List<ContentChunk> createContentChunk(int chunkLength) {
        String stringData = RandomStringGenerator.randomString(chunkLength);
        return createContentChunks(stringData, 1);
    }

    private List<ContentChunk> createContentChunks(String data, int numberOfChunks) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        List<ContentChunk> chunkList = new ArrayList<>(numberOfChunks);

        int chunkSize = dataBytes.length / numberOfChunks;
        for (int i = 0; i < numberOfChunks; i++) {
            int offset = i * chunkSize;
            ByteBuf buffer = Unpooled.wrappedBuffer(dataBytes,
                                                    offset,
                                                    Math.min(chunkSize, dataBytes.length - offset));
            chunkList.add(new ContentChunk(0, buffer));
        }

        return chunkList;
    }

    private static class RandomStringGenerator {
        private static final String ALPHA_NUM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        private static Random rnd = new Random();

        private static String randomString(int len) {
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                sb.append(ALPHA_NUM_CHARS.charAt(rnd.nextInt(ALPHA_NUM_CHARS.length())));
            }
            return sb.toString();
        }
    }
}
