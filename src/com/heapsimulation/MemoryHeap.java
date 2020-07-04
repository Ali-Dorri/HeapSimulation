package com.heapsimulation;

import com.heapsimulation.binsearching.IBinSearcher;

public class MemoryHeap {

    private final static int BIN_COUNT = 64;
    private final static int CHUNK_UNIT = 8;
    private final static int DEFAULT_HEAP_SIZE = 512;

    private byte[] memory;
    private int topIndex = 0;
    private int[] binsStartIndices;
    private IBinSearcher binSearcher;
    private ChunkReader reader;
    private ChunkWriter writer;

    public MemoryHeap(IBinSearcher searcher){
        this(DEFAULT_HEAP_SIZE, searcher);
    }

    public MemoryHeap(int size, IBinSearcher searcher){
        if(size < 0){
            throw new IndexOutOfBoundsException("Heap size can not be negative");
        }

        memory = new byte[size];
        binSearcher = searcher;
        reader = new ChunkReader(memory);
        writer = new ChunkWriter(memory);

        binsStartIndices = new int[BIN_COUNT];
        //make all bins free
        for(int i = 0; i < binsStartIndices.length; i++){
            binsStartIndices[i] = -1;
        }

        //set prev size for future first chunk
        writer.setPrevSize(0, 0);
    }

