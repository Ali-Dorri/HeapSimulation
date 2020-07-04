package com.heapsimulation.binsearching;

import com.heapsimulation.ChunkReader;

public class FirstFitSearcher implements IBinSearcher {
    public FirstFitSearcher(){

    }


    @Override
    public int getFreeChunkIndex(int size, int[] binStartIndices, ChunkReader chunkReader) {
        return 0;
    }
}
