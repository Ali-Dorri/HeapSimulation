package com.heapsimulation;

import com.heapsimulation.bincollection.*;
import com.heapsimulation.binmanaging.*;

public class MemoryHeap {

    private final static int DEFAULT_HEAP_SIZE = 512;

    private byte[] memory;
    private int topIndex = 0;
    private IBinManager binManager;
    private ChunkReader reader;
    private ChunkWriter writer;

    public MemoryHeap(IBinManager searcher){
        this(DEFAULT_HEAP_SIZE, searcher);
    }

    public MemoryHeap(int size, IBinManager searcher){
        if(size < 0){
            throw new IndexOutOfBoundsException("Heap size can not be negative");
        }

        memory = new byte[size];
        binManager = searcher;
        reader = new ChunkReader(memory);
        writer = new ChunkWriter(memory);

        //set prev size for future first chunk
        writer.setPrevRealDataSize(0, 0);
    }

    public int getTopIndex(){
        return topIndex;
    }

    /**
     * Allocate the requested size in heap..
     * @param size requested size
     * @return Return true if succeeded, return false otherwise.
     */
    public boolean malloc(int size)  {
        if(size <= 0){
            return false;
        }

        int unitSize = HeapUtility.ceilToChunkUnit(size);
        boolean binSizeSupported = binManager.isSupported(unitSize);
        int freeChunkIndex = -1;
        if(binSizeSupported){
            freeChunkIndex = binManager.getFreeChunkIndex(unitSize, reader, this);
        }

        if(freeChunkIndex > -1 && freeChunkIndex < memory.length){
            //allocate from chosen free chunk
            boolean isFree = reader.isFree(freeChunkIndex);
            int chunkSize = reader.getUnitDataSize(freeChunkIndex);
            int nextChunkIndex = reader.getNextChunkIndex(freeChunkIndex);

            if(isFree && unitSize <= chunkSize && nextChunkIndex < memory.length + 1){
                //valid chunk index found
                allocateFreeChunk(freeChunkIndex, unitSize);
                return true;
            }
            else{
                throw new IllegalStateException("Bin searcher returned invalid block index");
            }
        }
        else{
            //no suitable free block found, allocate from top
            boolean enoughSpace = reader.hasEnoughChunkSpace(topIndex, unitSize);
            if(enoughSpace){
                allocateChunk(topIndex, unitSize);
                topIndex = reader.getNextChunkIndex(topIndex);
                if(topIndex < memory.length){
                    //set prev size for future next chunk
                    writer.setPrevRealDataSize(topIndex, unitSize);
                }
            }

            return enoughSpace;
        }
    }

    /**
     * Free the first free chunk with requested size.
     * @param size requested size
     * @return Return true if any proper chunk was found, return false otherwise.
     */
    public boolean free(int size){
        if(size <= 0 || topIndex <= 0){
            return false;
        }
        size = HeapUtility.ceilToChunkUnit(size);

        //find proper free chunk
        int memoryIndex = 0;
        while(memoryIndex < memory.length){
            int chunkSize = reader.getUnitDataSize(memoryIndex);
            boolean isFree = reader.isFree(memoryIndex);
            if(!isFree && chunkSize == size){
                break;
            }

            memoryIndex = reader.getNextChunkIndex(memoryIndex);
        }

        if(memoryIndex < memory.length){
            mergeFreeChunksAndAddToBin(memoryIndex);
            return true;
        }

        return false;
    }

    private  void allocateFreeChunk(int freeChunkIndex, int requestedUnitSize){
        removeFreeChunk(freeChunkIndex);

        //try to make remain of free chunk as free chunk
        int freeChunkSize = reader.getRealDataSize(freeChunkIndex);
        int remainSize = freeChunkSize - requestedUnitSize - ChunkReader.getMetaDataSize()/*required for new free chunk size*/;
        if(remainSize >= HeapUtility.CHUNK_UNIT){
            allocateChunk(freeChunkIndex, requestedUnitSize);
            int remainFreeChunkIndex = reader.getNextChunkIndex(freeChunkIndex);
            freeChunk(remainFreeChunkIndex, remainSize);
        }
        else{
            //allocate chunk with it's internal fragmentation (if exists)
            allocateChunk(freeChunkIndex, freeChunkSize);
        }
    }

