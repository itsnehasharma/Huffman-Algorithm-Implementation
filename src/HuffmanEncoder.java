import java.io.*;
import java.io.IOException;
import java.util.*;

public class HuffmanEncoder implements HuffmanCoding {

    /**the chars[] will be used throughout the getFrequency and buildTree methods in order to keep track of "CharFrequency" objects.
     the CharFrequency objects hold the value of the characters and their frequency.
     The Huff Trees take data from the CharFrequency objects; they do not store them.*/
    CharFrequency chars[] = new CharFrequency[256];

    /**This method makes use of our overarching chars[] array in order to store object that hold data of
     * chars and frequency of those chars.
     * Returns a string of the frequencies of all ascii characters that have a frequency greater than 0. */
    public String getFrequencies(File inputFile) throws FileNotFoundException {

        for (int i = 0; i < 256; i++) {
            chars[i] = new CharFrequency();
            chars[i].setElement(i);
        }

        String frequencies = ""; //to store final string of frequencies

        try {
            FileReader fr = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fr);

            int nextChar;

            while ((nextChar = br.read()) != -1) {
                chars[nextChar].plusOne(); //frequency++ every time ascii is found
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        for (int i = 0; i < chars.length; i++) {

            if (chars[i].getFrequency() == 0) { //if ascii never appears, it is not added to the final stirng
                continue;
            }

            frequencies += chars[i].getElement() + " " + chars[i].getFrequency() + "\n";
        }

        return frequencies;
    }

    /** This method takes a file as input and builds a huffman tree.
     * First we call getFrequencies(file) to get all the values for our tree.
     * We then insert all our values into a priority queue: the queue is sorted by lowest frequency at the top.
     * ***Two nodes with the same frequency value are not sorted in any particular order.***
     * After the queue is created we iterate through it, combining the frequencies of two nodes at a time to create out huffman tree.
     * After the tree is created, we call setCodes(tree) in order to iterate through the tree and attach bit codes to each of the nodes.*/
    public HuffTree buildTree(File inputFile) throws FileNotFoundException, Exception {

        HuffTree temp1, temp2, temp3 = null;

        getFrequencies(inputFile); //in order to get our values for the tree
        Comparator<HuffTree> c = new HuffComparator(); //comparing by frequency
        PriorityQueue<HuffTree> huffQueue = new PriorityQueue(256, c); //initial capacity is 256, because this is the most # of chars we can expect

        for (int i = 0; i < chars.length; i++) {
            if (chars[i].getFrequency() != 0) { //only chars whose value is not 0 are added into the queue and therefore the tree.
                HuffTree temp = new HuffTree(chars[i].getElement(), chars[i].getFrequency());
                huffQueue.offer(temp);
            }
        }

        while (huffQueue.size() > 1) { //adding two current leaf nodes one at a time into the huffman tree
            temp1 = huffQueue.poll();
            temp2 = huffQueue.poll();
            temp3 = new HuffTree(temp1.root(), temp2.root(), temp1.getWeight() + temp2.getWeight()); //taking data from "CharFrequency objects"
            huffQueue.offer(temp3);
        }

        setCodes(temp3); //method to set all the bit codes for the nodes. see setCodes.

        return temp3;
    }

    /**
     * Takes a file and a huffman tree and encodes the file.
     * Sets the codes for the huffman tree that is given for the file.
     * Generates a hashmap of the tree given in order to easily access codes for each value
     * Reads through the file given and adds each char's code to String encoded */
    public String encodeFile(File inputFile, HuffTree huffTree) throws FileNotFoundException {

        //just in case we are given a tree that was not built through this code, and doesn't previously have codes.
        //setCodes(huffTree); //see setCodes function
        char x;
        StringBuilder encoded = new StringBuilder();
        HashMap codesMap = generateHashMap(huffTree); //see generateHashMap function

        try {
            FileReader fr = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fr);

            int nextChar;

            while ((nextChar = br.read()) != -1) {
                x = (char)nextChar;
                encoded.append(codesMap.get(x)); //continuously adding the relevent codes to the final string
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        return encoded.toString();
    }

    /***
     * Take a String and HuffTree and output the decoded words.
     * First we must set codes for the huffman tree that is given. See setCodes method.
     * We then loop through String code, taking left and right turns every time we hit one of their respective numbers.
     * We stop the loop once we hit a leaf, and then add that leaf character to our final string.
     * We take the substring of the end of the character added and repeat the process until the string is completely empty. */
    public String decodeFile(String code, HuffTree huffTree) throws Exception {

        //just in case we are given a tree that was not built through this code, and doesn't previously have codes.
        //setCodes(huffTree);

        StringBuilder decoded = new StringBuilder();

        do {
            int i = 0;
            HuffNode curr = huffTree.root(); //starting point

            //empty tree
            if (curr == null) { return ""; }

            while (curr.isLeaf() == false) { //continue while we are not at a leaf node
                if (code.charAt(i) == '1') {
                    curr = curr.getRight();
                    i++; //increment our spot in the given code string
                } else if (code.charAt(i) == '0') {
                    curr = curr.getLeft();
                    i++;
                }
            }

            decoded.append(curr.getValue()); //add leaf character to the decoded string.
            code = code.substring(i, code.length()); //get rid of the bits we already went through
        } while (code.length() != 0); //until the string is empty

        return decoded.toString();
    }

    /**
     * Print the characters and their codes.
     * Creates a hashmap for all values and keys.
     * Puts values of the hashmap into a tree map so that we can print out the values in ascii order
     * Returns a string of the final traversal */
    public String traverseHuffmanTree(HuffTree huffTree) throws Exception {

        //just in case we are given a tree that was not built through this code, and doesn't previously have codes.
        //setCodes(huffTree);

        String traverse = "";

        HashMap codesMap = generateHashMap(huffTree); //set a map to easier access codes and values
        TreeMap sortedAscii = new TreeMap(); //sorted so we can print in ascii order

        sortedAscii.putAll(codesMap);
        Iterator it = sortedAscii.entrySet().iterator();

        while (it.hasNext()){ //print out all characters that have frequencies
            Map.Entry pair = (Map.Entry)it.next();
            traverse += pair.getKey() + " " + pair.getValue() + "\n";
            it.remove();
        }

        return traverse;
    }

    /***
     * Used to set the bit codes for our huffman tree.
     * Works by traversing through the tree using a stack method ensuring that every node of the tree is visited.
     * Uses the node's parent's code to set the code for the next node.*/
    public void setCodes(HuffTree huffTree){
        if (huffTree == null)
            return;

        Stack<HuffNode> stack = new Stack<>();
        HuffNode h = huffTree.root();

        //first node to be visited will be the left one
        while (h != null) {
            stack.push(h);
            if (h.getLeft() != null)
                h.getLeft().setCode(h.getCode() + "0");
            h = h.getLeft();
        }

        //traverse the tree
        while (stack.size() > 0) {

            //visit the top node
            h = stack.pop(); //continuing back up the tree visiting both right and left nodes

            if (h.getRight() != null) {
                h.getRight().setCode(h.getCode() + "1");
                h = h.getRight(); //continue down as right as possible

                while (h != null) {
                    stack.push(h);
                    if (h.getLeft() != null)
                        h.getLeft().setCode(h.getCode() + "0");
                    h = h.getLeft();
                }
            }
        }
    }

    /**
     * The object used when we calculate frequencies and build our tree.
     * Consists of two data points: char and frequency.
     * This is only used a point to store data, and after a tree is built only the data is transferred, not the actual objects.*/
    class CharFrequency {

        private int frequency;
        private char element;

        CharFrequency() {
            element = 0;
            frequency = 0;
        }

        public void setElement(int x) { element = (char)x; }

        public void plusOne() { this.frequency++; } //every time the char is found in the file, this method is called

        public int getFrequency() { return frequency; }

        public char getElement() { return this.element; }
    }

     /**
      * Used in the priority queue to build our Huffman Tree (buildTree(file))
      * Uses the frequency of the characters to compare where in the priority queue the next node goes.*/
    class HuffComparator implements Comparator<HuffTree> { //the comparator used for the priority queue

        public int compare(HuffTree x, HuffTree y) { //comparing by weight
            if (x.getWeight() < y.getWeight()) {
                return -1;
            }
            if (x.getWeight() > y.getWeight()) {
                return 1;
            }
            return 0;
        }
    }

    /***
     * Creates a hash map consisting of characters and their bit codes.
     * Used in the encodeFile and traverseHuffmanTree methods for easier access to information.
     */
    public HashMap generateHashMap (HuffTree huffTree){

        HashMap codesMap = new HashMap<Character, String>();

        try {

            Stack<HuffNode> stack = new Stack<>();
            HuffNode h = huffTree.root();

            //first node to be visited will be the left one
            while (h != null) {
                stack.push(h);
                h = h.getLeft();
            }

            //traverse the tree
            while (stack.size() > 0) {

                //visit the top node
                h = stack.pop();

                if (h.isLeaf()) {
                    codesMap.put(h.getValue(), h.getCode()); //only adds codes of leaves to the map
                }

                if (h.getRight() != null) {
                    h = h.getRight();

                    //the next node to be visited is the left most
                    while (h != null) {
                        stack.push(h);
                        h = h.getLeft();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("No tree exists - cannot hash");
        }

        return codesMap;
    }
}

