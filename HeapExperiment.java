import java.util.*;

public class HeapExperiment {

    // Defined in the PDF, section 4
    private static final int N = 464646; 
    private static final int NUM_REPETITIONS = 20;

    // Helper class to store aggregate results for a specific heap type
    static class ExperimentStats {
        double runTime = 0;
        double finalSize = 0;
        double finalNumTrees = 0;
        double totalLinks = 0;
        double totalCuts = 0;
        double totalHeapify = 0;
        double maxCost = 0; // Average of the "Max Cost" seen in each run
    }

    public static void main(String[] args) {
        System.out.println("Running Experiments with N=" + N + " and " + NUM_REPETITIONS + " repetitions...");
        System.out.println("This may take a few minutes.\n");

        runExperiment1();
        runExperiment2();
        runExperiment3();
    }

    // =========================================================================================
    // EXPERIMENT 1
    // 1. Insert 1..n in random order
    // 2. DeleteMin
    // =========================================================================================
    private static void runExperiment1() {
        System.out.println("=== EXPERIMENT 1 ===");
        ExperimentStats[] stats = new ExperimentStats[4];
        for (int i = 0; i < 4; i++) stats[i] = new ExperimentStats();

        for (int iter = 0; iter < NUM_REPETITIONS; iter++) {
            int[] permutation = generatePermutation(N);

            // Run for each heap type
            runSingleExp1(permutation, false, false, stats[0]); // Binomial
            runSingleExp1(permutation, true, false, stats[1]);  // Lazy Binomial
            runSingleExp1(permutation, true, true, stats[2]);   // Fibonacci
            runSingleExp1(permutation, false, true, stats[3]);  // Binomial with Detachments
            
            System.out.print("."); // Progress bar
        }
        System.out.println();
        printTable("Experiment 1", stats);
    }

    private static void runSingleExp1(int[] permutation, boolean lm, boolean ld, ExperimentStats stats) {
        Heap heap = new Heap(lm, ld);
        long startTime = System.nanoTime();
        int currentRunMaxCost = 0;

        // 1. Insert random permutation
        for (int val : permutation) {
            currentRunMaxCost = Math.max(currentRunMaxCost, 
                measureOpCost(heap, () -> heap.insert(val, "")));
        }

        // 2. DeleteMin
        currentRunMaxCost = Math.max(currentRunMaxCost, 
            measureOpCost(heap, () -> heap.deleteMin()));

        long endTime = System.nanoTime();

        // Accumulate results
        stats.runTime += (endTime - startTime) / 1e6; // to ms
        stats.finalSize += heap.size();
        stats.finalNumTrees += heap.numTrees();
        stats.totalLinks += heap.totalLinks();
        stats.totalCuts += heap.totalCuts();
        stats.totalHeapify += heap.totalHeapifyCosts();
        stats.maxCost += currentRunMaxCost;
    }

    // =========================================================================================
    // EXPERIMENT 2
    // 1. Insert 1..n in random order
    // 2. DeleteMin
    // 3. Delete Max until 46 items remain
    // =========================================================================================
    private static void runExperiment2() {
        System.out.println("\n=== EXPERIMENT 2 ===");
        ExperimentStats[] stats = new ExperimentStats[4];
        for (int i = 0; i < 4; i++) stats[i] = new ExperimentStats();

        for (int iter = 0; iter < NUM_REPETITIONS; iter++) {
            int[] permutation = generatePermutation(N);
            
            runSingleExp2(permutation, false, false, stats[0]);
            runSingleExp2(permutation, true, false, stats[1]);
            runSingleExp2(permutation, true, true, stats[2]);
            runSingleExp2(permutation, false, true, stats[3]);
            
            System.out.print(".");
        }
        System.out.println();
        printTable("Experiment 2", stats);
    }

    private static void runSingleExp2(int[] permutation, boolean lm, boolean ld, ExperimentStats stats) {
        Heap heap = new Heap(lm, ld);
        // Array of pointers to items (index i holds item with key i)
        // Size N+1 because keys are 1..N
        Heap.HeapItem[] pointers = new Heap.HeapItem[N + 1];

        long startTime = System.nanoTime();
        int currentRunMaxCost = 0;

        // 1. Insert 1..n
        for (int val : permutation) {
            final int v = val;
            currentRunMaxCost = Math.max(currentRunMaxCost, 
                measureOpCost(heap, () -> {
                    pointers[v] = heap.insert(v, "");
                }));
        }

        // 2. DeleteMin (Removes key 1)
        currentRunMaxCost = Math.max(currentRunMaxCost, 
            measureOpCost(heap, () -> heap.deleteMin()));

        // 3. Delete Max until 46 items remain
        // Max starts at N and goes down. Key 1 is already gone.
        int currentMax = N;
        while (heap.size() > 46 && currentMax > 1) {
            final int k = currentMax;
            currentRunMaxCost = Math.max(currentRunMaxCost, 
                measureOpCost(heap, () -> heap.delete(pointers[k])));
            currentMax--;
        }

        long endTime = System.nanoTime();

        stats.runTime += (endTime - startTime) / 1e6;
        stats.finalSize += heap.size();
        stats.finalNumTrees += heap.numTrees();
        stats.totalLinks += heap.totalLinks();
        stats.totalCuts += heap.totalCuts();
        stats.totalHeapify += heap.totalHeapifyCosts();
        stats.maxCost += currentRunMaxCost;
    }

