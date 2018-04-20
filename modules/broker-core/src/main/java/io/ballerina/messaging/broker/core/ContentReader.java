package io.ballerina.messaging.broker.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;

/**
 * Read content list as a composite chunk.
 */
class ContentReader {
    private List<ContentChunk> chunkList;
    private int currentChunkIndex = 0;
    private int sliceStart = 0;

    ContentReader(List<ContentChunk> chunkList) {
        this.chunkList = chunkList;
    }

    ByteBuf getNextBytes(int size) {
        ContentChunk currentChunk = chunkList.get(currentChunkIndex);
        ByteBuf chunkByteBuf = currentChunk.getByteBuf();
        int readableBytes = chunkByteBuf.readableBytes() - sliceStart;
        if (readableBytes == size) {
            ByteBuf slice = getSlice(chunkByteBuf, size);
            jumpToNextChunk();
            return slice;
        } else if (readableBytes > size) {
            ByteBuf slice = getSlice(chunkByteBuf, size);
            sliceStart = sliceStart + size;
            return slice;
        } else {
            ByteBuf firstSlice = getSlice(chunkByteBuf, readableBytes);
            jumpToNextChunk();
            ByteBuf secondSlice = getNextBytes(size - readableBytes);
            return Unpooled.wrappedBuffer(firstSlice, secondSlice);
        }
    }

    private void jumpToNextChunk() {
        currentChunkIndex++;
        sliceStart = 0;
    }

    private ByteBuf getSlice(ByteBuf chunkByteBuf, int length) {
        return chunkByteBuf.slice(chunkByteBuf.readerIndex() + sliceStart, length);
    }
}
