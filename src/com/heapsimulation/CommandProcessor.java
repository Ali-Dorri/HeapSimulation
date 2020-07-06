package com.heapsimulation;

import com.heapsimulation.base.MemoryHeap;
import com.heapsimulation.binmanaging.*;

import java.io.*;

public class CommandProcessor {
    private final static String INVALID_COMMAND_ERROR = "Invalid command!";
    private MemoryHeap heap;

    public void processFile(String filePath){
        if(filePath == null){
            return;
        }

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try{
            fileReader = new FileReader(filePath);
            bufferedReader = new BufferedReader(fileReader);
            String command = bufferedReader.readLine();
            while(command != null){
                processCommand(command);
                command = bufferedReader.readLine();
            }
            printHeapEndInfo();
        }
        catch(IOException exception){
            exception.printStackTrace();
        }
        finally{
            Closeable closeable;
            if(bufferedReader != null){
                closeable = bufferedReader;
            }
            else if(fileReader != null){
                closeable = fileReader;
            }
            else{
                closeable = null;
            }

            if(closeable != null){
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void processCommand(String command){
        if(command != null){
            String[] args = command.split(" ");
            if(args.length > 1){
                switch(args[0]){
                    case "setBinManager":
                        if(heap != null){
                            //previous heap commands has finished
                            printHeapEndInfo();
                        }
                        parseBinManager(args[1]);
                        break;
                    case "malloc":
                        parseAllocation(args[1]);
                        break;
                    case "free":
                        parseDeAllocation(args[1]);
                        break;
                    default:
                        System.out.println(INVALID_COMMAND_ERROR);
                }
            }
            else if(args.length == 1 && !args[0].isEmpty()){
                System.out.println(INVALID_COMMAND_ERROR);
            }
        }
    }

    private void parseBinManager(String binManagerName){
        switch(binManagerName){
            case "firstFit":
                initializeHeap(new FirstFitManager());
                break;
            case "nextFit":
                initializeHeap(new NextFitManager());
                break;
            case "worstFit":
                initializeHeap(new WorstFitManager());
                break;
            case "bestFit":
                initializeHeap(new BestFitManager());
                break;
            default:
                System.out.println("No valid bin manager! No heap has been created.");
        }
    }

    private void initializeHeap(IBinManager binManager){
        heap = new MemoryHeap(binManager);
        String message = String.format("Heap with %s as bin manager has been created", binManager.getClass().getSimpleName());
        System.out.println(message);
    }

    private void parseAllocation(String sizeArg){
        try{
            int size = Integer.parseInt(sizeArg);
            processAllocation(size);
        }
        catch(NumberFormatException exception){
            System.out.println("No valid number for allocation");
        }
    }

    private void processAllocation(int size){
        if(heap != null){
            boolean succeeded = heap.malloc(size);
            printAllocation(succeeded, size);
            heap.printAllocatedChunks();
        }
        else{
            System.out.println("No heap has been setup");
        }
    }

    private void parseDeAllocation(String sizeArg){
        try{
            int size = Integer.parseInt(sizeArg);
            processDeAllocation(size);
        }
        catch(NumberFormatException exception){
            System.out.println("No valid number for deallocation");
        }
    }

    private void processDeAllocation(int size){
        if(heap != null){
            boolean succeeded = heap.free(size);
            printDeAllocation(succeeded, size);
            heap.printBins();
        }
        else{
            System.out.println("No heap has been setup");
        }
    }

    private void printAllocation(boolean succeeded, int size){
        printHeapAction(succeeded, size, "allocation");
    }

    private void printDeAllocation(boolean succeeded, int size){
        printHeapAction(succeeded, size, "deallocation");
    }

    private void printHeapAction(boolean succeeded, int size, String action){
        String message;
        if(succeeded){
            message = String.format("Memory %d bytes %s was successful.", size, action);
        }
        else{
            message = String.format("Memory %d bytes %s failed.", size, action);
        }

        System.out.println(message);
    }

    private void printHeapEndInfo(){
        System.out.println();
        System.out.println("Print heap info at end:");
        heap.printBins();
        heap.printAllocatedChunks();
        System.out.println();
    }
}
