package com.heapsimulation;

public class ChunkReader {
    private byte[] memory;

    public ChunkReader(byte[] memory){
        this.memory = memory;
    }

    public static int getMetaDataSize(boolean isFree){
        if(isFree){
            return Integer.BYTES * 4 + 1;   //prevSize, size, isFree flag, forward pointer, backward pointer
        }
        else{
            return Integer.BYTES * 2 + 1;   //prevSize, size, isFree flag
        }
    }

    public int getMetaDataSize(int chunkIndex){
        boolean isFree = isFree(chunkIndex);
        return getMetaDataSize(isFree);
    }

    public int getDataSize(int chunkIndex){
        return 0;
    }

    public int getPrevDataSize(int chunkIndex){
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
        int lastIndex = chunkIndex + getMetaDataSize(false) + size - 1/*(start index)*/;
        return lastIndex < memory.length;
    }
}
