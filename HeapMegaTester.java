import java.util.*;

public class HeapMegaTester {

    // ===== RESULT STRUCT =====
    static class TestResult {
        int id;
        String name;
        boolean lazyMelds;
        boolean lazyDecreaseKeys;
        boolean passed;
        String message;

        TestResult(int id, String name, boolean lm, boolean ld, boolean passed, String msg) {
            this.id = id;
            this.name = name;
            this.lazyMelds = lm;
            this.lazyDecreaseKeys = ld;
            this.passed = passed;
            this.message = msg;
        }
    }

    static List<TestResult> results = new ArrayList<>();
    static int testCounter = 0;

    // ===== ASSERT HELPERS (NON-STOPPING) =====
    private static void check(boolean cond, String msg) {
        if (!cond) throw new RuntimeException(msg);
    }

    private static void checkEquals(int exp, int got, String msg) {
        if (exp != got)
            throw new RuntimeException(msg + " | expected=" + exp + ", got=" + got);
    }

    private static void checkMin(Heap h, Integer expected, String msg) {
        Heap.HeapItem m = h.findMin();
        if (expected == null) {
            if (m != null)
                throw new RuntimeException(msg + " | expected null, got " + m.key);
        } else {
            if (m == null || m.key != expected)
                throw new RuntimeException(msg + " | expected min=" + expected +
                        ", got " + (m == null ? "null" : m.key));
        }
    }

    // ===== RUNNER =====
    private static void runTest(String name, boolean lm, boolean ld, Runnable test) {
        int id = ++testCounter;
        try {
            test.run();
            results.add(new TestResult(id, name, lm, ld, true, "OK"));
            System.out.println("[" + id + "] ✔ \u001B[32m" + name +
                    "\u001B[0m (lazyMelds=" + lm + ", lazyDecreaseKeys=" + ld + ")");
        } catch (Throwable e) {
            results.add(new TestResult(id, name, lm, ld, false, e.getMessage()));
            System.out.println("[" + id + "] ✘ \u001B[31m" + name +
                    "\u001B[0m (lazyMelds=" + lm + ", lazyDecreaseKeys=" + ld + ")");
        }
    }

    // ===== TESTS =====

    private static void testEmptyHeap(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        checkEquals(0, h.size(), "empty size");
        checkMin(h, null, "empty min");
        checkEquals(0, h.numTrees(), "empty numTrees");
        checkEquals(0, h.numMarkedNodes(), "empty numMarked");
    }

    private static void testSingleInsertDelete(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        h.insert(5, "");
        checkEquals(1, h.size(), "single insert size");
        checkMin(h, 5, "single insert min");
        h.deleteMin();
        checkEquals(0, h.size(), "single deleteMin size");
        checkMin(h, null, "single deleteMin min");
    }

    private static void testDeleteMinOrder(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        int[] arr = {7, 1, 9, 3, 5};
        for (int x : arr) h.insert(x, "");
        Arrays.sort(arr);
        for (int x : arr) {
            checkMin(h, x, "deleteMin order");
            h.deleteMin();
        }
        checkEquals(0, h.size(), "deleteMin drains heap");
    }

    private static void testDecreaseKey(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        Heap.HeapItem a = h.insert(10, "");
        Heap.HeapItem b = h.insert(20, "");
        h.decreaseKey(b, 15); // becomes 5
        checkMin(h, 5, "decreaseKey min");
    }

    private static void testDeleteArbitrary(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        Heap.HeapItem a = h.insert(10, "");
        Heap.HeapItem b = h.insert(5, "");
        Heap.HeapItem c = h.insert(20, "");
        h.delete(b);
        checkEquals(2, h.size(), "delete(x) size");
        checkMin(h, 10, "delete(x) min");
    }

    private static void testMeld(boolean lm, boolean ld) {
        Heap h1 = new Heap(lm, ld);
        Heap h2 = new Heap(lm, ld);
        h1.insert(10, "");
        h1.insert(3, "");
        h2.insert(7, "");
        h2.insert(1, "");
        h1.meld(h2);
        checkEquals(4, h1.size(), "meld size");
        checkMin(h1, 1, "meld min");
    }

    private static void stressTest(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        Random rnd = new Random(42);
        List<Heap.HeapItem> items = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            int v = rnd.nextInt(10000) + 1;
            items.add(h.insert(v, ""));
            pq.add(v);
        }

        for (int i = 0; i < 300; i++) {
            if (!items.isEmpty() && rnd.nextBoolean()) {
                int idx = rnd.nextInt(items.size());
                Heap.HeapItem x = items.remove(idx);
                h.delete(x);
                pq.remove(x.key);
            } else {
                int v = rnd.nextInt(10000) + 1;
                items.add(h.insert(v, ""));
                pq.add(v);
            }

            if (!pq.isEmpty())
                checkMin(h, pq.peek(), "stress min");
        }
    }

    // ===== MAIN =====
    public static void main(String[] args) {
        boolean[] flags = {false, true};

        System.out.println("\n=== HEAP MEGA TESTER (NON-STOPPING) ===\n");

        for (boolean lm : flags) {
            for (boolean ld : flags) {

                runTest("Empty heap", lm, ld, () -> testEmptyHeap(lm, ld));
                runTest("Single insert + deleteMin", lm, ld, () -> testSingleInsertDelete(lm, ld));
                runTest("DeleteMin order", lm, ld, () -> testDeleteMinOrder(lm, ld));
                runTest("DecreaseKey", lm, ld, () -> testDecreaseKey(lm, ld));
                runTest("Delete arbitrary node", lm, ld, () -> testDeleteArbitrary(lm, ld));
                runTest("Meld", lm, ld, () -> testMeld(lm, ld));
                runTest("Stress test", lm, ld, () -> stressTest(lm, ld));
            }
        }

        // ===== SUMMARY =====
        long passed = results.stream().filter(r -> r.passed).count();
        long failed = results.size() - passed;

        System.out.println("\n=== SUMMARY ===");
        System.out.println("Total tests : " + results.size());
        System.out.println("\u001B[32mPassed      : " + passed + "\u001B[0m");
        System.out.println("\u001B[31mFailed      : " + failed + "\u001B[0m");

        if (failed > 0) {
            System.out.println("\n=== FAILED TESTS DETAILS ===");
            for (TestResult r : results) {
                if (!r.passed) {
                    System.out.println("[" + r.id + "] " + r.name +
                            " (lazyMelds=" + r.lazyMelds +
                            ", lazyDecreaseKeys=" + r.lazyDecreaseKeys + ")");
                    System.out.println("    Reason: " + r.message);
                }
            }
        } else {
            System.out.println("\nALL TESTS PASSED ✔🔥");
        }
    }
}
