import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Parallel merge sort using Java's Fork/Join framework.
 * <p>
 * The Fork/Join framework (introduced in Java 7) is designed for recursive
 * divide-and-conquer parallelism. The idea is simple: split a large task into
 * two halves, fork one half to run in parallel on another thread, compute the
 * other half yourself, then join (wait for) the forked result. The framework
 * manages a pool of worker threads with work-stealing for load balancing.
 * <p>
 * This implementation includes two important optimizations that real-world
 * parallel sorts use:
 * <ul>
 *   <li><b>Parallelism cutoff</b>: below a threshold, both halves are sorted
 *       sequentially in the same thread, avoiding the overhead of task creation
 *       and thread scheduling for tiny subarrays.</li>
 *   <li><b>Insertion sort cutoff</b>: very small subarrays are sorted with
 *       insertion sort instead of merge sort, because its lower overhead and
 *       cache-friendliness beat merge sort at small sizes.</li>
 * </ul>
 *
 * Updated for Java 21+ with better naming, documentation, and a demonstration
 * main method that benchmarks parallel vs. sequential performance.
 *
 * @author Ilkka Kokkarinen
 */
public class FJMergeSort extends RecursiveAction {

    // -----------------------------------------------------------------------
    // Tuning parameters — adjust based on hardware and array element size.
    // -----------------------------------------------------------------------

    /** Subarrays smaller than this are sorted with insertion sort. */
    private static final int INSERTION_SORT_THRESHOLD = 50;

    /**
     * Subarrays smaller than this are not worth splitting into parallel
     * tasks — the overhead of forking and joining would exceed the gain.
     */
    private static final int PARALLEL_THRESHOLD = 300;

    // -----------------------------------------------------------------------
    // Task state: the subarray range and the workspace buffer.
    // -----------------------------------------------------------------------

    private final int[] array;      // The array being sorted (shared, mutated in place).
    private final int[] workspace;  // Temporary buffer for merging (same length as array).
    private final int low;          // Inclusive start of the subarray to sort.
    private final int high;         // Inclusive end of the subarray to sort.

    /**
     * Create a task to sort the subarray {@code array[low..high]} (inclusive).
     *
     * @param array     the array to sort
     * @param workspace a temporary buffer of the same length, used during merging
     * @param low       the inclusive lower bound of the subarray
     * @param high      the inclusive upper bound of the subarray
     */
    public FJMergeSort(int[] array, int[] workspace, int low, int high) {
        this.array = array;
        this.workspace = workspace;
        this.low = low;
        this.high = high;
    }

    /** Convenience constructor to sort the entire array. */
    public FJMergeSort(int[] array) {
        this(array, new int[array.length], 0, array.length - 1);
    }

    // -----------------------------------------------------------------------
    // RecursiveAction entry point — called by the Fork/Join framework.
    // -----------------------------------------------------------------------

    @Override
    protected void compute() {
        parallelMergeSort(low, high);
    }

    // -----------------------------------------------------------------------
    // The recursive merge sort with parallel forking.
    // -----------------------------------------------------------------------

    /**
     * Sort {@code array[low..high]} using merge sort. If the subarray is
     * large enough, the left half is forked to a new parallel task while
     * the right half is sorted in this thread.
     */
    private void parallelMergeSort(int low, int high) {
        int size = high - low + 1;

        if (size <= INSERTION_SORT_THRESHOLD) {
            // Too small for merge sort overhead — use insertion sort.
            insertionSort(low, high);
            return;
        }

        int mid = low + (high - low) / 2; // Avoids overflow vs. (low + high) / 2.

        if (size > PARALLEL_THRESHOLD) {
            // Large enough to benefit from parallelism: fork the left half
            // as a new task, sort the right half here, then wait for the left.
            var leftTask = new FJMergeSort(array, workspace, low, mid);
            leftTask.fork();                    // Start left half in parallel.
            parallelMergeSort(mid + 1, high);   // Sort right half in this thread.
            leftTask.join();                    // Wait for left half to finish.
        } else {
            // Too small for parallelism overhead — sort both halves sequentially.
            parallelMergeSort(low, mid);
            parallelMergeSort(mid + 1, high);
        }

        // Either way, the two sorted halves must now be merged.
        merge(low, mid, high);
    }

