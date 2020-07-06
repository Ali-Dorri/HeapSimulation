package com.heapsimulation.bincollection;

import com.heapsimulation.*;

public interface IBinCollection {
    public static final int NO_CHUNK = -1;

    boolean isSupported(int chunkSize);
    int getStartFreeChunkIndex(int chunkSize);
    void setStartFreeChunkIndex(int chunkSize, int index);
    int getBinFreeChunkCount(int chunkSize, ChunkReader reader);
}
