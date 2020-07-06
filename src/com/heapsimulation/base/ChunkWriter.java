package com.heapsimulation.base;

import java.nio.ByteBuffer;

/**
 * Write information in memory chunks. Compatible with ChunkReader.
 * Allocated Chunk Meta Data: prevSize(metaData+data size of previous chunk)(Integer bytes) +
 * size(metaData+data size of this chunk)(Integer bytes) + isFree(one byte flag)
 * Free Chunk Meta Data: allocated chunk meta data + forwardPointer(points to next free chunk of same size)(Integer bytes) +
 * backwardPointer(points to previous free chunk of same size)(Integer bytes)
 * Allocated Chunk Structure: allocated chunk meta data + data bytes
 * Free Chunk Structure: free chunk meta data + unused bytes(size of free chunk)
 * Note: pointers in free chunks simulate doubly circular linked list
 */
public class ChunkWriter {
    private byte[] memory;
    private ByteBuffer intBuffer;

    public ChunkWriter(byte[] memory){
        this.memory = memory;
        intBuffer = ByteBuffer.allocate(Integer.BYTES);
    }

    public void setRealDataSize(int chunkIndex, int realSize){
        CheckIndex(chunkIndex);
        int sizeIndex = chunkIndex + Integer.BYTES; //after prevSize bytes
        int chunkSize = realSize + ChunkReader.getMetaDataSize();
        FillMemoryByInt(sizeIndex, chunkSize);
    }

    public void setPrevRealDataSize(int chunkIndex, int realSize){
        CheckIndex(chunkIndex);
        int prevChunkSize = realSize + ChunkReader.getMetaDataSize();
        FillMemoryByInt(chunkIndex, prevChunkSize);
    }

    public void setFreeStatus(int chunkIndex, boolean isFree){
        CheckIndex(chunkIndex);
        int flagIndex = chunkIndex + 2 * Integer.BYTES; //after prevSize and size bytes
        memory[flagIndex] = (byte)(isFree ? 1 : 0);
    }

    public void setForwardFreeIndex(int chunkIndex, int forwardIndex){
        CheckIndex(chunkIndex);
        int forwardPointerIndex = chunkIndex + 2 * Integer.BYTES + 1;   //after prevSize, size and isFree bytes
        FillMemoryByInt(forwardPointerIndex, forwardIndex);
    }

    public void setBackwardFreeIndex(int chunkIndex, int backwardIndex){
        CheckIndex(chunkIndex);
        int backwardPointerIndex = chunkIndex + 3 * Integer.BYTES + 1;  //after prevSize, size, isFree and forwardPointer bytes
        FillMemoryByInt(backwardPointerIndex, backwardIndex);
    }

    private void CheckIndex(int chunkIndex){
        if(chunkIndex < 0 || chunkIndex > memory.length){
            String error = String.format("Chunk index must be between 0 and memory length (%d)", memory.length);
            throw new IndexOutOfBoundsException(error);
        }
    }

    private void FillMemoryByInt(int memoryIndex, int value){
        intBuffer.putInt(0, value);
        for(int i = 0; i < Integer.BYTES; i++){
            memory[memoryIndex + i] = intBuffer.get(i);
        }
    }
}
