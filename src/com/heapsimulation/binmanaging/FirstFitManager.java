package com.heapsimulation.binmanaging;

import com.heapsimulation.*;
import com.heapsimulation.bincollection.*;

public class FirstFitManager implements IBinManager {
    private SmallBinsArray array;

    public FirstFitManager(){
        array = new SmallBinsArray();
    }

    @Override
    public boolean isSupported(int chunkSize) {
        return array.isSupported(chunkSize);
    }

    @Override
    public int getStartFreeChunkIndex(int chunkSize) {
        return array.getStartFreeChunkIndex(chunkSize);
    }

    @Override
    public void setStartFreeChunkIndex(int chunkSize, int index) {
        array.setStartFreeChunkIndex(chunkSize, index);
    }

    @Override
    public int getBinFreeChunkCount(int chunkSize, ChunkReader reader) {
        return array.getBinFreeChunkCount(chunkSize, reader);
    }

    @Override
    public int getFreeChunkIndex(int size, ChunkReader chunkReader, MemoryHeap heap) {
        int chosenChunkIndex = chunkReader.getMemoryLength();
        int minimumBinIndex = size / HeapUtility.CHUNK_UNIT - 1;
        int[] binStartIndices = array.getBinStartIndices();
        for(int i = minimumBinIndex; i < binStartIndices.length; i++){
            if(binStartIndices[i] > -1 && binStartIndices[i] < chosenChunkIndex){
                chosenChunkIndex = binStartIndices[i];
            }
        }

        if(chosenChunkIndex == chunkReader.getMemoryLength()){
            chosenChunkIndex = NO_CHUNK;
        }

        return chosenChunkIndex;
    }
}
