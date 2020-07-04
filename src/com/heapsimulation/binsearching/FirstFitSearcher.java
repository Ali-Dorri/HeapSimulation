package com.heapsimulation.binsearching;

import com.heapsimulation.ChunkReader;

public class FirstFitSearcher implements IBinSearcher {
    public FirstFitSearcher(){

    }


    @Override
    public int GetFreeBlockIndex(int size, int[] binStartIndices, ChunkReader traverser) {
        return 0;
    }
}
