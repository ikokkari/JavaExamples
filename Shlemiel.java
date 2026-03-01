import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * How not to be a Shlemiel: turning O(n²) algorithms into O(n) single-pass
 * algorithms through clever use of state variables, two-pointer techniques,
 * and auxiliary data structures.
 * <p>
 * The name comes from the old joke about Shlemiel the street painter, who
 * paints fewer and fewer lines each day because he keeps walking back to the
 * paint can instead of moving it forward with him. Many array problems have
 * a natural but wasteful O(n²) solution where work is needlessly repeated.
 * With some thought, these can almost always be turned into O(n) solutions.
 * <p>
 * Each problem is shown in two or three versions: the Shlemiel way first
 * (to understand the problem), then the efficient way (to understand the
 * technique). The main method benchmarks both to make the difference visceral.
 *
 * @author Ilkka Kokkarinen
 */
public class Shlemiel {

    // ===================================================================
    // 1. PREFIX SUM (ACCUMULATION) ARRAY
    //
    // Given array a, produce array b where b[i] = a[0] + a[1] + ... + a[i].
    // Shlemiel recomputes the entire sum from scratch for every position.
    // The smart version notices that b[i] = b[i-1] + a[i].
    // ===================================================================

    /** O(n²) — recomputes the sum from index 0 for every position. */
    public static int[] prefixSumsShlemiel(int[] values) {
        var sums = new int[values.length];
        for (int i = 0; i < sums.length; i++) {
            int total = 0;
            for (int j = 0; j <= i; j++) { total += values[j]; }
            sums[i] = total;
        }
        return sums;
    }

    /** O(n) — each new sum builds on the previous one. */
    public static int[] prefixSums(int[] values) {
        var sums = new int[values.length];
        if (sums.length == 0) { return sums; }
        sums[0] = values[0];
        for (int i = 1; i < values.length; i++) {
            sums[i] = sums[i - 1] + values[i];
        }
        return sums;
    }

    // ===================================================================
    // 2. LONGEST ASCENDING RUN
    //
    // Find the length of the longest contiguous strictly ascending subarray.
    // Shlemiel starts a fresh scan from every position.
    // The smart version maintains "current run" and "best so far" state.
    // ===================================================================

    /** O(n²) — starts a new inner scan from every index. */
    public static int longestAscendingShlemiel(int[] values) {
        if (values.length == 0) { return 0; }
        int longest = 1;
        for (int i = 0; i < values.length - 1; i++) {
            int j = i + 1;
            while (j < values.length && values[j] > values[j - 1]) { j++; }
            if (j - i > longest) { longest = j - i; }
        }
        return longest;
    }

    /** O(n) — single pass with two state variables. */
    public static int longestAscending(int[] values) {
        if (values.length == 0) { return 0; }
        int currentRun = 1;
        int longestRun = 1;
        for (int i = 1; i < values.length; i++) {
            if (values[i] > values[i - 1]) {
                currentRun++;
                if (currentRun > longestRun) { longestRun = currentRun; }
            } else {
                currentRun = 1;
            }
        }
        return longestRun;
    }

    // ===================================================================
    // 3. TWO ELEMENTS SUMMING TO A TARGET (sorted array)
    //
    // Shlemiel tries all pairs. The smart version uses two pointers that
    // walk inward from both ends, guided by whether the current sum is
    // too small (advance left) or too large (advance right).
    // ===================================================================

    /** O(n²) — tries all pairs with nested loops. */
    public static boolean twoSumShlemiel(int[] sorted, int target) {
        for (int i = 0; i < sorted.length; i++) {
            for (int j = i + 1; j < sorted.length && sorted[i] + sorted[j] <= target; j++) {
                if (sorted[i] + sorted[j] == target) { return true; }
            }
        }
        return false;
    }

