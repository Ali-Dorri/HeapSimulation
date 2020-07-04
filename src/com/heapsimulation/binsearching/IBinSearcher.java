package com.heapsimulation.binsearching;

import com.heapsimulation.ChunkReader;

public interface IBinSearcher {
    /**
     * Search the bins and return the free found chunk index by it's strategy. If didn't find free chunk, return NO_CHUNK.
     * @param size requested chunk size
     * @param binStartIndices array of bins which point to index of free chunks
     * @param chunkReader
     * @return Return the free found chunk index by it's strategy.
     */
    int getFreeChunkIndex(int size, int[] binStartIndices, ChunkReader chunkReader);

    static final int NO_CHUNK = -1;
}
