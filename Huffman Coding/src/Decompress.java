import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.TreeMap;

public class Decompress{
    private BitInputStream input;
    private BitOutputStream output;
    private HTree tree;
    
    public Decompress(InputStream in, OutputStream out) {
        input = new BitInputStream(in);
        output = new BitOutputStream(out);
    }
    
    //trying this again
    public int decompress(IHuffViewer viewer) throws IOException {
        int magicNumber = input.readBits(IHuffConstants.BITS_PER_INT);
        if(magicNumber != IHuffConstants.MAGIC_NUMBER) {
            viewer.showError("There is an error with reading the compressed file.");
            return -1;
        }
        int treeConst = input.readBits(IHuffConstants.BITS_PER_INT);
        tree = treeConst == IHuffConstants.STORE_COUNTS ? SCTree() : STTree();
        
        int num = decode();
        
        input.close();
        output.close();
        
        return num;
    }
    
    private int decode() throws IOException {
        int count = 0;
        HTree.Squirrel climber = tree.squirrel(); //i love squirrels!!
        
        boolean finished = false;
        
        while (!finished) {
            climber.startFromTop();
            int code = -1;
            
            while (!climber.isLeaf()) {
                int inBit = input.readBits(1);
                if (inBit == -1) {
                    throw new IOException("Unexpected EOF");
                }
                
                code = inBit == 0 ? climber.climbLeft() : climber.climbRight();
            }
            
            if (code == IHuffConstants.PSEUDO_EOF) {
                //yaay we finished
                //pop the champagne
                //lets get wings!!!
                finished = true;
            } else {
                output.writeBits(IHuffConstants.BITS_PER_WORD, code);
                count += IHuffConstants.BITS_PER_WORD;
            }
        }
        
        return count;
    }
    
    private HTree SCTree() throws IOException {
        TreeMap<Integer, Integer> bitChunks = new TreeMap<>();
        
        for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
            int numBits = input.readBits(IHuffConstants.BITS_PER_INT);
            
            if (numBits != 0) {
                bitChunks.put(i, numBits);
            }
        }
        
        //oh no! why did i spend three hours debugging
        //because i forgot to put this!!
        //no champagne :(
        //no pop no pop
        bitChunks.put(IHuffConstants.PSEUDO_EOF, 1);
        
        return new HTree(bitChunks);
    }
    
    private HTree STTree() throws IOException {
        int size = input.readBits(IHuffConstants.BITS_PER_INT);
        
        StringBuilder bitCode = new StringBuilder();
        
        for (int i = 0; i < size; i++) {
            int num = input.readBits(1);
            bitCode.append(num);
        }
        
        return new HTree(bitCode.toString());
        
    }
}