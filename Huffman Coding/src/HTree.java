import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HTree{
    private TreeNode root;
    private int numLeaves;
    private int numInternalNodes;
    private String bitCode;
    
    
    public HTree(Map<Integer, Integer> bitChunks) {
        PriorityQueue314<TreeNode> pq = new PriorityQueue314<>();
        
        for (Integer i : bitChunks.keySet()) {
            pq.enqueue(new TreeNode(i, bitChunks.get(i)));
        }
        
        makeTree(pq);
    }
    
    public void makeTree(PriorityQueue314<TreeNode> pq) {
        //will be equal to number of leaves in pq
        numLeaves = pq.size();
        //needs to be at least two nodes
        while (pq.size() >= 2) {
            numInternalNodes++;
            TreeNode left = pq.dequeue();
            TreeNode right = pq.dequeue();
            
            pq.enqueue(new TreeNode(left, left.getFrequency() + 
                    right.getFrequency(), right));
        }
        
        root = pq.dequeue();
    }
    
    public HTree(String bitCod) {
        bitCode = bitCod;
        root = makeTree();
    }
    
    private TreeNode makeTree() {
        if (bitCode.charAt(0) == '0') {
            bitCode = bitCode.substring(1);
            TreeNode left = makeTree();
            TreeNode right = makeTree();
            return new TreeNode(left, 0, right);
        } else {
            //read next 9
            int code = Integer.parseInt(bitCode.substring(1, IHuffConstants.BITS_PER_WORD + 2), 2);
            bitCode = bitCode.substring(IHuffConstants.BITS_PER_WORD + 2);
            return new TreeNode(code, 0);
        }
    }
    
    public Map<Integer, String> getMap(){
        Map<Integer, String> key = new HashMap<>();
        getMapHelper("", key, root);
        return key;
    }
    
    private void getMapHelper(String current, Map<Integer, String> key, TreeNode node){
        if (!node.isLeaf()) {
            if (node.getRight() != null) {
                getMapHelper(current + "1", key, node.getRight());
            }
            
            if (node.getLeft() != null) {
                getMapHelper(current + "0", key, node.getLeft());
            }
        } else {
            key.put(node.getValue(), current);
        }
    }
    
    public int numLeaves() {
        return numLeaves;
    }
    
    public int numInternalNodes() {
        return numInternalNodes;
    }
    
    public String preorder() {
        String current = "";
        
        return preorderHelper(root, current);
    }
    
    private String preorderHelper(TreeNode root, String current) {
        if (root.isLeaf()) {
            current = current + "1";
            String binaryString = Integer.toBinaryString(root.getValue());
            
            int size = binaryString.length();
            
            for (int i = 0; i < IHuffConstants.BITS_PER_WORD + 1 - size; i++) {
                binaryString = "0" + binaryString;
            }
            
            current = current + binaryString;
        } else {
            current = current + "0";
            if (root.getLeft() != null) {
                current = preorderHelper(root.getLeft(), current);
            }
            
            if (root.getRight() != null) {
                current = preorderHelper(root.getRight(), current);
            }
        }
        
        return current;
    }
    
    public Squirrel squirrel(){
        return new Squirrel();
    }
    
    public class Squirrel{
        private TreeNode node;
        
        public Squirrel() {
            startFromTop();
        }
        
        public boolean isLeaf() {
            return node.isLeaf();
        }
        
        public int climbLeft() {
            if (node.isLeaf()) {
                return -1;
            }
            
            node = node.getLeft();
            
            return node.isLeaf() ? node.getValue() : -1;
        }
        
        public int climbRight() {
            if (node.isLeaf()) {
                return -1;
            }
            
            node = node.getRight();
            
            return node.isLeaf() ? node.getValue() : -1;
        }
        
        //so we do not need to make a new object each time
        public void startFromTop() {
            node = root;
        }
    }
}