package com.heapsimulation.binmanaging;

import com.heapsimulation.base.*;

public class NextFitManager implements IBinManager {
    private int currentChosenChunk = NO_CHUNK;
    private int startFreeChunk = NO_CHUNK;

    public NextFitManager(){

    }

    @Override
    public boolean isSupported(int chunkSize) {
        return true;
    }

    @Override
    public int getStartFreeChunkIndex(int chunkSize) {
        return startFreeChunk;
    }

    @Override
    public void setStartFreeChunkIndex(int chunkSize, int index) {
        startFreeChunk = index;
    }

    @Override
    public int getBinFreeChunkCount(int chunkSize, ChunkReader reader) {
        if(startFreeChunk > -1){
            chunkSize = HeapUtility.floorToChunkUnit(chunkSize);
            int freeChunkCount = 0;

            int foundChunkSize = reader.getRealDataSize(startFreeChunk);
            if(foundChunkSize == chunkSize){
                freeChunkCount++;
            }
            int freeChunkIndex = reader.getForwardFreeIndex(startFreeChunk);
            while(freeChunkIndex != startFreeChunk){
                foundChunkSize = reader.getRealDataSize(freeChunkIndex);
                if(foundChunkSize == chunkSize){
                    freeChunkCount++;
                }
                freeChunkIndex = reader.getForwardFreeIndex(freeChunkIndex);
            }

            return freeChunkCount;
        }

        return 0;
    }

    @Override
    public int getFreeChunkIndex(int size, ChunkReader chunkReader, MemoryHeap heap) {
        int prevChosenChunk = currentChosenChunk;
        boolean isFree = chunkReader.isFree(currentChosenChunk);
        if(!isFree){
            //fine closest next free chunk...

            //search from current index to memory top index
            currentChosenChunk = chunkReader.getNextChunkIndex(currentChosenChunk);
            isFree = chunkReader.isFree(currentChosenChunk);
            while(!isFree && currentChosenChunk < heap.getTopIndex()){
                currentChosenChunk = chunkReader.getNextChunkIndex(currentChosenChunk);
                isFree = chunkReader.isFree(currentChosenChunk);
            }

            //search from memory start index to current index
            if(!isFree){
                currentChosenChunk = 0;
                isFree = chunkReader.isFree(currentChosenChunk);
                while(!isFree && currentChosenChunk != prevChosenChunk){
                    currentChosenChunk = chunkReader.getNextChunkIndex(currentChosenChunk);
                    isFree = chunkReader.isFree(currentChosenChunk);
                }
            }
        }

        if(isFree){
            return searchFreeChunks(size, chunkReader);
        }
        else{
            //no free chunk exists
            return NO_CHUNK;
        }
    }

    private int searchFreeChunks(int size, ChunkReader reader){
        int chosenChunkIndex = NO_CHUNK;
        int prevChosenChunk = currentChosenChunk;
        int chunkSize = reader.getUnitDataSize(currentChosenChunk);
        if(chunkSize >= size){
            chosenChunkIndex = currentChosenChunk;
        }
        currentChosenChunk = reader.getForwardFreeIndex(currentChosenChunk);

        while(chosenChunkIndex == NO_CHUNK && currentChosenChunk != prevChosenChunk){
            chunkSize = reader.getUnitDataSize(currentChosenChunk);
            if(chunkSize >= size){
                chosenChunkIndex = currentChosenChunk;
            }
            currentChosenChunk = reader.getForwardFreeIndex(currentChosenChunk);
        }

        if (chosenChunkIndex != NO_CHUNK) {
            //found proper free chunk
            currentChosenChunk = chosenChunkIndex;
            return chosenChunkIndex;
        }
        else{
            //no proper free chunk found
            return NO_CHUNK;
        }
    }
}