    // -----------------------------------------------------------------------
    // Merge two adjacent sorted subarrays into one sorted subarray.
    // -----------------------------------------------------------------------

    /**
     * Merge the sorted subarrays {@code array[low..mid]} and
     * {@code array[mid+1..high]} into a single sorted subarray.
     * Uses {@code workspace} as temporary storage, then copies back.
     */
    private void merge(int low, int mid, int high) {
        int left = low;         // Cursor in the left half.
        int right = mid + 1;    // Cursor in the right half.
        int dest = low;         // Write position in workspace.

        // Merge by always picking the smaller of the two front elements.
        while (left <= mid && right <= high) {
            if (array[left] <= array[right]) {
                workspace[dest++] = array[left++];
            } else {
                workspace[dest++] = array[right++];
            }
        }
        // Copy whichever half has remaining elements.
        while (left <= mid)   { workspace[dest++] = array[left++]; }
        while (right <= high) { workspace[dest++] = array[right++]; }

        // Copy the merged result back into the original array.
        System.arraycopy(workspace, low, array, low, high - low + 1);
    }

    // -----------------------------------------------------------------------
    // Insertion sort for small subarrays — low overhead, cache-friendly.
    // -----------------------------------------------------------------------

    /**
     * Sort {@code array[low..high]} with insertion sort. For small subarrays,
     * this beats merge sort due to lower constant factors and better cache
     * locality (no auxiliary buffer needed).
     */
    private void insertionSort(int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            int key = array[i];
            int j = i;
            while (j > low && array[j - 1] > key) {
                array[j] = array[j - 1];
                j--;
            }
            array[j] = key;
        }
    }

    // -----------------------------------------------------------------------
    // Main — demonstrate and benchmark the parallel merge sort.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        var rng = new Random(42);
        int size = 10_000_000;

        System.out.printf("Generating %,d random integers...%n", size);
        int[] original = rng.ints(size).toArray();

        // --- Parallel Fork/Join merge sort ---
        int[] forForkJoin = original.clone();
        System.out.printf("%nParallel FJ merge sort (%d threads available):%n",
                ForkJoinPool.commonPool().getParallelism() + 1);
        long start = System.nanoTime();
        ForkJoinPool.commonPool().invoke(new FJMergeSort(forForkJoin));
        long fjTime = System.nanoTime() - start;
        System.out.printf("  Time: %.3f ms%n", fjTime / 1_000_000.0);

        // --- Arrays.parallelSort for comparison (uses a similar strategy) ---
        int[] forParallelSort = original.clone();
        System.out.println("\nArrays.parallelSort (JDK built-in):");
        start = System.nanoTime();
        Arrays.parallelSort(forParallelSort);
        long psTime = System.nanoTime() - start;
        System.out.printf("  Time: %.3f ms%n", psTime / 1_000_000.0);

        // --- Sequential Arrays.sort for comparison ---
        int[] forSequentialSort = original.clone();
        System.out.println("\nArrays.sort (sequential, dual-pivot quicksort):");
        start = System.nanoTime();
        Arrays.sort(forSequentialSort);
        long seqTime = System.nanoTime() - start;
        System.out.printf("  Time: %.3f ms%n", seqTime / 1_000_000.0);

        // --- Verify correctness ---
        boolean correct = Arrays.equals(forForkJoin, forSequentialSort)
                && Arrays.equals(forParallelSort, forSequentialSort);
        System.out.printf("%nAll three produce identical results: %s%n", correct);

        // --- Small example for visual verification ---
        System.out.println("\nSmall example (20 elements):");
        int[] small = rng.ints(20, 0, 100).toArray();
        System.out.println("Before: " + Arrays.toString(small));
        ForkJoinPool.commonPool().invoke(new FJMergeSort(small));
        System.out.println("After:  " + Arrays.toString(small));
    }
}