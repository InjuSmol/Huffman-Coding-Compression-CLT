public class Main {
    public static void main(String[] args) {
        if (validArgs(args) == -1) {
            usage("program_name", 1);
            System.exit(1);
        }

        if ((globalOptions & 1) != 0) {
            usage("program_name", 0);
            System.exit(0);
        } else if ((globalOptions & (1 << 1)) != 0) {
            compress();
        } else if ((globalOptions & (1 << 2)) != 0) {
            decompress();
        }
    }

    private static int validArgs(String[] args) {
    if (args.length != 2) {
        return 0;
    }
    try {
        int option = Integer.parseInt(args[0]);
        int blockSize = Integer.parseInt(args[1]);
        globalOptions = (option << 16) | blockSize;
        return 1;
    } catch (NumberFormatException e) {
        return 0;
    }
}


    private static void usage(String programName, int returnCode) {
    System.err.println("Usage: " + programName + " [options] <blocksize>");
    System.err.println("Options:");
    System.err.println("  -c     Compress the input file");
    System.err.println("  -d     Decompress the input file");
    System.err.println("Blocksize must be an integer.");
    System.exit(returnCode);
}


    private static void compress() {
    try {
        int isEof = 0;
        while (isEof == 0) {
            int bytesRead = readBlock(isEof);
            if (bytesRead > 0) {
                huffmanTree.buildTree(currentBlock, bytesRead);
                huffmanTree.writeHeader();
                huffmanTree.compressBlock(currentBlock, bytesRead);
            }
        }
    } catch (IOException e) {
        System.err.println("Error during compression: " + e.getMessage());
    }
}


    private static void decompress() {
    try {
        huffmanTree.readHeader();
        int isEof = 0;
        while (isEof == 0) {
            int bytesRead = huffmanTree.decompressBlock(currentBlock);
            if (bytesRead > 0) {
                for (int i = 0; i < bytesRead; i++) {
                    System.out.write(currentBlock[i]);
                }
            } else {
                isEof = 1;
            }
        }
    } catch (IOException e) {
        System.err.println("Error during decompression: " + e.getMessage());
    }
}


    private static int globalOptions;
}
