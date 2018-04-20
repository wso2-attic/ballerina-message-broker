package io.ballerina.messaging.broker.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter used to convert the chunk sizes  less than a configured maximum.
 */
public class ChunkConverter {

    private int maxChunkSizeLimit;

    public ChunkConverter(int maxChunkSizeLimit) {
        this.maxChunkSizeLimit = maxChunkSizeLimit;
    }

    public List<ContentChunk> convert(List<ContentChunk> chunkList, long totalLength) {
        if (chunkList.isEmpty() || isChunksUnderLimit(chunkList)) {
            return chunkList;
        }

        ArrayList<ContentChunk> convertedChunks = new ArrayList<>();

        long pendingBytes = totalLength;
        long offset = 0;
        ContentReader contentReader = new ContentReader(chunkList);

        while (pendingBytes > 0) {
            long newBufferLength = Math.min(pendingBytes, maxChunkSizeLimit);
            ContentChunk newChunk = new ContentChunk(offset,
                                                     contentReader.getNextBytes((int) newBufferLength));
            convertedChunks.add(newChunk);

            pendingBytes = pendingBytes - newBufferLength;
            offset = offset + newBufferLength;
        }

        return convertedChunks;
    }

    private boolean isChunksUnderLimit(List<ContentChunk> chunkList) {
        boolean allChunksUnderLimit = true;
        for (ContentChunk chunk : chunkList) {
            if (chunk.getByteBuf().readableBytes() > maxChunkSizeLimit) {
                allChunksUnderLimit = false;
                break;
            }
        }
        return allChunksUnderLimit;
    }

}