    // =========================================================================================
    // EXPERIMENT 3
    // 1. Insert 1..n in random order
    // 2. DeleteMin
    // 3. Perform [0.1n] DecreaseKey of Max to 0
    // 4. DeleteMin
    // =========================================================================================
    private static void runExperiment3() {
        System.out.println("\n=== EXPERIMENT 3 ===");
        ExperimentStats[] stats = new ExperimentStats[4];
        for (int i = 0; i < 4; i++) stats[i] = new ExperimentStats();

        for (int iter = 0; iter < NUM_REPETITIONS; iter++) {
            int[] permutation = generatePermutation(N);
            
            runSingleExp3(permutation, false, false, stats[0]);
            runSingleExp3(permutation, true, false, stats[1]);
            runSingleExp3(permutation, true, true, stats[2]);
            runSingleExp3(permutation, false, true, stats[3]);
            
            System.out.print(".");
        }
        System.out.println();
        printTable("Experiment 3", stats);
    }

    private static void runSingleExp3(int[] permutation, boolean lm, boolean ld, ExperimentStats stats) {
        Heap heap = new Heap(lm, ld);
        Heap.HeapItem[] pointers = new Heap.HeapItem[N + 1];

        long startTime = System.nanoTime();
        int currentRunMaxCost = 0;

        // 1. Insert 1..n
        for (int val : permutation) {
            final int v = val;
            currentRunMaxCost = Math.max(currentRunMaxCost, 
                measureOpCost(heap, () -> {
                    pointers[v] = heap.insert(v, "");
                }));
        }

        // 2. DeleteMin (Removes key 1)
        currentRunMaxCost = Math.max(currentRunMaxCost, 
            measureOpCost(heap, () -> heap.deleteMin()));

        // 3. [0.1n] DecreaseKey of Max to 0
        int iterations = (int)(0.1 * N);
        int currentMax = N;
        
        for (int i = 0; i < iterations; i++) {
            // Find current max (skipping already deleted/decreased items if any logic failed, 
            // but here we just go down from N)
            final int k = currentMax;
            final Heap.HeapItem item = pointers[k];
            
            // Decrease key to 0
            // Note: In the PDF it says "decrease key of max to 0".
            // Since item.key is k, diff = k - 0 = k.
            currentRunMaxCost = Math.max(currentRunMaxCost, 
                measureOpCost(heap, () -> heap.decreaseKey(item, k)));
            
            currentMax--;
        }

        // 4. DeleteMin
        currentRunMaxCost = Math.max(currentRunMaxCost, 
            measureOpCost(heap, () -> heap.deleteMin()));

        long endTime = System.nanoTime();

        stats.runTime += (endTime - startTime) / 1e6;
        stats.finalSize += heap.size();
        stats.finalNumTrees += heap.numTrees();
        stats.totalLinks += heap.totalLinks();
        stats.totalCuts += heap.totalCuts();
        stats.totalHeapify += heap.totalHeapifyCosts();
        stats.maxCost += currentRunMaxCost;
    }

    // =========================================================================================
    // HELPERS
    // =========================================================================================

    /**
     * Executes an operation and returns the "Cost" (Links + Cuts + Heapify) of that single operation.
     */
    private static int measureOpCost(Heap h, Runnable op) {
        int linksBefore = h.totalLinks();
        int cutsBefore = h.totalCuts();
        int heapifyBefore = h.totalHeapifyCosts();

        op.run();

        int linksDelta = h.totalLinks() - linksBefore;
        int cutsDelta = h.totalCuts() - cutsBefore;
        int heapifyDelta = h.totalHeapifyCosts() - heapifyBefore;

        return linksDelta + cutsDelta + heapifyDelta;
    }

    /**
     * Generates a random permutation of 1..N
     */
    private static int[] generatePermutation(int n) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) list.add(i);
        Collections.shuffle(list);
        
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = list.get(i);
        return arr;
    }

    /**
     * Prints the table in the format requested.
     */
    private static void printTable(String title, ExperimentStats[] stats) {
        String[] headers = {
            "Metric", 
            "Binomial", 
            "Lazy Binomial", 
            "Fibonacci", 
            "Binomial w/ Detach"
        };
        
        // stats[0] = Binomial (F, F)
        // stats[1] = Lazy Binomial (T, F)
        // stats[2] = Fibonacci (T, T)
        // stats[3] = Binomial w/ Detach (F, T)

        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.printf("%-25s | %-15s | %-15s | %-15s | %-20s%n", 
            title, headers[1], headers[2], headers[3], headers[4]);
        System.out.println("---------------------------------------------------------------------------------------------------------");

        printRow("Avg Runtime (ms)", stats, s -> s.runTime);
        printRow("Avg Size", stats, s -> s.finalSize);
        printRow("Avg Num Trees", stats, s -> s.finalNumTrees);
        printRow("Avg Total Links", stats, s -> s.totalLinks);
        printRow("Avg Total Cuts", stats, s -> s.totalCuts);
        printRow("Avg Total Heapify", stats, s -> s.totalHeapify);
        printRow("Max Cost (Single Op)", stats, s -> s.maxCost);
        
        System.out.println("---------------------------------------------------------------------------------------------------------\n");
    }

    private interface MetricExtractor {
        double get(ExperimentStats s);
    }

    private static void printRow(String name, ExperimentStats[] stats, MetricExtractor extractor) {
        System.out.printf("%-25s | %-15.2f | %-15.2f | %-15.2f | %-20.2f%n",
            name,
            extractor.get(stats[0]) / NUM_REPETITIONS,
            extractor.get(stats[1]) / NUM_REPETITIONS,
            extractor.get(stats[2]) / NUM_REPETITIONS,
            extractor.get(stats[3]) / NUM_REPETITIONS
        );
    }
}