import java.util.LinkedList;

public class PriorityQueue314<E extends Comparable<? super E>> {
    private LinkedList<E> list;
    
    public PriorityQueue314() {
        list = new LinkedList<>();
    }
    
    public boolean enqueue(E value) {
        if (value == null) {
            throw new IllegalArgumentException("The value passed in cannot be null");
        }
        
        int index = 0;
        
        while (index < list.size()) {
            E listVal = list.get(index);
            
            if (listVal.compareTo(value) > 0) {
                list.add(index, value);
                return true;
            }
            
            index++;
        }
        
        list.add(value);
        return true;
    }
    
    public E dequeue() {
        if (list.size() == 0) {
            throw new IllegalStateException("The PriorityQueue is empty");
        }
        
        E oldVal = list.get(0);
        list.remove(0);
        return oldVal;
    }
    
    public boolean isEmpty() {
        return list.size() == 0;
    }
    
    public int size() {
        return list.size();
    }
}