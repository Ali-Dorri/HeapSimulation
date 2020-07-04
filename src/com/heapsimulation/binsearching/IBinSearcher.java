package com.heapsimulation.binsearching;

import com.heapsimulation.ChunkReader;

public interface IBinSearcher {
    int GetFreeBlockIndex(int size, int[] binStartIndices, ChunkReader traverser);
}
