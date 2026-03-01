import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Four array searching algorithms, from the simplest to the most efficient,
 * each illustrating a different optimization idea.
 * <p>
 * <ol>
 *   <li><b>Linear search</b> — the baseline. Checks every element left to
 *       right. O(n) with two comparisons per element (bounds + value).</li>
 *   <li><b>Sentinel search</b> — a clever trick that temporarily plants the
 *       target at the end of the array, eliminating the bounds check from
 *       the inner loop. Still O(n), but roughly half the comparisons.</li>
 *   <li><b>Unrolled search</b> — processes two elements per iteration,
 *       halving the number of loop-control bounds checks. A technique
 *       borrowed from high-performance computing (Duff's device, etc.).</li>
 *   <li><b>Binary search</b> — for <em>sorted</em> arrays only. Halves the
 *       search space each step, giving O(log n) performance.</li>
 * </ol>
 * <p>
 * All methods are static (they don't need instance state) and the main method
 * benchmarks them side by side.
 *
 * @author Ilkka Kokkarinen
 */
public class ArraySearchDemo {

    // -----------------------------------------------------------------------
    // 1. Linear search — the straightforward baseline.
    //
    // Two comparisons per element: one for bounds, one for value.
    // -----------------------------------------------------------------------

    /**
     * Search for {@code target} in {@code array} by scanning left to right.
     *
     * @param array  the array to search
     * @param target the value to find
     * @return the index of the first occurrence, or -1 if not found
     */
    public static int linearSearch(int[] array, int target) {
        int i = 0;
        while (i < array.length && array[i] != target) { i++; }
        return (i < array.length) ? i : -1;
    }

    // -----------------------------------------------------------------------
    // 2. Sentinel search — eliminate the bounds check.
    //
    // By temporarily placing the target at the last position, the loop is
    // guaranteed to terminate without ever checking i < array.length.
    // After the loop, we restore the original last element and figure out
    // whether we found a real match or just hit the sentinel.
    //
    // IMPORTANT: this mutates the array temporarily, so it is NOT safe for
    // concurrent use. It also requires a non-empty array.
    // -----------------------------------------------------------------------

    /**
     * Search for {@code target} using a sentinel at the end of the array.
     * The inner loop contains only a single comparison per element.
     *
     * @param array  the array to search (must be non-empty; temporarily mutated)
     * @param target the value to find
     * @return the index of the first occurrence, or -1 if not found
     */
    public static int sentinelSearch(int[] array, int target) {
        int lastIndex = array.length - 1;
        int savedLast = array[lastIndex];  // Save the last element.
        array[lastIndex] = target;         // Plant the sentinel.

        int i = 0;
        while (array[i] != target) { i++; } // No bounds check needed!

        array[lastIndex] = savedLast;      // Restore the original value.

        // Did we find a real match, or just the sentinel?
        return (i < lastIndex || savedLast == target) ? i : -1;
    }

    // -----------------------------------------------------------------------
    // 3. Unrolled search — process two elements per iteration.
    //
    // Each iteration checks two consecutive elements against the target,
    // cutting the number of loop-control (bounds) checks in half. If the
    // array has an odd number of elements, we handle the first one separately
    // so the main loop always processes pairs.
    //
    // This is a simplified version of "loop unrolling", a technique from
    // high-performance computing. The classic extreme version is Duff's
    // Device, which is worth looking up for entertainment value alone.
    // -----------------------------------------------------------------------

    /**
     * Search for {@code target} with a loop unrolled to two elements per step.
     *
     * @param array  the array to search
     * @param target the value to find
     * @return the index of the first occurrence, or -1 if not found
     */
    public static int unrolledSearch(int[] array, int target) {
        int i = 0;
        // Handle the odd element so the main loop always has pairs.
        if (array.length % 2 == 1) {
            if (array[i++] == target) { return 0; }
        }
        // Main loop: two elements per iteration, one bounds check.
        while (i < array.length) {
            if (array[i++] == target) { return i - 1; }
            if (array[i++] == target) { return i - 1; }
        }
        return -1;
    }

    // -----------------------------------------------------------------------
    // 4. Binary search — O(log n) for sorted arrays.
    //
    // Maintains a range [low, high] that is guaranteed to contain the target
    // if it exists. Each step picks the midpoint and discards half the range.
    //
    // If the target is not found, returns the insertion point — the index
    // where the target would need to be inserted to keep the array sorted.
    // This makes binary search useful for more than just "is it there?".
    // -----------------------------------------------------------------------

    /**
     * Search for {@code target} in a sorted array using binary search.
     * <p>
     * If found, returns the index of the target. If not found, returns the
     * <em>insertion point</em>: the index where the target would go to keep
     * the array in sorted order. Returns {@code array.length} if the target
     * is larger than every element.
     *
     * @param sorted the array to search (must be sorted in ascending order)
     * @param target the value to find
     * @return the index of the target or its insertion point
     */
    public static int binarySearch(int[] sorted, int target) {
        int low = 0;
        int high = sorted.length - 1;

        if (sorted[high] < target) { return sorted.length; } // Larger than all elements.

        while (low < high) {
            int mid = low + (high - low) / 2; // Overflow-safe midpoint.
            if (sorted[mid] < target) {
                low = mid + 1;  // Target must be to the right.
            } else {
                high = mid;     // Target is here or to the left.
            }
        }
        return low;
    }

    // -----------------------------------------------------------------------
    // Benchmarking helper.
    // -----------------------------------------------------------------------

    @FunctionalInterface
    private interface SearchMethod {
        int search(int[] array, int target);
    }

    /**
     * Time a search method over many random lookups and return the average
     * nanoseconds per search.
     */
    private static double benchmarkSearch(String name, SearchMethod method,
                                          int[] array, int[] targets) {
        // Warmup run (JIT compilation).
        for (int target : targets) { method.search(array, target); }

        long start = System.nanoTime();
        int dummy = 0; // Prevent dead-code elimination.
        for (int target : targets) {
            dummy += method.search(array, target);
        }
        long elapsed = System.nanoTime() - start;
        double nanosPerSearch = (double) elapsed / targets.length;
        System.out.printf("  %-18s %8.1f ns/search%n", name, nanosPerSearch);
        return nanosPerSearch + dummy * 0.0; // Use dummy to prevent optimization.
    }

    // -----------------------------------------------------------------------
    // Main — verify correctness and benchmark all four algorithms.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        var rng = new Random(42);

        // --- Correctness verification ---
        System.out.println("=== Correctness Verification ===\n");
        int[] testArray = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        System.out.println("Array: " + Arrays.toString(testArray));
        System.out.println();

        for (int target : new int[]{5, 9, 7, 3}) {
            System.out.printf("Searching for %d:%n", target);
            System.out.printf("  linearSearch:   %d%n", linearSearch(testArray, target));
            System.out.printf("  sentinelSearch: %d%n", sentinelSearch(testArray, target));
            System.out.printf("  unrolledSearch: %d%n", unrolledSearch(testArray, target));
            System.out.println();
        }

        // Binary search on a sorted array.
        int[] sortedArray = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};
        System.out.println("Sorted array: " + Arrays.toString(sortedArray));
        for (int target : new int[]{23, 1, 50, 100}) {
            int result = binarySearch(sortedArray, target);
            boolean found = (result < sortedArray.length && sortedArray[result] == target);
            System.out.printf("  binarySearch(%d): index=%d (%s)%n",
                    target, result, found ? "found" : "insertion point");
        }

        // --- Benchmark ---
        System.out.println("\n=== Benchmark (unsorted, n=100,000) ===\n");
        int size = 100_000;
        int[] largeArray = rng.ints(size, 0, size * 10).toArray();
        int[] targets = rng.ints(10_000, 0, size * 10).toArray();

        benchmarkSearch("linearSearch",   ArraySearchDemo::linearSearch,   largeArray, targets);
        benchmarkSearch("sentinelSearch", ArraySearchDemo::sentinelSearch, largeArray, targets);
        benchmarkSearch("unrolledSearch", ArraySearchDemo::unrolledSearch, largeArray, targets);

        // Binary search needs a sorted copy.
        int[] sortedLarge = largeArray.clone();
        Arrays.sort(sortedLarge);
        System.out.println();
        System.out.println("  (Binary search on sorted copy:)");
        benchmarkSearch("binarySearch",   ArraySearchDemo::binarySearch,   sortedLarge, targets);

        // --- Verify all linear searches agree ---
        System.out.println("\n=== Agreement Check ===\n");
        boolean allAgree = IntStream.of(targets).allMatch(t -> {
            int r1 = linearSearch(largeArray, t);
            int r2 = sentinelSearch(largeArray, t);
            int r3 = unrolledSearch(largeArray, t);
            return r1 == r2 && r2 == r3;
        });
        System.out.println("All three linear searches agree on every target: " + allAgree);
    }
}