    /** O(n) — two pointers walking inward from both ends. */
    public static boolean twoSum(int[] sorted, int target) {
        int left = 0;
        int right = sorted.length - 1;
        // Invariant: if a valid pair exists, it lies within [left, right].
        while (left < right) {
            int sum = sorted[left] + sorted[right];
            if (sum == target) { return true; }
            if (sum < target) { left++; }  // Need a larger sum → advance left.
            else              { right--; } // Need a smaller sum → advance right.
        }
        return false;
    }

    // Puzzle: suppose the task were to find THREE elements summing to target.
    // Shlemiel solves this in O(n³). Can you do O(n²)? How about O(n)?

    // ===================================================================
    // 4. POLYNOMIAL EVALUATION
    //
    // Three levels of improvement: Shlemiel recomputes x^i from scratch
    // each time. The linear version maintains a running power. Horner's
    // rule processes coefficients high-to-low, needing only one multiply
    // per step and giving better numerical stability.
    // ===================================================================

    /** O(n²) — recomputes x^i from scratch for each term. */
    public static double evalPolynomialShlemiel(double[] coefficients, double x) {
        double result = 0;
        for (int i = 0; i < coefficients.length; i++) {
            double power = 1;
            for (int j = 0; j < i; j++) { power *= x; }
            result += coefficients[i] * power;
        }
        return result;
    }

    /** O(n) — maintains a running power of x. Two multiplies per term. */
    public static double evalPolynomialLinear(double[] coefficients, double x) {
        double result = 0;
        double powerOfX = 1;
        for (double coeff : coefficients) {
            result += coeff * powerOfX;
            powerOfX *= x;
        }
        return result;
    }

    /** O(n) — Horner's rule: one multiply per term, better numerical stability. */
    public static double evalPolynomialHorner(double[] coefficients, double x) {
        double result = 0;
        for (int i = coefficients.length - 1; i >= 0; i--) {
            result = result * x + coefficients[i];
        }
        return result;
    }

    // ===================================================================
    // 5. IS THE ARRAY A PERMUTATION OF 1..n?
    //
    // Three approaches at different complexity levels:
    //   Shlemiel O(n²): for each number, scan the whole array.
    //   Sorting O(n log n): sort, then check consecutive values.
    //   Linear O(n): boolean "already seen" array trades space for time.
    // ===================================================================

    /** O(n²) — for each required number, scans the entire array. */
    public static boolean isPermutationShlemiel(int[] values, int n) {
        for (int target = 1; target <= n; target++) {
            boolean found = false;
            for (int element : values) {
                if (element == target) { found = true; break; }
            }
            if (!found) { return false; }
        }
        return true;
    }

    /** O(n log n) — sort first, then verify in a single pass. */
    public static boolean isPermutationSorting(int[] values, int n) {
        var sorted = values.clone(); // Don't modify the caller's array.
        Arrays.sort(sorted);
        for (int i = 0; i < n; i++) {
            if (sorted[i] != i + 1) { return false; }
        }
        return true;
    }

    /** O(n) — boolean tracking array trades memory for speed. */
    public static boolean isPermutationLinear(int[] values, int n) {
        var seen = new boolean[n + 1]; // Index 0 unused, for cleaner code.
        for (int element : values) {
            if (element < 1 || element > n || seen[element]) { return false; }
            seen[element] = true;
        }
        return true;
    }

    // ===================================================================
    // 6. REMOVE SHORT STRINGS FROM AN ARRAYLIST
    //
    // Shlemiel removes from the middle in a loop — each remove is O(n)
    // because it shifts all subsequent elements, giving O(n²) total.
    // The smart version builds a new list of keepers in O(n), or uses
    // the modern removeIf one-liner.
    // ===================================================================

    /** O(n²) — each remove(i) shifts all later elements left. */
    public static void removeShortStringsShlemiel(ArrayList<String> strings, int minLength) {
        int i = 0;
        while (i < strings.size()) {
            if (strings.get(i).length() < minLength) {
                strings.remove(i); // O(n) shift hidden inside this innocent-looking call!
            } else {
                i++;
            }
        }
    }

