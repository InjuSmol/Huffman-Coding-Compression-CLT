import java.io.*;
import java.util.*;

public class HuffmanTree {
    private Node root;
    private Node[] nodeForSymbol = new Node[256]; // Adjust size as necessary
    private List<Node> nodes = new ArrayList<>();

    public Node getRoot() {
        return root;
    }

    public Node getNodeForSymbol(int symbol) {
        return nodeForSymbol[symbol];
    }

    public void buildTree(byte[] block, int size) {
        int[] frequency = new int[256]; // ASCII size
        for (int i = 0; i < size; i++) {
            frequency[block[i] & 0xFF]++;
        }

        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.weight));
        for (int i = 0; i < 256; i++) {
            if (frequency[i] > 0) {
                Node node = new Node(i, frequency[i]);
                nodeForSymbol[i] = node;
                queue.add(node);
            }
        }

        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            Node parent = new Node(-1, left.weight + right.weight);
            parent.left = left;
            parent.right = right;
            left.parent = parent;
            right.parent = parent;
            queue.add(parent);
        }
        root = queue.poll();
    }

    public void writeHeader() throws IOException {
        DataOutputStream out = new DataOutputStream(System.out);
        writeTree(root, out);
        out.flush();
    }

    private void writeTree(Node node, DataOutputStream out) throws IOException {
        if (node.left == null && node.right == null) {
            out.writeBoolean(true); // Leaf
            out.writeByte(node.symbol);
        } else {
            out.writeBoolean(false); // Internal node
            writeTree(node.left, out);
            writeTree(node.right, out);
        }
    }

    public int compressBlock(byte[] block, int size) throws IOException {
        int charCount = 0;
        byte currentByte = 0;
        int bitPos = 0;
        for (int i = 0; i < size; i++) {
            getCode(block[i] & 0xFF, charCount, currentByte, bitPos);
        }
        flushBits(currentByte, bitPos);
        return charCount;
    }

    public void readHeader() throws IOException {
        DataInputStream in = new DataInputStream(System.in);
        root = readTree(in);
    }

    private Node readTree(DataInputStream in) throws IOException {
        boolean isLeaf = in.readBoolean();
        if (isLeaf) {
            int symbol = in.readByte();
            return new Node(symbol, 0);
        } else {
            Node left = readTree(in);
            Node right = readTree(in);
            Node parent = new Node(-1, left.weight + right.weight);
            parent.left = left;
            parent.right = right;
            left.parent = parent;
            right.parent = parent;
            return parent;
        }
    }

    public int decompressBlock(byte[] block) throws IOException {
        int count = 0;
        Node currentNode = root;
        while (true) {
            int bit = readBit();
            if (bit == -1) {
                return count;
            }
            currentNode = bit == 0 ? currentNode.left : currentNode.right;
            if (currentNode.left == null && currentNode.right == null) {
                if (currentNode.symbol == 256) { // EOF symbol
                    return count;
                }
                block[count++] = (byte) currentNode.symbol;
                currentNode = root;
            }
        }
    }

    public void resetNodeWeights() {
        resetNodeWeights(root);
    }

    private void resetNodeWeights(Node node) {
        if (node != null) {
            node.weight = 0;
            resetNodeWeights(node.left);
            resetNodeWeights(node.right);
        }
    }

    private void getCode(int symbol, int charCounter, byte currentByte, int bitPos) throws IOException {
        Node currentNode = getNodeForSymbol(symbol);
        Stack<Integer> bits = new Stack<>();
        while (currentNode.parent != null) {
            if (currentNode == currentNode.parent.left) {
                bits.push(0);
            } else {
                bits.push(1);
            }
            currentNode = currentNode.parent;
        }
        while (!bits.isEmpty()) {
            int bit = bits.pop();
            if (bitPos == 8) {
                System.out.write(currentByte);
                currentByte = 0;
                bitPos = 0;
                charCounter++;
            }
            if (bit == 1) {
                currentByte |= (1 << (7 - bitPos));
            }
            bitPos++;
        }
    }

    private void flushBits(byte currentByte, int bitPos) throws IOException {
        if (bitPos > 0) {
            System.out.write(currentByte);
        }
    }

    private int readBit() throws IOException {
        int currentByte = System.in.read();
        if (currentByte == -1) {
            return -1;
        }
        int bitPos = 0;
        return (currentByte >> (7 - bitPos)) & 1;
    }
}

