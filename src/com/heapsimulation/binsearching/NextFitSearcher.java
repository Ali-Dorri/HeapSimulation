package com.heapsimulation.binsearching;

import com.heapsimulation.ChunkReader;

public class NextFitSearcher implements  IBinSearcher {
    public NextFitSearcher(){

    }

    @Override
    public int getFreeChunkIndex(int size, int[] binStartIndices, ChunkReader chunkReader) {
        return 0;
    }
}