    private void removeFreeChunk(int chunkIndex){
        int previousFreeChunk = reader.getBackwardFreeIndex(chunkIndex);
        int chunkUnitSize = reader.getUnitDataSize(chunkIndex);

        if(previousFreeChunk == chunkIndex){
            //remove the only free chunk from bin
            if(chunkIndex == binManager.getStartFreeChunkIndex(chunkUnitSize)){
                binManager.setStartFreeChunkIndex(chunkUnitSize, IBinCollection.NO_CHUNK);
            }
        }
        else{
            int nextFreeChunk = reader.getForwardFreeIndex(chunkIndex);
            //update bin
            if(chunkIndex == binManager.getStartFreeChunkIndex(chunkUnitSize)){
                binManager.setStartFreeChunkIndex(chunkUnitSize, nextFreeChunk);
            }
            //update pointers
            writer.setForwardFreeIndex(previousFreeChunk, nextFreeChunk);
            writer.setBackwardFreeIndex(nextFreeChunk, previousFreeChunk);
        }
    }

    private void allocateChunk(int chunkIndex, int size){
        writer.setFreeStatus(chunkIndex, false);
        writer.setRealDataSize(chunkIndex, size);
    }

    /**
     * Join the adjacent free chunks to make bigger free chunk and add the first or merged free chunk to bin.
     * @param chunkIndex Index of the free chunk
     */
    private void mergeFreeChunksAndAddToBin(int chunkIndex){
        boolean isFreeChunk;
        int chosenIndex = chunkIndex;
        int adjacentChunkIndex = chosenIndex;
        int adjacentCount = 0;
        int joinedChunksSize = reader.getRealDataSize(chosenIndex);

        removeFreeChunk(chosenIndex);

        //check previous chunk
        adjacentChunkIndex = reader.getPrevChunkIndex(chosenIndex);
        if(adjacentChunkIndex > -1){
            isFreeChunk = reader.isFree(adjacentChunkIndex);
            if(isFreeChunk){
                adjacentCount++;
                joinedChunksSize += reader.getRealDataSize(adjacentChunkIndex);
                removeFreeChunk(adjacentChunkIndex);
                chunkIndex = adjacentChunkIndex;
            }
        }

        //check next chunk
        adjacentChunkIndex = reader.getNextChunkIndex(chosenIndex);
        if(adjacentChunkIndex == topIndex){
            //join to top chunk
            topIndex = chunkIndex;
            if(topIndex == 0){
                //whole heap has been freed, set prev size for future first chunk
                writer.setPrevRealDataSize(0, 0);
            }
        }
        else{   //adjacentChunkIndex must be less than memory.length because it can not be more than topIndex
            isFreeChunk = reader.isFree(adjacentChunkIndex);
            if(isFreeChunk){
                adjacentCount++;
                joinedChunksSize += reader.getRealDataSize(adjacentChunkIndex);
                removeFreeChunk(adjacentChunkIndex);
            }

            //free chunk
            int mergedSize = joinedChunksSize + adjacentCount * ChunkReader.getMetaDataSize();
            freeChunk(chunkIndex, mergedSize);

            //update next chunk
            int nextChunkIndex = reader.getNextChunkIndex(chunkIndex);
            writer.setPrevRealDataSize(nextChunkIndex, mergedSize);
        }
    }