    /** O(n) — collect keepers into a buffer, then swap contents. */
    public static void removeShortStringsBuffer(ArrayList<String> strings, int minLength) {
        var keepers = new ArrayList<String>();
        for (String s : strings) {
            if (s.length() >= minLength) { keepers.add(s); } // O(1) amortized
        }
        strings.clear();       // O(n)
        strings.addAll(keepers); // O(n)
    }

    /** O(n) — modern one-liner using removeIf with a lambda predicate (Java 8+). */
    public static void removeShortStrings(ArrayList<String> strings, int minLength) {
        strings.removeIf(s -> s.length() < minLength);
    }

    // ===================================================================
    // 7. MAJORITY ELEMENT (Boyer–Moore voting algorithm)
    //
    // An element is a strict majority if it appears more than n/2 times.
    // Three approaches: brute force O(n²), HashMap O(n) with O(n) space,
    // and the elegant Boyer–Moore voting algorithm in O(n) time and O(1) space.
    // ===================================================================

    /** O(n²) — for each element, count its occurrences across the whole array. */
    public static boolean hasMajorityShlemiel(int[] items) {
        for (int candidate : items) {
            int edge = 0;
            for (int item : items) {
                edge += (item == candidate) ? +1 : -1;
            }
            if (edge > 0) { return true; }
        }
        return false;
    }

    /** O(n) time, O(n) space — count occurrences in a HashMap. */
    public static boolean hasMajorityHashMap(int[] items) {
        var counts = new HashMap<Integer, Integer>();
        for (int item : items) {
            counts.merge(item, 1, Integer::sum);
        }
        int threshold = items.length / 2;
        return counts.values().stream().anyMatch(count -> count > threshold);
    }

    /**
     * O(n) time, O(1) space — Boyer–Moore majority vote algorithm.
     * <p>
     * The insight: walk through the array maintaining a "candidate" and an
     * "edge" (lead count). Whenever the edge drops to zero, the current
     * candidate is abandoned and the next element becomes the new candidate.
     * If a majority element exists, it will be the final candidate — but we
     * must verify, because the algorithm can produce a false candidate when
     * no majority exists.
     */
    public static boolean hasMajorityBoyerMoore(int[] items) {
        // Phase 1: find the candidate.
        int candidate = 0;
        int edge = 0;
        for (int item : items) {
            if (edge == 0) {
                candidate = item;
                edge = 1;
            } else {
                edge += (item == candidate) ? +1 : -1;
            }
        }
        // Phase 2: verify the candidate actually has a majority.
        // Copy to a local variable — "candidate" is not effectively final
        // (it was reassigned in the loop), so a lambda cannot capture it.
        int finalCandidate = candidate;
        long candidateCount = Arrays.stream(items)
                .filter(item -> item == finalCandidate)
                .count();
        return candidateCount * 2 > items.length;
    }

    // ===================================================================
    // 8. STRING CONCATENATION IN A LOOP (bonus example)
    //
    // A classic Shlemiel trap: string += something in a loop creates a
    // new String object every iteration, copying all previous characters.
    // Total work: 1 + 2 + 3 + ... + n = O(n²).
    // StringBuilder.append is amortized O(1) per call, giving O(n) total.
    // ===================================================================

    /** O(n²) — each += copies the entire string built so far. */
    public static String buildStringShlemiel(int count) {
        String result = "";
        for (int i = 0; i < count; i++) {
            result += i + " "; // Allocates a new String every iteration!
        }
        return result;
    }

