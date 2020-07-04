package com.heapsimulation.binsearching;

import com.heapsimulation.ChunkReader;

public class NextFitSearcher implements  IBinSearcher {
    public NextFitSearcher(){

    }

    @Override
    public int GetFreeBlockIndex(int size, int[] binStartIndices, ChunkReader traverser) {
        return 0;
    }
}
