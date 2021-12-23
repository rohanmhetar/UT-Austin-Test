import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

//class for compressing files
public class Compress {
    private BitInputStream input;
    private BitOutputStream output;
    private TreeMap<Integer, Integer> bitChunks;
    private Map<Integer, String> key;
    private HTree tree;
    private int size;
    private int compressedSize;
    private int formatter;
    
    public Compress(InputStream in, int header) {
        formatter = header;
        input = new BitInputStream(in);
    }
    
    public int preprocesscompress() throws IOException{
        int numBits = input.readBits(IHuffConstants.BITS_PER_WORD);
        bitChunks = new TreeMap<>();
        
        while (numBits != -1) {
            size += IHuffConstants.BITS_PER_WORD;
            
            if (bitChunks.containsKey(numBits)) {
                bitChunks.put(numBits, bitChunks.get(numBits) + 1);
            } else {
                bitChunks.put(numBits, 1);
            }
            
            numBits = input.readBits(IHuffConstants.BITS_PER_WORD);
        }
        
        bitChunks.put(IHuffConstants.PSEUDO_EOF, 1);
        
        tree = new HTree(bitChunks);
        
        key = tree.getMap();
        
        compressedSize += IHuffConstants.BITS_PER_INT * 2;
        
        if (formatter == IHuffConstants.STORE_COUNTS) {
            compressedSize += IHuffConstants.ALPH_SIZE * IHuffConstants.BITS_PER_INT;
        }
        
        else {
            compressedSize += IHuffConstants.BITS_PER_INT;
            int sizeToBeAdded = IHuffConstants.BITS_PER_WORD * tree.numLeaves() + 
                    tree.numLeaves() + (tree.numLeaves() + tree.numInternalNodes());
            compressedSize += sizeToBeAdded;
        }
        
        for (Integer i : bitChunks.keySet()) {
            compressedSize += key.get(i).length() * bitChunks.get(i);
        }
        
        //compressedSize += key.get(IHuffConstants.PSEUDO_EOF).length();
        
        input.close();
        
        return size - compressedSize;
    }
    
//    private void setCompressedSize() {
//        compressedSize += IHuffConstants.BITS_PER_INT * 2;
//        
//        compressedSize += IHuffConstants.BITS_PER_INT;
//        
//        compressedSize += IHuffConstants.BITS_PER_INT * tree.numLeaves();
//        compressedSize += tree.numInternalNodes();
//        
//        for (Integer i : bitChunks.keySet()) {
//            compressedSize += key.get(i).length() * bitChunks.get(i);
//        }
//    }
    
    public int compress(InputStream in, OutputStream out) throws IOException {
        input = new BitInputStream(in);
        output = new BitOutputStream(out);
        
        //need to write out the magic number
        output.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
        
        if (formatter != IHuffConstants.STORE_COUNTS) {
            output.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.STORE_TREE);
            output.writeBits(IHuffConstants.BITS_PER_INT, 
                    (IHuffConstants.BITS_PER_WORD + 1) * tree.numLeaves() + tree.numLeaves() + tree.numInternalNodes());
            write(tree.preorder());
        } else {
            output.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.STORE_COUNTS);
            for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {;
                output.writeBits(IHuffConstants.BITS_PER_INT, bitChunks.containsKey(i) ? bitChunks.get(i) : 0);
            }
        }
        
        int bits = input.readBits(IHuffConstants.BITS_PER_WORD);
        
        while (bits != -1) {
            write(key.get(bits));
            bits = input.readBits(IHuffConstants.BITS_PER_WORD);
        }
        
        //take care of end of file
        
        write(key.get(IHuffConstants.PSEUDO_EOF));
        
        output.flush();
        input.close();
        output.close();
        
        return compressedSize;
    }
    
    public int getCompressedSize() {
        return compressedSize;
    }
    
    public int size() {
        return size;
    }
    
    private void write(String word) {
        for (int i = 0; i < word.length(); i++) {
            output.writeBits(1, word.charAt(i) == '0' ? 0 : 1);
        }
    }
}