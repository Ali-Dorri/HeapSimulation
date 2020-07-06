package com.heapsimulation.binmanaging;

import com.heapsimulation.*;
import com.heapsimulation.bincollection.*;

public class WorstFitManager implements IBinManager {
    private SmallBinsArray array;

    public WorstFitManager(){
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
        int chosenChunkIndex = NO_CHUNK;
        int[] binStartIndices = array.getBinStartIndices();
        for(int i = binStartIndices.length; i > -1; i--){
            if(binStartIndices[i] > -1){
                int chunkSize = chunkReader.getUnitDataSize(binStartIndices[i]);
                if(chunkSize >= size){
                    chosenChunkIndex = binStartIndices[i];
                }
                break;
            }
        }

        return chosenChunkIndex;
    }
}
