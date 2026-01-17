public class HeapTestRunner {

    public static void main(String[] args) {
        // Check assertions enabled
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (!assertsEnabled) {
            System.err.println("WARNING: Assertions are not enabled. Run with '-ea' flag to see test failures.");
        }

        try {
            test1_BasicInsertAndMin();
            test2_SimpleDeleteMin();
            test3_SuccessiveLinkingLogic();
            test4_NonLazyMeld();
            test5_LazyDecreaseKey_CutHappens();
            test6_NonLazyDecreaseKey_HeapifyUpHappens();
            test7_DeleteSpecificItem();
            test8_MeldWithEmptyHeap();
            test9_LargeScaleInsertAndDeleteMin();
            test10_EmptyHeapEdgeCases();
            test11_MarkedNodesMustBeZeroWhenNonLazyDecrease();

            System.out.println("\nAll tests completed successfully!");
        } catch (AssertionError e) {
            System.err.println("\nTest failed: " + e.getMessage());
            throw e;
        }
    }

    /** 1. Basic Insert: Check size, numTrees, and min pointer */
    private static void test1_BasicInsertAndMin() {
        System.out.println("Test 1: Basic Insertions...");
        Heap h = new Heap(true, true);
        h.insert(10, "ten");
        h.insert(5, "five");
        h.insert(20, "twenty");

        assert h.size() == 3 : "Size should be 3";
        assert h.numTrees() == 3 : "NumTrees should be 3 (lazy melds => no linking on insert)";
        assert h.findMin() != null && h.findMin().key == 5 : "Min should be 5";
    }

    /** 2. Simple deleteMin: single tree becomes empty */
    private static void test2_SimpleDeleteMin() {
        System.out.println("Test 2: DeleteMin until empty...");
        Heap h = new Heap(true, true);
        h.insert(10, "ten");
        h.deleteMin();

        assert h.size() == 0 : "Size should be 0";
        assert h.findMin() == null : "Min should be null";
        assert h.numTrees() == 0 : "NumTrees should be 0";
    }

    /** 3. Successive Linking: Ensure roots consolidate after deleteMin */
    private static void test3_SuccessiveLinkingLogic() {
        System.out.println("Test 3: Successive Linking (Consolidation)...");
        Heap h = new Heap(true, true);
        for (int i = 1; i <= 8; i++) h.insert(i, "");

        // Before deleteMin: 8 trees (lazy melds)
        h.deleteMin(); // removes 1; consolidation is always done in deleteMin

        // 7 in binary is 111 => should be 3 trees (ranks 0,1,2) in a binomial decomposition
        assert h.size() == 7 : "Size should be 7";
        assert h.numTrees() == 3 : "Should have 3 trees after consolidation (ranks 0,1,2)";
    }

    /** 4. Non-Lazy Meld: Linking should happen immediately */
    private static void test4_NonLazyMeld() {
        System.out.println("Test 4: Non-Lazy Meld...");
        Heap h1 = new Heap(false, true);
        Heap h2 = new Heap(false, true);

        for (int i = 1; i <= 4; i++) h1.insert(i, "");
        for (int i = 5; i <= 8; i++) h2.insert(i, "");

        h1.meld(h2);

        // size 8 = 1000b => with full consolidation should end with 1 tree
        assert h1.size() == 8 : "h1 size should be 8";
        assert h1.numTrees() == 1 : "Non-lazy meld should consolidate to 1 tree";
        assert h1.findMin() != null && h1.findMin().key == 1 : "Min should be 1";
    }

    /** 5. Lazy DecreaseKey: ensure at least one CUT occurs and min updates */
    private static void test5_LazyDecreaseKey_CutHappens() {
        System.out.println("Test 5: Lazy decreaseKey causes cut(s)...");
        Heap h = new Heap(true, true); // Fibonacci behavior

        // Build a non-trivial structure: insert 1..16 then deleteMin to consolidate
        Heap.HeapItem[] items = new Heap.HeapItem[16];
        for (int i = 0; i < 16; i++) items[i] = h.insert(i + 1, "v" + (i + 1));
        h.deleteMin(); // remove 1, consolidate remaining 15 nodes into binomial forest

        // Pick some non-min item and decrease a lot; likely violates parent relation => cut happens.
        Heap.HeapItem x = items[15]; // key=16 originally
        int cutsBefore = h.totalCuts();
        h.decreaseKey(x, 1000); // makes key negative in theory, but your code just subtracts; assume tests allow

        assert h.findMin() == x : "Decreased item should become min";
        assert h.totalCuts() > cutsBefore : "At least one cut should have happened in lazyDecreaseKeys=true";
    }

    /**
     * 6. Non-Lazy DecreaseKey: heapifyUp via HeapItem swaps must happen.
     * We avoid relying on a specific parent-child relation by forcing a consolidation
     * then decreasing a non-root item and checking totalHeapifyCosts increases.
     */
    private static void test6_NonLazyDecreaseKey_HeapifyUpHappens() {
        System.out.println("Test 6: Non-Lazy decreaseKey triggers heapifyUp swaps...");
        Heap h = new Heap(false, false); // non-lazy melds + heapifyUp decreaseKey (binomial-like)

        // Insert many keys; non-lazy melds consolidates on each insert => structured forest.
        Heap.HeapItem[] items = new Heap.HeapItem[32];
        for (int i = 0; i < 32; i++) items[i] = h.insert(100 + i, "x" + i);

        // Choose some item that is very unlikely to remain as a root in a consolidated binomial heap.
        Heap.HeapItem target = items[31]; // key=131

        int before = h.totalHeapifyCosts();
        h.decreaseKey(target, 200); // 131 -> -69, should rise towards root by swaps

        assert h.totalHeapifyCosts() > before : "heapifyUp swaps should increase totalHeapifyCosts";
        assert h.findMin() == target || h.findMin().key <= target.key : "Min should be <= decreased key";
        assert h.numMarkedNodes() == 0 : "When lazyDecreaseKeys=false, marked nodes must be 0";
    }

    /** 7. Delete specific item */
    private static void test7_DeleteSpecificItem() {
        System.out.println("Test 7: Delete Specific Item...");
        Heap h = new Heap(true, true);

        Heap.HeapItem target = h.insert(50, "target");
        h.insert(10, "min");

        h.delete(target);

        assert h.size() == 1 : "Size should be 1 after deleting one item";
        assert h.findMin() != null && h.findMin().key == 10 : "Min should be 10";
    }

    /** 8. Meld with empty heap */
    private static void test8_MeldWithEmptyHeap() {
        System.out.println("Test 8: Meld with Empty...");
        Heap h1 = new Heap(true, true);
        h1.insert(10, "");
        Heap h2 = new Heap(true, true);

        h1.meld(h2);

        assert h1.size() == 1 : "h1 size should remain 1";
        assert h1.findMin() != null && h1.findMin().key == 10 : "h1 min should remain 10";
        assert h2.findMin() == null : "h2 should still be empty";
    }

    /** 9. Large Scale Stress Test */
    private static void test9_LargeScaleInsertAndDeleteMin() {
        System.out.println("Test 9: Large Scale Stress Test...");
        Heap h = new Heap(true, true);
        int count = 1000;

        for (int i = count; i > 0; i--) {
            h.insert(i, "val" + i);
        }
        assert h.size() == count : "Size should be 1000";
        assert h.findMin() != null && h.findMin().key == 1 : "Min should be 1";

        for (int i = 0; i < 100; i++) {
            h.deleteMin();
        }
        assert h.size() == count - 100 : "Size should be 900";
        assert h.findMin() != null && h.findMin().key == 101 : "Min should be 101 after deleting 100 mins";
    }

    /** 10. Empty Heap Edge Cases */
    private static void test10_EmptyHeapEdgeCases() {
        System.out.println("Test 10: Empty Heap Edge Cases...");
        Heap h = new Heap(true, true);

        // These should not throw exceptions in your implementation
        h.deleteMin();
        h.decreaseKey(null, 0);
        h.delete(null);

        assert h.size() == 0 : "Size should be 0";
        assert h.numTrees() == 0 : "NumTrees should be 0";
        assert h.findMin() == null : "Min should be null";
    }

    /** 11. Requirement: if lazyDecreaseKeys=false then no node can be marked */
    private static void test11_MarkedNodesMustBeZeroWhenNonLazyDecrease() {
        System.out.println("Test 11: No marked nodes when lazyDecreaseKeys=false...");
        Heap h = new Heap(true, false); // lazy melds, but decrease is non-lazy (heapifyUp)
        Heap.HeapItem a = h.insert(10, "a");
        Heap.HeapItem b = h.insert(20, "b");
        Heap.HeapItem c = h.insert(30, "c");

        // Trigger a decreaseKey that might cause swaps
        h.decreaseKey(c, 25);

        assert h.numMarkedNodes() == 0 : "Marked nodes must remain 0 when lazyDecreaseKeys=false";
        assert h.findMin() != null : "Min should not be null";
    }
}
