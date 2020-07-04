package com.heapsimulation;

public class ChunkWriter {
    private byte[] memory;

    public ChunkWriter(byte[] memory){
        this.memory = memory;
    }

    public void setSize(int chunkIndex, int size){

    }

    public void setPrevSize(int chunkIndex, int size){

    }

    public void setFreeStatus(int chunkIndex, boolean isFree){

    }

    public void setBackwardFreeIndex(int chunkIndex, int backwardIndex){

    }

    public void setForwardFreeIndex(int chunkIndex, int forwardIndex){

    }
}
