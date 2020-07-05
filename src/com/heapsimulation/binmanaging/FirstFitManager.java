package com.heapsimulation.binmanaging;

import com.heapsimulation.*;

public class FirstFitManager implements IBinManager {
    private int[] binStartIndices;

    public FirstFitManager(){
        binStartIndices = new int[HeapUtility.SMALL_BINS_COUNT];
        //make all bins free
        for(int i = 0; i < binStartIndices.length; i++){
            binStartIndices[i] = -1;
        }
    }

    @Override
    public boolean isSupported(int chunkSize) {
        int binIndex = getBinIndex(chunkSize);
        return binIndex > -1 && binIndex < binStartIndices.length;
    }

    @Override
    public int getStartFreeChunkIndex(int chunkSize) {
        int binIndex = getBinIndex(chunkSize);
        if(isSupported(chunkSize)){
            return binStartIndices[binIndex];
        }

        throw new IndexOutOfBoundsException(getUnsupportedSizeError());
    }

    @Override
    public void setStartFreeChunkIndex(int chunkSize, int index) {
        int binIndex = getBinIndex(chunkSize);
        if(isSupported(chunkSize)){
            binStartIndices[binIndex] = index;
        }
    }

    @Override
    public int getBinFreeChunkCount(int chunkSize, ChunkReader reader) {
        int binIndex = getBinIndex(chunkSize);
        int startChunkIndex = binStartIndices[binIndex];

        if(isSupported(chunkSize) && startChunkIndex > -1){
            int freeChunkCount = 1;
            int freeChunkIndex = reader.getForwardFreeIndex(startChunkIndex);
            while(freeChunkIndex != startChunkIndex){
                freeChunkCount++;
                freeChunkIndex = reader.getForwardFreeIndex(freeChunkIndex);
            }

            return freeChunkCount;
        }

        throw new IndexOutOfBoundsException(getUnsupportedSizeError());
    }

    @Override
    public int getFreeChunkIndex(int size, ChunkReader chunkReader, MemoryHeap heap) {
        int chosenChunkIndex = chunkReader.getMemoryLength();
        int minimumBinIndex = size / HeapUtility.CHUNK_UNIT - 1;
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

    private int getBinIndex(int chunkSize){
        return chunkSize / HeapUtility.CHUNK_UNIT - 1;
    }

    private String getUnsupportedSizeError() {
        return String.format("Chunk size is out of supported size. (%1$-%$)", HeapUtility.CHUNK_UNIT
                , HeapUtility.CHUNK_UNIT * HeapUtility.SMALL_BINS_COUNT);
    }
}
