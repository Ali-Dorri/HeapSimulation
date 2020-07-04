package com.heapsimulation.binsearching;

import com.heapsimulation.ChunkReader;

public class WorstFitSearcher implements IBinSearcher {
    public WorstFitSearcher(){

    }

    @Override
    public int GetFreeBlockIndex(int size, int[] binStartIndices, ChunkReader traverser) {
        return 0;
    }
}
