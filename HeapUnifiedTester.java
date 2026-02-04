import java.util.*;

public class HeapUnifiedTester {

    // =========================================================
    // =============== MULTISET GROUND TRUTH ===================
    // =========================================================

    private static void multisetAdd(TreeMap<Integer, Integer> ms, int key) {
        ms.put(key, ms.getOrDefault(key, 0) + 1);
    }

    private static void multisetRemoveOne(TreeMap<Integer, Integer> ms, int key) {
        Integer c = ms.get(key);
        if (c == null) throw new AssertionError("GroundTruth missing key=" + key);
        if (c == 1) ms.remove(key);
        else ms.put(key, c - 1);
    }

    private static int multisetSize(TreeMap<Integer, Integer> ms) {
        int s = 0;
        for (int c : ms.values()) s += c;
        return s;
    }

    // =========================================================
    // =============== STRUCTURAL VALIDATION ===================
    // =========================================================

    /**
     * Validates heap structure after a PUBLIC operation returned.
     * Assumes heap should be consistent at method boundaries.
     */
    public static void validateHeapStructure(Heap h) {
        if (h.size() == 0) {
            assert h.min == null : "Empty heap must have null min";
            assert h.firstRoot == null : "Empty heap must have null firstRoot";
            assert h.numTrees() == 0 : "Empty heap must have 0 trees";
            assert h.numMarkedNodes() == 0 : "Empty heap must have 0 marked";
            return;
        }

        assert h.firstRoot != null : "Non-empty heap must have non-null firstRoot";
        assert h.min != null : "Non-empty heap must have non-null min";
        assert h.min.node != null : "Min item must point to a node";

        Set<Heap.HeapNode> visited = new HashSet<>();
        int actualSize = 0;
        int actualTrees = 0;
        int actualMarked = 0;

        Heap.HeapNode start = h.firstRoot;
        Heap.HeapNode curr = start;

        // root list must be circular and non-null pointers
        do {
            assert curr != null : "Root list reached null (broken circular list)";
            assert curr.item != null : "A node in root list has null item";
            assert curr.item.node == curr : "Item<->Node backpointer mismatch at root key=" + curr.item.key;

            actualTrees++;
            actualSize += validateNodeRecursive(curr, null, visited);
            if (curr.marked) actualMarked++;

            assert curr.parent == null : "Root has non-null parent, key=" + curr.item.key;
            assert curr.next != null && curr.prev != null : "Root has null next/prev, key=" + curr.item.key;
            assert curr.next.prev == curr : "Broken next.prev at root key=" + curr.item.key;
            assert curr.prev.next == curr : "Broken prev.next at root key=" + curr.item.key;

            curr = curr.next;
        } while (curr != start);

        assert actualSize == h.size() :
                "Size mismatch! expected=" + h.size() + ", actual=" + actualSize;

        assert actualTrees == h.numTrees() :
                "Tree count mismatch! expected=" + h.numTrees() + ", actual=" + actualTrees;

        assert actualMarked == h.numMarkedNodes() :
                "Marked count mismatch! expected=" + h.numMarkedNodes() + ", actual=" + actualMarked;

        // validate min is indeed minimal among visited nodes
        for (Heap.HeapNode n : visited) {
            assert h.min.key <= n.item.key :
                    "Found key smaller than min! node=" + n.item.key + ", min=" + h.min.key;
        }
    }

    private static int validateNodeRecursive(Heap.HeapNode node,
                                             Heap.HeapNode parent,
                                             Set<Heap.HeapNode> visited) {
        assert node != null : "validateNodeRecursive got null node";
        assert visited.add(node) : "Cycle detected or shared node between trees, key=" + node.item.key;

        assert node.item != null : "Node has null item";
        assert node.item.node == node : "Item<->Node backpointer mismatch at key=" + node.item.key;

        assert node.parent == parent :
                "Parent pointer mismatch at key=" + node.item.key;

        if (parent != null) {
            assert node.item.key >= parent.item.key :
                    "Heap property violated! child=" + node.item.key + " < parent=" + parent.item.key;
        }

        int count = 1;

        if (node.child != null) {
            Heap.HeapNode start = node.child;
            Heap.HeapNode c = start;
            int childCount = 0;

            do {
                assert c != null : "Child list reached null at parent key=" + node.item.key;
                assert c.next != null && c.prev != null : "Child has null next/prev, key=" + c.item.key;
                assert c.next.prev == c : "Broken next.prev in child list, key=" + c.item.key;
                assert c.prev.next == c : "Broken prev.next in child list, key=" + c.item.key;

                childCount++;
                count += validateNodeRecursive(c, node, visited);
                c = c.next;
            } while (c != start);

            assert childCount == node.rank :
                    "Rank mismatch at key=" + node.item.key + " | rank=" + node.rank + ", children=" + childCount;
        } else {
            assert node.rank == 0 : "Node with no children must have rank 0, key=" + node.item.key;
        }

        return count;
    }

    // =========================================================
    // =================== BASIC TESTS =========================
    // =========================================================

