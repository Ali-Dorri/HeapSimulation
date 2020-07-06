package com.heapsimulation.bincollection;

import com.heapsimulation.base.*;

public class SmallBinsArray implements IBinCollection {
    private int[] binStartIndices;

    public int[] getBinStartIndices(){
        return binStartIndices;
    }

    public SmallBinsArray(){
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

        throw new IndexOutOfBoundsException(getUnsupportedSizeError(chunkSize));
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


        if(isSupported(chunkSize)){
            int startChunkIndex = binStartIndices[binIndex];

            if(startChunkIndex > -1){
                int freeChunkCount = 1;
                int freeChunkIndex = reader.getForwardFreeIndex(startChunkIndex);
                while(freeChunkIndex != startChunkIndex){
                    freeChunkCount++;
                    freeChunkIndex = reader.getForwardFreeIndex(freeChunkIndex);
                }

                return freeChunkCount;
            }
            else{
                return 0;
            }
        }

        throw new IndexOutOfBoundsException(getUnsupportedSizeError(chunkSize));
    }

    private int getBinIndex(int chunkSize){
        return chunkSize / HeapUtility.CHUNK_UNIT - 1;
    }

    private String getUnsupportedSizeError(int chunkSize) {
        return String.format("Chunk size %1$d is out of supported size. (%2$d - %3$d)", chunkSize, HeapUtility.CHUNK_UNIT
                , HeapUtility.CHUNK_UNIT * HeapUtility.SMALL_BINS_COUNT);
    }
}
