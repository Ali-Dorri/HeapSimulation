package com.heapsimulation;

public class ChunkWriter {
    private byte[] memory;

    public ChunkWriter(byte[] memory){
        this.memory = memory;
    }

    public void SetSize(int chunkIndex, int size){

    }

    public void SetPrevSize(int chunkIndex, int size){

    }

    public void SetFreeStatus(int chunkIndex, boolean isFree){

    }

    public void SetBackwardFreeIndex(int chunkIndex, int backwardIndex){

    }

    public void SetForwardFreeIndex(int chunkIndex, int forwardIndex){

    }
}