    public static void testFourConfigurations() {
        boolean[][] configs = {{true, true}, {true, false}, {false, true}, {false, false}};
        String[] labels = {"Fibonacci", "Lazy Binomial", "Binomial w/ Cuts", "Strict Binomial"};

        for (int i = 0; i < 4; i++) {
            System.out.println("Testing Configuration: " + labels[i]);
            Heap h = new Heap(configs[i][0], configs[i][1]);

            Heap.HeapItem[] items = new Heap.HeapItem[100];
            for (int j = 0; j < 100; j++)
                items[j] = h.insert(j + 100, "v" + j);

            validateHeapStructure(h);

            h.deleteMin();
            validateHeapStructure(h);

            h.decreaseKey(items[50], 80);
            validateHeapStructure(h);

            h.delete(items[20]);
            validateHeapStructure(h);

            System.out.println("  - Status: OK");
        }
    }

    public static void testEdgeCases() {
        System.out.println("Testing Edge Cases...");

        // Meld empty heaps
        Heap h1 = new Heap(true, true);
        Heap h2 = new Heap(true, true);
        h1.meld(h2);
        assert h1.size() == 0;
        validateHeapStructure(h1);

        // Delete only node
        h1.insert(5, "only");
        validateHeapStructure(h1);
        h1.deleteMin();
        assert h1.size() == 0;
        validateHeapStructure(h1);

        // DecreaseKey to same value
        Heap h3 = new Heap(false, false);
        Heap.HeapItem n = h3.insert(10, "x");
        h3.decreaseKey(n, 0);
        assert n.key == 10;
        validateHeapStructure(h3);

        // Massive successive linking scenario (strict binomial)
        Heap h4 = new Heap(false, false);
        for (int i = 0; i < 1024; i++) h4.insert(i, "");
        assert h4.numTrees() <= 12;
        validateHeapStructure(h4);

        System.out.println("Edge Cases: OK");
    }

    // =========================================================
    // ==================== CHAOS MONKEY =======================
    // =========================================================

    public static void runChaosMonkey(boolean lazyM, boolean lazyD) {
        System.out.println("Chaos Monkey (lm=" + lazyM + ", ld=" + lazyD + ")");
        Heap h = new Heap(lazyM, lazyD);

        TreeMap<Integer, Integer> truth = new TreeMap<>();
        List<Heap.HeapItem> active = new ArrayList<>();
        Random rnd = new Random(12345);

        for (int i = 0; i < 5000; i++) {
            int op = rnd.nextInt(5);

            if (op == 0) { // Insert
                int v = rnd.nextInt(10000) + 1;
                Heap.HeapItem it = h.insert(v, "i" + v);
                active.add(it);
                multisetAdd(truth, v);

            } else if (op == 1) { // DeleteMin
                if (h.size() > 0) {
                    Heap.HeapItem m = h.findMin(); // capture exact item that should be removed
                    int mk = m.key;

                    h.deleteMin();

                    // update truth + active using captured item
                    multisetRemoveOne(truth, mk);
                    active.remove(m);
                }

            } else if (op == 2) { // DecreaseKey
                if (!active.isEmpty()) {
                    // pick a still-active item
                    int idx = rnd.nextInt(active.size());
                    Heap.HeapItem it = active.get(idx);

                    // it should still be in heap; if not, it's a tester bookkeeping bug
                    assert it.node != null : "Active item has null node (tester bookkeeping bug)";

                    int oldKey = it.key;
                    int diff = rnd.nextInt(Math.max(1, oldKey)); // 0..oldKey-1 (legal)
                    h.decreaseKey(it, diff);
                    int newKey = it.key;

                    multisetRemoveOne(truth, oldKey);
                    multisetAdd(truth, newKey);
                }

            } else if (op == 3) { // Delete arbitrary
                if (!active.isEmpty()) {
                    int idx = rnd.nextInt(active.size());
                    Heap.HeapItem it = active.remove(idx);

                    assert it.node != null : "Active item has null node (tester bookkeeping bug)";
                    int k = it.key;

                    h.delete(it);
                    multisetRemoveOne(truth, k);
                }

            } else { // FindMin + size checks
                if (h.size() == 0) {
                    assert truth.isEmpty() : "Heap empty but truth not empty";
                    assert h.findMin() == null : "Heap empty but findMin not null";
                } else {
                    assert !truth.isEmpty() : "Heap non-empty but truth empty";
                    assert h.findMin() != null : "Heap non-empty but findMin null";
                    assert h.findMin().key == truth.firstKey() :
                            "Min mismatch! heapMin=" + h.findMin().key + ", truthMin=" + truth.firstKey();
                }

                assert h.size() == multisetSize(truth) :
                        "Size mismatch! heap=" + h.size() + ", truth=" + multisetSize(truth);
            }

            // periodic deep validation (heap should be consistent after each op)
            if (i % 250 == 0) {
                validateHeapStructure(h);
            }
        }

        validateHeapStructure(h);
        System.out.println("Chaos Monkey finished OK");
    }

    // =========================================================
    // ======================= MAIN ============================
    // =========================================================

    public static void main(String[] args) {
        try {
            testFourConfigurations();
            testEdgeCases();
            runChaosMonkey(true, true);
            runChaosMonkey(false, false);
            System.out.println("\n>>> ALL UNIFIED TESTS PASSED! <<<");
        } catch (AssertionError e) {
            System.err.println("\n!!! TEST FAILED !!!");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
