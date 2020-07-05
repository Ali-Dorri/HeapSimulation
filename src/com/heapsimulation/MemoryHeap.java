package com.heapsimulation;

import com.heapsimulation.binsearching.IBinSearcher;

public class MemoryHeap {

    private final static int BIN_COUNT = 64;
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
        writer.setPrevRealDataSize(0, 0);
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

        int unitSize = HeapUtility.ceilToChunkUnit(size);
        int binIndex = getBinIndex(unitSize);
        int freeChunkIndex = -1;
        if(binIndex < binsStartIndices.length){
            freeChunkIndex = binSearcher.getFreeChunkIndex(unitSize, binsStartIndices, reader);
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
            mergeFreeChunksAndAddToBin(memoryIndex, size);
            return true;
        }

        return false;
    }

    private void allocateFreeChunk(int freeChunkIndex, int requestedUnitSize){
        removeFreeChunk(freeChunkIndex);
        allocateChunk(freeChunkIndex, requestedUnitSize);

        //make remain of free chunk as free chunk
        int freeChunkSize = reader.getRealDataSize(freeChunkIndex);
        int remainSize = freeChunkSize - requestedUnitSize - ChunkReader.getMetaDataSize(false)/*required for new free chunk size
        , pointer space come from previous allocated free chunk so we need allocated meta data size*/;
        if(remainSize >= HeapUtility.CHUNK_UNIT){
            int remainFreeChunkIndex = reader.getNextChunkIndex(freeChunkIndex);
            freeChunk(remainFreeChunkIndex, remainSize, freeChunkSize);
        }
    }

    private void removeFreeChunk(int chunkIndex){
        int previousFreeChunk = reader.getBackwardFreeIndex(chunkIndex);
        int unitChunkSize = reader.getUnitDataSize(chunkIndex);
        int binIndex = getBinIndex(unitChunkSize);
        if(previousFreeChunk == chunkIndex){
            //remove the only free chunk from bin
            binsStartIndices[binIndex] = -1;
        }
        else{
            int nextFreeChunk = reader.getForwardFreeIndex(chunkIndex);
            //update bin
            if(chunkIndex == binsStartIndices[binIndex]){
                binsStartIndices[binIndex] = nextFreeChunk;
            }
            //update pointers
            writer.setForwardFreeIndex(previousFreeChunk, nextFreeChunk);
            writer.setBackwardFreeIndex(nextFreeChunk, previousFreeChunk);
        }
    }

    private void allocateChunk(int chunkIndex, int size){
        writer.setRealDataSize(chunkIndex, size);
        writer.setFreeStatus(chunkIndex, false);
    }

    private int getBinIndex(int unitChunkSize){
        return unitChunkSize / HeapUtility.CHUNK_UNIT - 1;
    }

    /**
     * Join the adjacent free chunks to make bigger free chunk and add the first or merged free chunk to bin.
     * @param chunkIndex Index of the free chunk
     */
    private void mergeFreeChunksAndAddToBin(int chunkIndex, int chunkSize){
        boolean isFreeChunk;
        int chosenIndex = chunkIndex;
        int adjacentChunkIndex = chosenIndex;
        int adjacentCount = 0;
        int joinedChunksSize = reader.getUnitDataSize(chosenIndex);

        removeFreeChunk(chosenIndex);

        //check previous chunk
        adjacentChunkIndex = reader.getPrevChunkIndex(chosenIndex);
        isFreeChunk = reader.isFree(adjacentChunkIndex);
        if(isFreeChunk && adjacentChunkIndex > -1){
            adjacentCount++;
            joinedChunksSize += reader.getRealDataSize(adjacentChunkIndex);
            removeFreeChunk(adjacentChunkIndex);
            chunkIndex = adjacentChunkIndex;
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
            int mergedSize = chunkSize + joinedChunksSize + adjacentCount * ChunkReader.getMetaDataSize(true);
            int prevRealChunkSize = reader.getPrevRealDataSize(chunkIndex);
            freeChunk(chunkIndex, mergedSize, prevRealChunkSize);

            //update next chunk
            int nextChunkIndex = reader.getNextChunkIndex(chunkIndex);
            writer.setPrevRealDataSize(nextChunkIndex, mergedSize);


        }
    }

    private void freeChunk(int chunkIndex, int chunkRealSize, int prevChunkRealSize){
        writer.setFreeStatus(chunkIndex, true);
        writer.setRealDataSize(chunkIndex, chunkRealSize);
        writer.setPrevRealDataSize(chunkIndex, prevChunkRealSize);

        //add chunk to bin and link it to other free chunks
        int chunkUnitSize = HeapUtility.floorToChunkUnit(chunkRealSize);
        int binIndex = getBinIndex(chunkUnitSize);
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

    public void PrintAllocatedChunks(){
        int chunkIndex = 0;
        while(chunkIndex < topIndex){
            boolean isFree = reader.isFree(chunkIndex);
            if(isFree){
                System.out.print(chunkIndex + " ");
            }

            chunkIndex = reader.getNextChunkIndex(chunkIndex);
        }

        System.out.println();
    }

    public void PrintBins(){
        for(int i = 0; i < binsStartIndices.length; i++){
            if(binsStartIndices[i] > -1){
                int freeChunkCount = 1;
                int initialChunkIndex = binsStartIndices[i];
                int freeChunkIndex = reader.getForwardFreeIndex(initialChunkIndex);
                while(freeChunkIndex != initialChunkIndex){
                    freeChunkCount++;
                    freeChunkIndex = reader.getForwardFreeIndex(freeChunkIndex);
                }

                String message = String.format("bin%1$ %2$", i + 1, freeChunkCount);
                System.out.println(message);
            }
        }
    }
}