    private void freeChunk(int chunkIndex, int chunkRealSize){
        writer.setFreeStatus(chunkIndex, true);
        writer.setRealDataSize(chunkIndex, chunkRealSize);

        //add chunk to bin and link it to other free chunks
        int chunkUnitSize = HeapUtility.floorToChunkUnit(chunkRealSize);
        boolean isSizeSupported = binManager.isSupported(chunkUnitSize);
        if(isSizeSupported){
            if(binManager.getStartFreeChunkIndex(chunkUnitSize) < 0){
                //bin is empty
                binManager.setStartFreeChunkIndex(chunkUnitSize, chunkIndex);
                writer.setBackwardFreeIndex(chunkIndex, chunkIndex);
                writer.setForwardFreeIndex(chunkIndex, chunkIndex);
            }
            else{
                //find previous and next same size free chunks in corresponding bin
                int backwardIndex;
                int forwardIndex;
                int binStartChunkIndex = binManager.getStartFreeChunkIndex(chunkUnitSize);
                if(binStartChunkIndex < chunkIndex){
                    //search forwardly
                    int prevFreeChunkIndex = reader.getForwardFreeIndex(binStartChunkIndex);
                    while(prevFreeChunkIndex < chunkIndex && prevFreeChunkIndex != binStartChunkIndex){
                        prevFreeChunkIndex = reader.getForwardFreeIndex(prevFreeChunkIndex);
                    }

                    forwardIndex = prevFreeChunkIndex;
                    backwardIndex = reader.getBackwardFreeIndex(prevFreeChunkIndex);
                }
                else{   //binStartChunkIndex > chunkIndex (chunkIndex can not be equal to binStartChunkIndex because it has not been in bin)
                    //search backwardly
                    int nextFreeChunkIndex = reader.getBackwardFreeIndex(binStartChunkIndex);
                    while(nextFreeChunkIndex > chunkIndex && nextFreeChunkIndex != binStartChunkIndex){
                        nextFreeChunkIndex = reader.getBackwardFreeIndex(nextFreeChunkIndex);
                    }

                    backwardIndex = nextFreeChunkIndex;
                    forwardIndex = reader.getForwardFreeIndex(nextFreeChunkIndex);
                }

                linkFreeChunk(chunkIndex, backwardIndex, forwardIndex);
            }
        }
    }

    private void linkFreeChunk(int chunkIndex, int backwardIndex, int forwardIndex){
        writer.setForwardFreeIndex(backwardIndex, chunkIndex);
        writer.setBackwardFreeIndex(forwardIndex, chunkIndex);
        writer.setBackwardFreeIndex(chunkIndex, backwardIndex);
        writer.setForwardFreeIndex(chunkIndex, forwardIndex);
    }

    /**
     * Print first byte index of allocated chunks and top index on the end.
     */
    public void PrintAllocatedChunks(){
        boolean allocatedChunkExists = false;
        int chunkIndex = 0;
        while(chunkIndex < topIndex){
            boolean isFree = reader.isFree(chunkIndex);
            if(!isFree){
                allocatedChunkExists = true;
                System.out.print(chunkIndex + " ");
            }

            chunkIndex = reader.getNextChunkIndex(chunkIndex);
        }

        if(allocatedChunkExists){
            System.out.println(topIndex);
        }
        else{
            System.out.println("No allocated chunk exist");
        }
    }

    public void PrintBins(){
        int totalFreeChunksCount = 0;
        for(int i = 0; i < HeapUtility.SMALL_BINS_COUNT; i++){
            int chunkUnitSize = (i + 1) * HeapUtility.CHUNK_UNIT;
            int freeChunkCount = binManager.getBinFreeChunkCount(chunkUnitSize, reader);
            totalFreeChunksCount +=freeChunkCount;
            if(freeChunkCount > 0){
                String message = String.format("bin%1$d %2$d", i + 1, freeChunkCount);
                System.out.println(message);
            }
        }

        if(totalFreeChunksCount == 0){
            System.out.println("No bin exist");
        }
    }
}
