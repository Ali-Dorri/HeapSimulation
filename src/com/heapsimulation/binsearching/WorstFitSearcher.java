package com.heapsimulation.binsearching;

import com.heapsimulation.ChunkReader;

public class WorstFitSearcher implements IBinSearcher {
    public WorstFitSearcher(){

    }

    @Override
    public int getFreeChunkIndex(int size, int[] binStartIndices, ChunkReader chunkReader) {
        return 0;
    }
}
