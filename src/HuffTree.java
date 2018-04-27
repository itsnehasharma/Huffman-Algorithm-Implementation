public class HuffTree {

    private HuffNode root;

    HuffTree(char ch, int freq){
        root = new HuffNode(ch, freq);
    } //constructor for priority queue insertion

    HuffTree(HuffNode l, HuffNode r, int freq){ //constructor for actual tree insertion
        l.setCode("0");
        r.setCode("1");
        root = new HuffNode(l, r, freq);
        root.setCode("");
    }

    HuffNode root(){ return root; }

    int getWeight(){ return root.getWeight(); } //weight of the entire tree
}

class HuffNode {

    private char element;
    private int frequency;
    private HuffNode left;
    private HuffNode right;
    private boolean isLeaf;     //only leaves have char/frequency
    private String code = "";   //bit code for the nodes

    HuffNode(char ch, int freq) { //constructor for inner nodes
        element = ch;
        frequency = freq;
        isLeaf = true; }

    HuffNode(HuffNode l, HuffNode r, int freq) { //constructor for leaf nodes
        left = l;
        right = r;
        frequency = freq;
        isLeaf = false; }

    char getValue() {
        return element;
    }

    public int getWeight() {
        return frequency;
    }

    boolean isLeaf() {
        return isLeaf;
    }

    HuffNode getLeft() { return ((!isLeaf) ? left : null); }

    HuffNode getRight() { return ((!isLeaf) ? right : null); }

    String getCode() {
        return code;
    }

    void setCode(String cv) {
        this.code = cv;
    }
}

