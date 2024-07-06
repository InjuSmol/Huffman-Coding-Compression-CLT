public class Node {
    int symbol;
    int weight;
    Node left;
    Node right;
    Node parent;

    public Node(int symbol, int weight) {
        this.symbol = symbol;
        this.weight = weight;
        this.left = null;
        this.right = null;
        this.parent = null;
    }
}

