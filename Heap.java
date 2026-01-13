/**
 * Heap
 *
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 */
public class Heap
{
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    public HeapItem min;
    private int size = 0;
    private int numTrees = 0;
    private int numMarkedNodes = 0;
    private int totalLinks = 0;
    private int totalCuts = 0;
    private int totalHeapifyCosts = 0;
    
    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys)
    {
        this.lazyMelds = lazyMelds;
        this.lazyDecreaseKeys = lazyDecreaseKeys;
        // student code can be added here
    }

    /**
     * 
     * pre: key > 0
     *
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     *
     */
    public HeapItem insert(int key, String info) 
    {    
        return null; // should be replaced by student code
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     *
     */
    public HeapItem findMin()
    {
        return null; // should be replaced by student code
    }

    /**
     * 
     * Delete the minimal item.
     *
     */
    public void deleteMin()
    {
        return; // should be replaced by student code
    }

    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * 
     */
    public void decreaseKey(HeapItem x, int diff) {    
        if (x == null || diff < 0) return;        
        x.key -= diff;
        HeapNode node = x.node;
        HeapNode parent = node.parent;
        if (parent != null && node.item.key < parent.item.key) {
            if (!lazyDecreaseKeys) {
                heapifyUp(node);
            } else {
                cut(node, parent);
                cascadingCut(parent);
            }
        }
        if (x.key < min.key) {
            min = x;
        }
    }

    private void heapifyUp(HeapNode node) {
        while (node.parent != null && node.item.key < node.parent.item.key) {
            HeapNode parent = node.parent;            
            swapItemPointers(node, parent);
            totalHeapifyCosts++; //
            node = parent;
        }
    }

    private void swapItemPointers(HeapNode a, HeapNode b) {
        HeapItem itemA = a.item;
        HeapItem itemB = b.item;        
        a.item = itemB;
        b.item = itemA;        
        itemA.node = b;
        itemB.node = a;
    }

    private void cut(HeapNode node, HeapNode parent) {
        node.parent = null;
        parent.rank--;        
        if (node.next == node) { 
            parent.child = null;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            if (parent.child == node) { 
                parent.child = node.next;
            }
        }        
        node.next = node;
        node.prev = node;        
        if (node.marked) {
            numMarkedNodes--;
            node.marked = false;
        }        
        addToRootList(node);        
        totalCuts++;
    }

    private void cascadingCut(HeapNode node) {
        HeapNode parent = node.parent;
        if (parent != null) {
            if (!node.marked) {
                node.marked = true;
                numMarkedNodes++;
            } else {
                cut(node, parent);
                cascadingCut(parent);
            }
        }
    }

    private void addToRootList(HeapNode node) {
        if (min == null) {
            min = node.item;
        } else {
            HeapNode minNode = min.node;
            node.next = minNode.next;
            node.prev = minNode;
            minNode.next.prev = node;
            minNode.next = node;
        }
        numTrees++;
    }

    /**
     * 
     * Delete the x from the heap.
     *
     */
    public void delete(HeapItem x) 
    {    
        return; // should be replaced by student code
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2)
    {
        return; // should be replaced by student code           
    }
    
    
    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size()
    {
        return 46; // should be replaced by student code
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return 46; // should be replaced by student code
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * Class implementing a node in a Heap.
     *  
     */
    public static class HeapNode{
        public HeapItem item;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;
        public boolean marked;
        public HeapNode(HeapItem item) {
            this.item = item;
            this.next = this;
            this.prev = this;
            this.marked = false;
        }
    }
    
    /**
     * Class implementing an item in a Heap.
     *  
     */
    public static class HeapItem{
        public HeapNode node;
        public int key;
        public String info;
        public HeapItem(int key, String info) {
            this.key = key;
            this.info = info;
        }
    }
}
