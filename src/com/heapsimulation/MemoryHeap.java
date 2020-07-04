package com.heapsimulation;

import com.heapsimulation.binsearching.IBinSearcher;

public class MemoryHeap {

    private final static int BIN_COUNT = 64;
    private final static int BIN_DIFF = 8;
    private final static int DEFAULT_HEAP_SIZE = 512;

    private byte[] memory;
    private int[] binsStartIndices = new int[BIN_COUNT];
    private IBinSearcher binSearcher;
    private ChunkReader reader;
    private ChunkWriter writer;

    public MemoryHeap(IBinSearcher searcher){
        memory = new byte[DEFAULT_HEAP_SIZE];
        binSearcher = searcher;
        reader = new ChunkReader(memory);
        writer = new ChunkWriter(memory);
    }

    public MemoryHeap(int size, IBinSearcher searcher){
        if(size < 0){
            throw new IndexOutOfBoundsException("Heap size can not be negative");
        }

        memory = new byte[size];
        binSearcher = searcher;
        reader = new ChunkReader(memory);
        writer = new ChunkWriter(memory);
    }

    public void mAlloc(int size){
        int freeBlockIndex = binSearcher.GetFreeBlockIndex(size, binsStartIndices, reader);
        if(freeBlockIndex > -1 && freeBlockIndex < memory.length){
            //allocate the free block, remove it from bins

        }
        else{
            //no suitable frre block found, allocate from top

        }
    }

    public void free(int size){

    }
}
