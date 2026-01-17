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
    public HeapItem insert(int key, String info) {   
        //create a new heap only with one item - key,info
        HeapItem new_item = new HeapItem(key, info);
        HeapNode new_node = new HeapNode(new_item);
        new_item.node = new_node;

        Heap tmp_heap = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
        tmp_heap.min = new_item;
        tmp_heap.numTrees = 1;
        tmp_heap.size = 1;
        
        //meld into the new try
        meld(tmp_heap); 
        return new_item;
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     * takes O(n) - iterated all roots
     *
     */
    public HeapItem findMin()
    {
        return this.min;
    }

    /**
     * 
     * Delete the minimal item.
     *
     */
    public void deleteMin()
    { 
        if (this.min == null){
            return;
        }
        HeapNode min_node = this.min.node;
        if (min_node.child != null){
            HeapNode child = min_node.child;
            HeapNode firstChild = child;
        
            HeapNode curr = firstChild;
            do {
                HeapNode nextVal = curr.next;
                curr.parent = null;
                addToRootList(curr);
                curr = nextVal;
            } while (curr != firstChild);
        }
         if(min_node.next == min_node){
            //clears tree if yes - deleted last node
            if (min_node.next == min_node){
                this.min = null;
                this.size = 0;
                this.numTrees = 0;
                return;
            }
        } else {
            min_node.prev.next = min_node.next;
            min_node.next.prev = min_node.prev;
            this.min = min_node.next.item;
        }
        this.size --;
        this.min.node = successiveLinking(this.min.node);
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

        //TODO - make sure that the heap is okay before, maybe difference even with no decreasekey
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
        
        //means the only child of the parent - after cut no child
        //TODO - check if need to do something different if had only one child and it was cut. maybe consider markup
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
        totalCuts++;

        Heap tmp_heap = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
        tmp_heap.min = node.item;
        tmp_heap.size = 0; //0 so want change size of tree - was already part
        tmp_heap.numTrees = 1;

        meld(tmp_heap);

    }

    private void cascadingCut(HeapNode node) {
        //TODO - make sure functionality is good - in class no other cut, only cascading, and the cut is done in the beggining of the func
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

    private void addHeapToRootList(Heap heap2){
        //edge cases - one of the heaps is empty
        if (heap2 == null || heap2.min==null){
            return;
        }

        if (this.min==null || this.min.node == null){
            this.min=heap2.min;
            return;
        }

        HeapNode minNode = this.min.node;
        HeapNode min2Node = heap2.min.node;
        HeapNode minNode_prev = minNode.prev;
        HeapNode minNode2_prev = min2Node.prev;

        //adding list2
        minNode_prev.next = min2Node;
        min2Node.prev = minNode_prev;
        minNode.prev = minNode2_prev;
        minNode2_prev.next = minNode;

        //replace min if needed
        if(heap2.min.key < this.min.key){
            this.min = heap2.min;
        }
    }

    /**
     * creates an ordered heap with only one of each size
     * @param x gets HeapItem x - min of heap
     * @return new min of ordered heap
     */
    private HeapNode successiveLinking(HeapNode x) {
        if (x==null) return null;
        HeapNode[] buckets =  new HeapNode[(int)(Math.log(Math.max(2, this.size)) / Math.log(2) + 5)];
        to_buckets(buckets, x);
        return from_buckets(buckets);
    }

    private void to_buckets(HeapNode[] buckets, HeapNode x){
        //discconnet the connected list so it would end after going all list
        if(x.prev != null){
            x.prev.next = null;
            x.prev = null;
        }
        HeapNode curr = x;
        while (curr!=null){
            HeapNode nextNode = curr.next;
            HeapNode node = curr;
            node.next = node;
            node.prev = node;
            while (buckets[node.rank] != null){
                HeapNode other = buckets[node.rank];
                buckets[node.rank] = null;
                node = link(node, other);
            }
            buckets[node.rank] = node;
            curr = nextNode;
        }
    }

    private HeapNode from_buckets(HeapNode[] buckets){
        HeapNode head = null;
        HeapNode tail = null;
        
        //reset the heap
        this.min = null;
        this.numTrees = 0;

        for (int i = 0; i < buckets.length; i++){
            if (buckets[i] != null){
                HeapNode node = buckets[i];
                this.numTrees++;

                if (head==null){
                    head = node;
                    tail = node;
                    head.next = head;
                    head.prev = head;
                    this.min = node.item;
                } else {
                    //insert B[i] to list
                    tail.next = node;
                    node.prev = tail;
                    node.next = head;
                    head.prev = node;
                    tail = node;

                    if (node.item.key < this.min.key){
                        this.min = node.item;
                    }
                }
            }
        }
        if (this.min != null){
            return this.min.node;
        } else {
            return null;
        }
    }

    /**
     * the func links two heaps with the same size, min value is root
     * @param x HeapItem x in rank z
     * @param y HeapItem y in rank z
     * @return HeapItem with the rank - x+1
     */
    private HeapNode link(HeapNode x, HeapNode y){
        // make sure that x is the min key from both
        if (x.item.key > y.item.key){
            HeapNode tmp = x;
            x = y;
            y = tmp;
        }

        y.parent = x;
        if (x.child == null){
            x.child = y;
            y.next = y;
            y.prev = y;
        } else {
            y.next = x.child;
            y.prev = x.child.prev;
            x.child.prev.next = y;
            x.child.prev = y;
        }

        x.rank++;
        this.totalLinks ++;
        this.numTrees--;
        return x;
    }


    /**
     * 
     * Delete the x from the heap.
     *
     */
    public void delete(HeapItem x) 
    {
        //changes the value of x to be the new tree min, after that - calls delete min
        // adds x to make sure no overflow from min value
        decreaseKey(x, Integer.MAX_VALUE);
        deleteMin();
        return;
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2)
    {
        addHeapToRootList(heap2);

        //TODO - make sure all these need to be simply added and not differently
        update_parameters(heap2);
        
        if (!this.lazyMelds){
            this.min.node = successiveLinking(this.min.node);
        }
        return;           
    }
    
    public void update_parameters(Heap heap2){
        this.numMarkedNodes += heap2.numMarkedNodes;
        this.totalLinks += heap2.totalLinks;
        this.totalCuts += heap2.totalCuts;
        this.totalHeapifyCosts += heap2.totalHeapifyCosts;
        this.numTrees += heap2.numTrees;
        this.size += heap2.size;
    }
    
    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size()
    {
        return this.size;
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return this.numTrees;
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes()
    {
        return this.numMarkedNodes;
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return this.totalLinks;
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return this.totalCuts;
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts()
    {
        return this.totalHeapifyCosts;
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
        //TODO - make sure rank is updated when needed
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
