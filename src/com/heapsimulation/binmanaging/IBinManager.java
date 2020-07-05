package com.heapsimulation.binmanaging;

import com.heapsimulation.*;

public interface IBinManager {
    public static final int NO_CHUNK = -1;

    boolean isSupported(int chunkSize);
    int getStartFreeChunkIndex(int chunkSize);
    void setStartFreeChunkIndex(int chunkSize, int index);
    int getBinFreeChunkCount(int chunkSize, ChunkReader reader);

    /**
     * Search the bins and return the free found chunk index by it's strategy. If didn't find free chunk, return NO_CHUNK.
     * @param size requested chunk size
     * @param chunkReader
     * @return Return the free found chunk index by it's strategy.
     */
    int getFreeChunkIndex(int size, ChunkReader chunkReader, MemoryHeap heap);
}
