import java.util.*;

public class HeapEdgeCaseTester {

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

    // ===== ASSERT HELPERS =====
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
            System.out.println("[" + id + "] ✔ " + name +
                    " (lazyMelds=" + lm + ", lazyDecreaseKeys=" + ld + ")");
        } catch (Throwable e) {
            results.add(new TestResult(id, name, lm, ld, false, e.getMessage()));
            System.out.println("[" + id + "] ✘ " + name +
                    " (lazyMelds=" + lm + ", lazyDecreaseKeys=" + ld + ")");
        }
    }

    // =========================================================
    // ===================== EDGE TESTS ========================
    // =========================================================

    /** insert בלבד – בדיקות size / numTrees */
    private static void testOnlyInserts(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        for (int i = 1; i <= 20; i++) {
            h.insert(i, "");
            checkEquals(i, h.size(), "size after insert");
            check(h.numTrees() >= 1, "numTrees positive");
        }
        checkMin(h, 1, "min after only inserts");
    }

    /** deleteMin רצוף עד ריק */
    private static void testRepeatedDeleteMin(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        for (int i = 20; i >= 1; i--) h.insert(i, "");
        for (int i = 1; i <= 20; i++) {
            checkMin(h, i, "deleteMin sequence");
            h.deleteMin();
            checkEquals(20 - i, h.size(), "size after deleteMin");
        }
        checkEquals(0, h.numTrees(), "numTrees empty");
        checkEquals(0, h.numMarkedNodes(), "numMarked empty");
    }

    /** decreaseKey עד הפיכה למינימום */
    private static void testDecreaseKeyToMin(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        Heap.HeapItem x = h.insert(50, "");
        h.insert(10, "");
        h.insert(20, "");
        h.decreaseKey(x, 49); // key becomes 1
        checkMin(h, 1, "decreaseKey creates new min");
    }

    /** decreaseKey = 0 (no-op לוגי) */
    private static void testDecreaseKeyZero(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        Heap.HeapItem x = h.insert(7, "");
        h.decreaseKey(x, 0);
        checkMin(h, 7, "decreaseKey zero");
        checkEquals(1, h.size(), "size unchanged");
    }

    /** delete של צומת שאינו מינימום */
    private static void testDeleteNonMin(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        Heap.HeapItem a = h.insert(5, "");
        Heap.HeapItem b = h.insert(10, "");
        Heap.HeapItem c = h.insert(1, "");
        h.delete(b);
        checkEquals(2, h.size(), "delete non-min size");
        checkMin(h, 1, "delete non-min min");
    }

    /** deleteMin על ערימה עם עץ יחיד */
    private static void testDeleteMinSingleTree(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        h.insert(3, "");
        h.insert(4, "");
        h.insert(5, "");
        h.deleteMin();
        checkEquals(2, h.size(), "size after deleteMin");
        checkMin(h, 4, "min after deleteMin");
    }

    /** meld עם ערימה ריקה */
    private static void testMeldWithEmpty(boolean lm, boolean ld) {
        Heap h1 = new Heap(lm, ld);
        Heap h2 = new Heap(lm, ld);
        h1.insert(4, "");
        h1.meld(h2);
        checkEquals(1, h1.size(), "meld empty");
        checkMin(h1, 4, "meld empty min");
    }

    /** meld דו־כיווני */
    private static void testSymmetricMeld(boolean lm, boolean ld) {
        Heap h1 = new Heap(lm, ld);
        Heap h2 = new Heap(lm, ld);
        h1.insert(10, "");
        h2.insert(3, "");
        h2.insert(7, "");
        h2.meld(h1);
        checkEquals(3, h2.size(), "symmetric meld size");
        checkMin(h2, 3, "symmetric meld min");
    }

    /** בדיקת עקביות totals */
    private static void testTotalsConsistency(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        List<Heap.HeapItem> nodes = new ArrayList<>();

        for (int i = 0; i < 15; i++)
            nodes.add(h.insert(100 - i, ""));

        for (int i = 0; i < 5; i++)
            h.delete(nodes.get(i));

        checkEquals(10, h.size(), "size after deletes");
        check(h.numTrees() <= h.size(), "trees <= size");
        check(h.numMarkedNodes() <= h.size(), "marked <= size");
    }

    /** ריבוי decreaseKey + deleteMin */
    private static void testMixedOperations(boolean lm, boolean ld) {
        Heap h = new Heap(lm, ld);
        Heap.HeapItem a = h.insert(30, "");
        Heap.HeapItem b = h.insert(40, "");
        Heap.HeapItem c = h.insert(50, "");

        h.decreaseKey(c, 45); // 5
        checkMin(h, 5, "mixed decreaseKey");

        h.deleteMin();
        checkEquals(2, h.size(), "mixed deleteMin size");
        checkMin(h, 30, "mixed min");
    }

    // ===== MAIN =====
    public static void main(String[] args) {
        boolean[] flags = {false, true};

        System.out.println("\n=== HEAP EDGE CASE TESTER (NON-STOPPING) ===\n");

        for (boolean lm : flags) {
            for (boolean ld : flags) {

                runTest("Only inserts", lm, ld, () -> testOnlyInserts(lm, ld));
                runTest("Repeated deleteMin", lm, ld, () -> testRepeatedDeleteMin(lm, ld));
                runTest("DecreaseKey to min", lm, ld, () -> testDecreaseKeyToMin(lm, ld));
                runTest("DecreaseKey zero", lm, ld, () -> testDecreaseKeyZero(lm, ld));
                runTest("Delete non-min", lm, ld, () -> testDeleteNonMin(lm, ld));
                runTest("DeleteMin single tree", lm, ld, () -> testDeleteMinSingleTree(lm, ld));
                runTest("Meld with empty", lm, ld, () -> testMeldWithEmpty(lm, ld));
                runTest("Symmetric meld", lm, ld, () -> testSymmetricMeld(lm, ld));
                runTest("Totals consistency", lm, ld, () -> testTotalsConsistency(lm, ld));
                runTest("Mixed operations", lm, ld, () -> testMixedOperations(lm, ld));
            }
        }

        long passed = results.stream().filter(r -> r.passed).count();
        long failed = results.size() - passed;

        System.out.println("\n=== SUMMARY ===");
        System.out.println("Total tests : " + results.size());
        System.out.println("Passed      : " + passed);
        System.out.println("Failed      : " + failed);

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
            System.out.println("\nALL EDGE TESTS PASSED ✔💪");
        }
    }
}
