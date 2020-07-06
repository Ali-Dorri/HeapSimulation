package com.heapsimulation.base;

public final class HeapUtility {
    public final static int CHUNK_UNIT = 8;
    public final static int SMALL_BINS_COUNT = 64;

    public static int ceilToChunkUnit(int size){
        int remain = size % CHUNK_UNIT;
        if(remain == 0){
            return size;
        }
        return size + CHUNK_UNIT - remain;
    }

    public static int floorToChunkUnit(int size){
        int remain = size % CHUNK_UNIT;
        return size - remain;
    }
}
