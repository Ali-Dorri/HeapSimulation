package com.heapsimulation.binmanaging;

import com.heapsimulation.*;
import com.heapsimulation.bincollection.*;

public interface IBinManager extends IBinCollection {

    /**
     * Search the bins and return the free found chunk index by it's strategy. If didn't find free chunk, return NO_CHUNK.
     * @param size requested chunk size
     * @param chunkReader
     * @return Return the free found chunk index by it's strategy.
     */
    int getFreeChunkIndex(int size, ChunkReader chunkReader, MemoryHeap heap);
}
