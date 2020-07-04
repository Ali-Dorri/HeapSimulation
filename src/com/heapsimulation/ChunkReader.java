package com.heapsimulation;

public class ChunkReader {
    private byte[] memory;

    public ChunkReader(byte[] memory){
        this.memory = memory;
    }

    public int getSize(int chunkIndex){
        return 0;
    }

    public int getPrevSize(int chunkIndex){
        return 0;
    }

    public boolean isFree(int chunkIndex){
        return false;
    }

    public int getNextChunkIndex(int chunkIndex){
        return 0;
    }

    public int getPrevChunkIndex(int chunkIndex){
        return 0;
    }

    public int getBackwardFreeIndex(int chunkIndex){
        return 0;
    }

    public int getForwardFreeIndex(int chunkIndex){
        return 0;
    }

    public boolean hasEnoughChunkSpace(int chunkIndex, int size){
        if(chunkIndex < memory.length){
            int prevSizeBytes = Integer.BYTES;
            int chunkSizeBytes = prevSizeBytes;
            int lastIndex = chunkIndex + prevSizeBytes + chunkSizeBytes /*+ 1 (is free flag byte) - 1 (start index)*/ + size;
            return lastIndex < memory.length;
        }
        return false;
    }
}