    /** O(n) — StringBuilder.append is amortized O(1). */
    public static String buildString(int count) {
        var builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(i).append(' ');
        }
        return builder.toString();
    }

    // ===================================================================
    // 9. FIRST UNIQUE CHARACTER (bonus example)
    //
    // Shlemiel checks every character against every other character.
    // The smart version counts frequencies in one pass using an array
    // (or HashMap for general objects), then scans once more.
    // ===================================================================

    /** O(n²) — for each character, scan ahead for duplicates. */
    public static int firstUniqueCharShlemiel(String text) {
        for (int i = 0; i < text.length(); i++) {
            boolean unique = true;
            for (int j = 0; j < text.length(); j++) {
                if (i != j && text.charAt(i) == text.charAt(j)) {
                    unique = false;
                    break;
                }
            }
            if (unique) { return i; }
        }
        return -1;
    }

    /** O(n) — two passes: count frequencies, then find the first with count 1. */
    public static int firstUniqueChar(String text) {
        // For ASCII/Latin text, a small array is faster than a HashMap.
        var frequency = new int[Character.MAX_VALUE + 1];
        for (int i = 0; i < text.length(); i++) {
            frequency[text.charAt(i)]++;
        }
        for (int i = 0; i < text.length(); i++) {
            if (frequency[text.charAt(i)] == 1) { return i; }
        }
        return -1;
    }

    // ===================================================================
    // Benchmarking utility
    // ===================================================================

    @FunctionalInterface
    private interface TimedTask {
        void run();
    }

    /** Run a task and return its elapsed time in milliseconds. */
    private static double timeMillis(TimedTask task) {
        long start = System.nanoTime();
        task.run();
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    /** Format a speedup ratio nicely. */
    private static String speedup(double slow, double fast) {
        if (fast < 0.001) { return "(too fast to measure)"; }
        return "%.1fx speedup".formatted(slow / fast);
    }

    // ===================================================================
    // Main — benchmark each Shlemiel vs. smart pair.
    // ===================================================================

    public static void main(String[] args) {
        var rng = new Random(42);

        // --- 1. Prefix sums ---
        System.out.println("=== 1. Prefix Sums ===");
        int[] data = rng.ints(50_000, 0, 100).toArray();
        double t1 = timeMillis(() -> prefixSumsShlemiel(data));
        double t2 = timeMillis(() -> prefixSums(data));
        System.out.printf("  Shlemiel O(n²): %.1f ms%n", t1);
        System.out.printf("  Smart O(n):     %.1f ms  %s%n%n", t2, speedup(t1, t2));

        // --- 2. Longest ascending run ---
        System.out.println("=== 2. Longest Ascending Run ===");
        int[] runData = rng.ints(100_000, 0, 1000).toArray();
        t1 = timeMillis(() -> longestAscendingShlemiel(runData));
        t2 = timeMillis(() -> longestAscending(runData));
        System.out.printf("  Shlemiel O(n²): %.1f ms%n", t1);
        System.out.printf("  Smart O(n):     %.1f ms  %s%n%n", t2, speedup(t1, t2));

        // --- 3. Two-sum ---
        System.out.println("=== 3. Two-Sum (sorted array) ===");
        int[] sorted = IntStream.range(0, 100_000).map(i -> i * 2).toArray();
        int target = sorted[99_998] + sorted[99_999]; // pair near the end
        t1 = timeMillis(() -> twoSumShlemiel(sorted, target));
        t2 = timeMillis(() -> twoSum(sorted, target));
        System.out.printf("  Shlemiel O(n²): %.1f ms%n", t1);
        System.out.printf("  Two-pointer O(n): %.1f ms  %s%n%n", t2, speedup(t1, t2));

        // --- 4. Polynomial evaluation ---
        System.out.println("=== 4. Polynomial Evaluation ===");
        double[] poly = rng.doubles(10_000, -1, 1).toArray();
        t1 = timeMillis(() -> { for (int i = 0; i < 10; i++) evalPolynomialShlemiel(poly, 1.0001); });
        t2 = timeMillis(() -> { for (int i = 0; i < 10; i++) evalPolynomialLinear(poly, 1.0001); });
        double t3 = timeMillis(() -> { for (int i = 0; i < 10; i++) evalPolynomialHorner(poly, 1.0001); });
        System.out.printf("  Shlemiel O(n²):    %.1f ms%n", t1);
        System.out.printf("  Running power O(n): %.1f ms  %s%n", t2, speedup(t1, t2));
        System.out.printf("  Horner O(n):        %.1f ms  %s%n%n", t3, speedup(t1, t3));

        // --- 5. Permutation check ---
        System.out.println("=== 5. Is Permutation of 1..n? ===");
        int permSize = 20_000;
        int[] perm = IntStream.rangeClosed(1, permSize).toArray();
        // Shuffle to make it a valid permutation in random order.
        for (int i = permSize - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = perm[i]; perm[i] = perm[j]; perm[j] = tmp;
        }
        t1 = timeMillis(() -> isPermutationShlemiel(perm, permSize));
        t2 = timeMillis(() -> isPermutationSorting(perm.clone(), permSize));
        t3 = timeMillis(() -> isPermutationLinear(perm, permSize));
        System.out.printf("  Shlemiel O(n²):     %.1f ms%n", t1);
        System.out.printf("  Sorting O(n log n): %.1f ms  %s%n", t2, speedup(t1, t2));
        System.out.printf("  Boolean array O(n): %.1f ms  %s%n%n", t3, speedup(t1, t3));

        // --- 6. Remove short strings ---
        System.out.println("=== 6. Remove Short Strings ===");
        int listSize = 50_000;
        ArrayList<String> words1 = new ArrayList<>(), words2 = new ArrayList<>(), words3 = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            String word = "x".repeat(rng.nextInt(10) + 1);
            words1.add(word); words2.add(word); words3.add(word);
        }
        t1 = timeMillis(() -> removeShortStringsShlemiel(words1, 5));
        t2 = timeMillis(() -> removeShortStringsBuffer(words2, 5));
        t3 = timeMillis(() -> removeShortStrings(words3, 5));
        System.out.printf("  Shlemiel O(n²):   %.1f ms%n", t1);
        System.out.printf("  Buffer O(n):      %.1f ms  %s%n", t2, speedup(t1, t2));
        System.out.printf("  removeIf O(n):    %.1f ms  %s%n%n", t3, speedup(t1, t3));

        // --- 7. Majority element ---
        System.out.println("=== 7. Majority Element ===");
        int majSize = 30_000;
        int[] majority = new int[majSize];
        for (int i = 0; i < majSize; i++) {
            majority[i] = (i < majSize / 2 + 1) ? 42 : rng.nextInt(1000);
        }
        t1 = timeMillis(() -> hasMajorityShlemiel(majority));
        t2 = timeMillis(() -> hasMajorityHashMap(majority));
        t3 = timeMillis(() -> hasMajorityBoyerMoore(majority));
        System.out.printf("  Shlemiel O(n²):     %.1f ms%n", t1);
        System.out.printf("  HashMap O(n):       %.1f ms  %s%n", t2, speedup(t1, t2));
        System.out.printf("  Boyer-Moore O(n):   %.1f ms  %s%n%n", t3, speedup(t1, t3));

        // --- 8. String concatenation ---
        System.out.println("=== 8. String Concatenation ===");
        int concatSize = 50_000;
        t1 = timeMillis(() -> buildStringShlemiel(concatSize));
        t2 = timeMillis(() -> buildString(concatSize));
        System.out.printf("  Shlemiel += O(n²):     %.1f ms%n", t1);
        System.out.printf("  StringBuilder O(n):    %.1f ms  %s%n%n", t2, speedup(t1, t2));

        // --- 9. First unique character ---
        System.out.println("=== 9. First Unique Character ===");
        var sb = new StringBuilder();
        for (int i = 0; i < 50_000; i++) { sb.append((char)('a' + rng.nextInt(26))); }
        String text = sb.toString();
        t1 = timeMillis(() -> firstUniqueCharShlemiel(text));
        t2 = timeMillis(() -> firstUniqueChar(text));
        System.out.printf("  Shlemiel O(n²):    %.1f ms%n", t1);
        System.out.printf("  Frequency O(n):    %.1f ms  %s%n", t2, speedup(t1, t2));
    }
}