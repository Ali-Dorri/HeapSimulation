package com.heapsimulation.base;

import java.nio.ByteBuffer;

/**
 * Read information from memory chunks. Compatible with ChunkWriter.
 * Allocated Chunk Meta Data: prevSize(metaData+data size of previous chunk)(Integer bytes) +
 * size(metaData+data size of this chunk)(Integer bytes) + isFree(one byte flag)
 * Free Chunk Meta Data: allocated chunk meta data + forwardPointer(points to next free chunk of same size)(Integer bytes) +
 * backwardPointer(points to previous free chunk of same size)(Integer bytes)
 * Allocated Chunk Structure: allocated chunk meta data + data bytes
 * Free Chunk Structure: free chunk meta data + unused bytes(size of free chunk)
 * Note: pointers in free chunks simulate doubly circular linked list
 */
public class ChunkReader {
    private byte[] memory;
    private ByteBuffer intBuffer;

    public ChunkReader(byte[] memory){
        this.memory = memory;
        intBuffer = ByteBuffer.allocate(Integer.BYTES);
    }

    public static int getMetaDataSize(){
        return Integer.BYTES * 2 + 1;   //prevSize, size, isFree flag
        //for free chunks the pointers space is shared with data space so it is not included in meta data size
    }

    /**
     * Get chunk size floored to chunk unit.
     * @param chunkIndex
     * @return
     */
    public int getUnitDataSize(int chunkIndex){
        return HeapUtility.floorToChunkUnit(getRealDataSize(chunkIndex));
    }

    /**
     * Get chunk size including internal fragmentation.
     * @param chunkIndex
     * @return
     */
    public int getRealDataSize(int chunkIndex){
        CheckIndex(chunkIndex);
        int sizeIndex = chunkIndex + Integer.BYTES; //after prevSize bytes
        FillIntBuffer(sizeIndex);
        return intBuffer.getInt(0) - getMetaDataSize();
    }

    /**
     * Get previous chunk size floored to chunk unit.
     * @param chunkIndex
     * @return
     */
    public int getPrevUnitDataSize(int chunkIndex){
        return HeapUtility.floorToChunkUnit(getPrevRealDataSize(chunkIndex));
    }

    /**
     * Get previous chunk size including internal fragmentation.
     * @param chunkIndex
     * @return
     */
    public int getPrevRealDataSize(int chunkIndex){
        CheckIndex(chunkIndex);
        FillIntBuffer(chunkIndex);
        return intBuffer.getInt(0) - getMetaDataSize();
    }

    public boolean isFree(int chunkIndex){
        CheckIndex(chunkIndex);
        int flagIndex = chunkIndex + 2 * Integer.BYTES; //after prevSize and size bytes
        return memory[flagIndex] != 0;
    }

    public int getNextChunkIndex(int chunkIndex){
        CheckIndex(chunkIndex);
        int sizeIndex = chunkIndex + Integer.BYTES; //after prevSize bytes
        FillIntBuffer(sizeIndex);
        return chunkIndex + intBuffer.getInt(0);
    }

    public int getPrevChunkIndex(int chunkIndex){
        CheckIndex(chunkIndex);
        FillIntBuffer(chunkIndex);
        return chunkIndex - intBuffer.getInt(0);
    }

    public int getForwardFreeIndex(int chunkIndex){
        CheckIndex(chunkIndex);
        int forwardPointerIndex = chunkIndex + 2 * Integer.BYTES + 1;   //after prevSize, size and isFree bytes
        FillIntBuffer(forwardPointerIndex);
        return intBuffer.getInt(0);
    }

    public int getBackwardFreeIndex(int chunkIndex){
        CheckIndex(chunkIndex);
        int backwardPointerIndex = chunkIndex + 3 * Integer.BYTES + 1;  //after prevSize, size, isFree and forwardPointer bytes
        FillIntBuffer(backwardPointerIndex);
        return intBuffer.getInt(0);
    }

    public boolean hasEnoughChunkSpace(int chunkIndex, int size){
        CheckIndex(chunkIndex);
        int lastIndex = chunkIndex + getMetaDataSize() + size - 1/*(start index)*/;
        return lastIndex < memory.length;
    }

    public int getMemoryLength(){
        return memory.length;
    }

    private void CheckIndex(int chunkIndex){
        if(chunkIndex < 0 || chunkIndex > memory.length){
            String error = String.format("Chunk index %1$d must be between 0 and memory length (%2$d)", chunkIndex, memory.length);
            throw new IndexOutOfBoundsException(error);
        }
    }

    private void FillIntBuffer(int memoryIndex){
        for(int i = 0; i < Integer.BYTES; i++){
            intBuffer.put(i, memory[memoryIndex + i]);
        }
    }
}
