package com.heapsimulation;

public class ChunkReader {
    private byte[] memory;

    public ChunkReader(byte[] memory){
        this.memory = memory;
    }

    public int GetSize(int chunkIndex){
        return 0;
    }

    public int GetPrevSize(int chunkIndex){
        return 0;
    }

    public boolean IsFree(int chunkIndex){
        return false;
    }

    public int GetNextChunkIndex(int chunkIndex){
        return 0;
    }

    public int GetPrevChunkIndex(int chunkIndex){
        return 0;
    }

    public int GetBackwardFreeIndex(int chunkIndex){
        return 0;
    }

    public int GetForwardFreeIndex(int chunkIndex){
        return 0;
    }
}