    /**
     * Allocate the requested size in heap..
     * @param size requested size
     * @return Return true if succeeded, return false otherwise.
     */
    public boolean mAlloc(int size)  {
        if(size <= 0){
            return false;
        }

        size = ceilToChunkStep(size);
        int binIndex = getBinIndex(size);
        int freeChunkIndex = -1;
        if(binIndex < binsStartIndices.length){
            freeChunkIndex = binSearcher.getFreeChunkIndex(size, binsStartIndices, reader);
        }

        if(freeChunkIndex > -1 && freeChunkIndex < memory.length){

            boolean isFree = reader.isFree(freeChunkIndex);
            int chunkSize = reader.getSize(freeChunkIndex);
            int nextChunkIndex = reader.getNextChunkIndex(freeChunkIndex);

            if(isFree && chunkSize == size && nextChunkIndex < memory.length + 1){
                allocateFreeChunk(freeChunkIndex, chunkSize);
                return true;
            }
            else{
                throw new IllegalStateException("Bin searcher returned invalid block index");
            }
        }
        else{
            //no suitable free block found, allocate from top
            boolean enoughSpace = reader.hasEnoughChunkSpace(topIndex, size);
            if(enoughSpace){
                allocateChunk(topIndex,size);
                topIndex = reader.getNextChunkIndex(topIndex);
                if(topIndex < memory.length){
                    //set prev size for future next chunk
                    writer.setPrevSize(topIndex, size);
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
        size = ceilToChunkStep(size);

        //find proper free chunk
        int memoryIndex = 0;
        while(memoryIndex < memory.length){
            int chunkSize = reader.getSize(memoryIndex);
            boolean isFree = reader.isFree(memoryIndex);
            if(!isFree && chunkSize == size){
                break;
            }

            memoryIndex = reader.getNextChunkIndex(memoryIndex);
        }

        if(memoryIndex < memory.length){
            writer.setFreeStatus(memoryIndex, true);

            //update free chunk parameters and add it to bin
            boolean adjacentsJoined = joinFreeChunksAndAddToBin(memoryIndex, size);
            if(!adjacentsJoined){
                addFreeChunkToBin(memoryIndex, size);
            }

            return true;
        }

        return false;
    }

    private void allocateFreeChunk(int freeChunkIndex, int chunkSize){
        //valid chunk index found
        int previousFreeChunk = reader.getBackwardFreeIndex(freeChunkIndex);
        int binIndex = getBinIndex(chunkSize);
        if(previousFreeChunk == freeChunkIndex){
            //remove only free chunk from bin
            binsStartIndices[binIndex] = -1;
        }
        else{
            int nextFreeChunk = reader.getForwardFreeIndex(freeChunkIndex);
            //update bin
            if(freeChunkIndex == binsStartIndices[binIndex]){
                binsStartIndices[binIndex] = nextFreeChunk;
            }
            //update pointers
            writer.setForwardFreeIndex(previousFreeChunk, nextFreeChunk);
            writer.setBackwardFreeIndex(nextFreeChunk, previousFreeChunk);
        }

        allocateChunk(freeChunkIndex, chunkSize);
    }

    private void allocateChunk(int chunkIndex, int size){
        writer.setSize(chunkIndex, size);
        writer.setFreeStatus(chunkIndex, false);
    }

    private int ceilToChunkStep(int size){
        int remain = size % CHUNK_UNIT;
        if(remain == 0){
            return size;
        }
        return size + CHUNK_UNIT - remain;
    }

    private int getBinIndex(int chunkSize){
        return chunkSize / CHUNK_UNIT - 1;
    }

    /**
     * Join the adjacent free chunks to make bigger free chunk and add it to bin if any exists, otherwise do nothing.
     * @param chunkIndex Index of the free chunk
     * @return Returns true if adjacent free chunk exists to join, otherwise return false.
     */
    private boolean joinFreeChunksAndAddToBin(int chunkIndex, int chunkSize){
        boolean isJoined = false;
        boolean isFreeChunk;
        boolean isStartBinInAdjacents = false;
        int adjacentChunkIndex = chunkIndex;
        int adjacentCandidateIndex = chunkIndex;
        int binIndex = getBinIndex(chunkSize);
        int binStartChunkIndex = binsStartIndices[binIndex];
        int adjacentCount = 0;
        int joinedChunksSize = reader.getSize(chunkIndex);

        //search previous chunks
        adjacentCandidateIndex = reader.getPrevChunkIndex(chunkIndex);
        isFreeChunk = reader.isFree(adjacentCandidateIndex);
        while(isFreeChunk && adjacentCandidateIndex > -1){
            isJoined = true;
            joinedChunksSize += reader.getSize(adjacentCandidateIndex);
            if(adjacentCandidateIndex == binStartChunkIndex){
                isStartBinInAdjacents = true;
            }
            adjacentChunkIndex = adjacentCandidateIndex;

            adjacentCandidateIndex = reader.getPrevChunkIndex(adjacentCandidateIndex);
            isFreeChunk = reader.isFree(adjacentCandidateIndex);
        }

        //join to backward free chunk
        if(adjacentChunkIndex != chunkIndex){
            int backwardIndex = reader.getBackwardFreeIndex(adjacentChunkIndex);
            writer.setForwardFreeIndex(backwardIndex, adjacentChunkIndex);
            writer.setBackwardFreeIndex(adjacentChunkIndex, backwardIndex);
            writer.setSize(adjacentChunkIndex, joinedChunksSize);
            chunkIndex = adjacentChunkIndex;
        }

        //search next chunks
        adjacentCandidateIndex = reader.getNextChunkIndex(chunkIndex);
        isFreeChunk = reader.isFree(adjacentCandidateIndex);
        while(isFreeChunk && adjacentCandidateIndex < memory.length){
            isJoined = true;
            joinedChunksSize += reader.getSize(adjacentCandidateIndex);
            if(adjacentCandidateIndex == binStartChunkIndex){
                isStartBinInAdjacents = true;
            }
            adjacentChunkIndex = adjacentCandidateIndex;

            adjacentCandidateIndex = reader.getNextChunkIndex(adjacentCandidateIndex);
            isFreeChunk = reader.isFree(adjacentCandidateIndex);
        }

        //join to forward free chunk
        if(adjacentChunkIndex != chunkIndex){
            int forwardIndex = reader.getForwardFreeIndex(adjacentChunkIndex);
            writer.setBackwardFreeIndex(forwardIndex, chunkIndex);
            writer.setForwardFreeIndex(chunkIndex, forwardIndex);
            writer.setSize(chunkIndex, joinedChunksSize);

            //update next chunk
            int nextChunkIndex = reader.getNextChunkIndex(chunkIndex);
            writer.setPrevSize(nextChunkIndex, joinedChunksSize);
        }

        //add it to bin
        if(isStartBinInAdjacents){
            binsStartIndices[binIndex] = chunkIndex;
        }

        return isJoined;
    }

    private void addFreeChunkToBin(int chunkIndex, int chunkSize){
        int binIndex = getBinIndex(chunkSize);
        if(binIndex < binsStartIndices.length){
            if(binsStartIndices[binIndex] == -1){
                //bin is empty
                binsStartIndices[binIndex] = chunkIndex;
                writer.setBackwardFreeIndex(chunkIndex, chunkIndex);
                writer.setForwardFreeIndex(chunkIndex, chunkIndex);
            }
            else{
                //find previous and next same size free chunks in corresponding bin
                int backwardIndex;
                int forwardIndex;
                int binStartChunkIndex = binsStartIndices[binIndex];
                if(binStartChunkIndex < chunkIndex){
                    //search forwardly
                    int prevFreeChunkIndex = reader.getForwardFreeIndex(binStartChunkIndex);
                    while(prevFreeChunkIndex < chunkIndex && prevFreeChunkIndex != binStartChunkIndex){
                        prevFreeChunkIndex = reader.getForwardFreeIndex(prevFreeChunkIndex);
                    }

                    forwardIndex = prevFreeChunkIndex;
                    backwardIndex = reader.getBackwardFreeIndex(prevFreeChunkIndex);
                }
                else{
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
}
