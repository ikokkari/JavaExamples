import java.util.Arrays;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

/**
 * A first batch of array algorithms demonstrating loops, two-pointer
 * techniques, and functional predicates.
 * <p>
 * Updated for Java 21+ with {@link IntPredicate} for flexible filtering,
 * streams where they improve clarity, and better naming throughout.
 *
 * @author Ilkka Kokkarinen
 */
public class FirstBatch {

    // -----------------------------------------------------------------------
    // Counting and collecting elements that satisfy a predicate.
    // Demonstrates: IntPredicate (functional interface), higher-order methods.
    //
    // The old version hardcoded an "isFunny" test. The modern version accepts
    // any IntPredicate, so callers choose the criterion:
    //
    //   countMatching(a, n -> n % 3 == 0)       // divisible by 3
    //   countMatching(a, n -> isPrime(n))        // primes
    //   countMatching(a, FirstBatch::isPrime)    // same, method reference
    // -----------------------------------------------------------------------

    /**
     * Count how many elements in the array satisfy the given predicate.
     *
     * @param values    the array to examine
     * @param predicate the condition to test each element against
     * @return the count of matching elements
     */
    public static int countMatching(int[] values, IntPredicate predicate) {
        int count = 0;
        for (int value : values) {
            if (predicate.test(value)) { count++; }
        }
        return count;
    }

    /**
     * Collect all elements satisfying the predicate into a new array.
     *
     * @param values    the array to filter
     * @param predicate the condition to test each element against
     * @return a new array containing only the matching elements, in order
     */
    public static int[] collectMatching(int[] values, IntPredicate predicate) {
        // With streams, this is a clean one-liner. Under the hood it does
        // essentially the same two-pass (or buffer-and-trim) work.
        return Arrays.stream(values)
                .filter(predicate)
                .toArray();
    }

    // A sample predicate for demonstration purposes.
    public static boolean isDivisibleByThree(int n) {
        return n % 3 == 0;
    }

    // -----------------------------------------------------------------------
    // Selection sort — our first (and worst) sorting algorithm.
    // Demonstrates: nested loops, in-place swapping, finding a minimum.
    // -----------------------------------------------------------------------

    /**
     * Find the index of the smallest element in {@code array[from..to]}
     * (inclusive on both ends).
     */
    private static int indexOfMinimum(int[] array, int from, int to) {
        int minIndex = from;
        for (int i = from + 1; i <= to; i++) {
            if (array[i] < array[minIndex]) { minIndex = i; }
        }
        return minIndex;
    }

    /**
     * Sort the array in place using selection sort. Time complexity is
     * O(n²) regardless of input — there are far better algorithms, but
     * this one is easy to understand and prove correct.
     *
     * @param array the array to sort
     */
    public static void selectionSort(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            int minIndex = indexOfMinimum(array, i, array.length - 1);
            // Swap array[i] with the smallest remaining element.
            int temp = array[i];
            array[i] = array[minIndex];
            array[minIndex] = temp;
        }
    }

    // -----------------------------------------------------------------------
    // Three two-pointer array algorithms of similar spirit.
    // Each walks through one or two sorted arrays in a single pass.
    // -----------------------------------------------------------------------

    /**
     * Interleave elements of two arrays: take one from {@code first}, one
     * from {@code second}, alternating. When one runs out, append the rest
     * of the other. (This is sometimes called a "riffle" or "zip".)
     *
     * @param first  the first array
     * @param second the second array
     * @return the interleaved result
     */
    public static int[] zip(int[] first, int[] second) {
        var result = new int[first.length + second.length];
        int i = 0, dest = 0;
        while (i < first.length && i < second.length) {
            result[dest++] = first[i];
            result[dest++] = second[i];
            i++;
        }
        while (i < first.length)  { result[dest++] = first[i++]; }
        while (i < second.length) { result[dest++] = second[i++]; }
        return result;
    }

    /**
     * Merge two sorted arrays into one sorted array. Both inputs must
     * already be sorted in ascending order. This is the key subroutine
     * of merge sort.
     *
     * @param first  the first sorted array
     * @param second the second sorted array
     * @return a new sorted array containing all elements of both inputs
     */
    public static int[] merge(int[] first, int[] second) {
        var result = new int[first.length + second.length];
        int i = 0, j = 0, dest = 0;
        while (i < first.length && j < second.length) {
            if (first[i] <= second[j]) { result[dest++] = first[i++]; }
            else                       { result[dest++] = second[j++]; }
        }
        while (i < first.length)  { result[dest++] = first[i++]; }
        while (j < second.length) { result[dest++] = second[j++]; }
        return result;
    }

    /**
     * Compute the intersection of two sorted arrays — elements that appear
     * in both. Both inputs must be sorted in ascending order. The result
     * preserves duplicates only if they appear in both arrays.
     *
     * @param first  the first sorted array
     * @param second the second sorted array
     * @return a new sorted array of common elements
     */
    public static int[] intersection(int[] first, int[] second) {
        // IntStream.Builder avoids the two-pass approach (count then fill)
        // and the boxing overhead of ArrayList<Integer>.
        var builder = IntStream.builder();
        int i = 0, j = 0;
        while (i < first.length && j < second.length) {
            if (first[i] < second[j])      { i++; }
            else if (first[i] > second[j]) { j++; }
            else {
                builder.add(first[i]);
                i++;
                j++;
            }
        }
        return builder.build().toArray();
    }

    // -----------------------------------------------------------------------
    // Remove consecutive duplicates from a sorted array.
    // -----------------------------------------------------------------------

    /**
     * Return a new array with consecutive duplicates removed. The input
     * must be sorted so that equal elements are adjacent.
     *
     * @param sorted the sorted input array
     * @return a new array with duplicates removed
     */
    public static int[] removeDuplicates(int[] sorted) {
        if (sorted.length == 0) { return sorted; }
        // IntStream with a stateful filter is one option, but the explicit
        // approach is clearer and avoids concerns about stream ordering.
        // Single pass: write each element that differs from its predecessor.
        var result = new int[sorted.length]; // upper bound on size
        result[0] = sorted[0];
        int dest = 1;
        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i] != sorted[i - 1]) {
                result[dest++] = sorted[i];
            }
        }
        return Arrays.copyOf(result, dest);
    }

    // -----------------------------------------------------------------------
    // Longest ascending run — a one-pass algorithm with state variables.
    // -----------------------------------------------------------------------

    /**
     * Find the length of the longest strictly ascending run in the array.
     * Demonstrates the pattern of maintaining "current" and "best so far"
     * state variables that are updated at each step.
     *
     * @param values the array to examine
     * @return the length of the longest ascending run (at least 1 if non-empty)
     */
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

    // -----------------------------------------------------------------------
    // Roman numeral encoding and decoding.
    // -----------------------------------------------------------------------

    // Parallel arrays: each Roman symbol paired with its decimal value.
    // Ordered from largest to smallest for the greedy encoding algorithm.
    private static final String[] ROMAN_SYMBOLS = {
            "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
    };
    private static final int[] ROMAN_VALUES = {
            1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
    };

    /**
     * Encode a positive integer as a Roman numeral string.
     * Uses a greedy algorithm: repeatedly subtract the largest possible
     * Roman value and append its symbol.
     *
     * @param number the positive integer to encode (must be ≥ 1)
     * @return the Roman numeral representation
     * @throws IllegalArgumentException if number is less than 1
     */
    public static String romanEncode(int number) {
        if (number < 1) {
            throw new IllegalArgumentException("Cannot convert " + number + " to Roman");
        }
        var result = new StringBuilder();
        int symbolIndex = 0;
        while (number > 0) {
            while (number >= ROMAN_VALUES[symbolIndex]) {
                result.append(ROMAN_SYMBOLS[symbolIndex]);
                number -= ROMAN_VALUES[symbolIndex];
            }
            symbolIndex++;
        }
        return result.toString();
    }

    // For decoding, we only need the seven basic symbols and their values.
    private static final String BASIC_SYMBOLS = "MDCLXVI";
    private static final int[] BASIC_VALUES = {1000, 500, 100, 50, 10, 5, 1};

    /**
     * Decode a Roman numeral string to its integer value. Reads right to left:
     * if a symbol's value is less than the previous one, it is subtracted
     * (this handles cases like IV = 4 and IX = 9).
     *
     * @param roman the Roman numeral string (case-insensitive)
     * @return the decoded integer value
     * @throws IllegalArgumentException if the string contains invalid characters
     */
    public static int romanDecode(String roman) {
        roman = roman.toUpperCase();
        int result = 0;
        int previousValue = 0;
        // Process right to left so we can detect subtractive notation.
        for (int i = roman.length() - 1; i >= 0; i--) {
            int symbolIndex = BASIC_SYMBOLS.indexOf(roman.charAt(i));
            if (symbolIndex == -1) {
                throw new IllegalArgumentException(
                        "Illegal Roman numeral character: " + roman.charAt(i));
            }
            int currentValue = BASIC_VALUES[symbolIndex];
            // Subtractive rule: IV means 5 - 1, not 5 + 1.
            result += (currentValue < previousValue) ? -currentValue : currentValue;
            previousValue = currentValue;
        }
        return result;
    }

    /**
     * Verify that encoding and decoding are perfect inverses for 1..4999.
     */
    public static boolean testRomanConversions() {
        return IntStream.rangeClosed(1, 4999)
                .allMatch(n -> romanDecode(romanEncode(n)) == n);
    }

    // -----------------------------------------------------------------------
    // Main method — exercise each example and display results.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        // --- Predicate-based counting and collecting ---
        System.out.println("--- countMatching / collectMatching ---");
        int[] data = {3, 7, 9, 12, 15, 20, 21, 25, 30};
        System.out.printf("Data: %s%n", Arrays.toString(data));

        // Pass different predicates to the same method:
        IntPredicate divBy3 = FirstBatch::isDivisibleByThree;
        IntPredicate isEven = n -> n % 2 == 0;
        IntPredicate over15 = n -> n > 15;

        System.out.printf("Divisible by 3: count=%d, values=%s%n",
                countMatching(data, divBy3), Arrays.toString(collectMatching(data, divBy3)));
        System.out.printf("Even:           count=%d, values=%s%n",
                countMatching(data, isEven), Arrays.toString(collectMatching(data, isEven)));
        System.out.printf("Greater than 15: count=%d, values=%s%n",
                countMatching(data, over15), Arrays.toString(collectMatching(data, over15)));

        // Predicates can also be composed with and/or/negate:
        IntPredicate divBy3AndEven = divBy3.and(isEven);
        System.out.printf("Div by 3 AND even: %s%n%n",
                Arrays.toString(collectMatching(data, divBy3AndEven)));

        // --- Selection sort ---
        System.out.println("--- selectionSort ---");
        int[] unsorted = {38, 27, 43, 3, 9, 82, 10};
        System.out.printf("Before: %s%n", Arrays.toString(unsorted));
        selectionSort(unsorted);
        System.out.printf("After:  %s%n%n", Arrays.toString(unsorted));

        // --- Zip, merge, intersection ---
        System.out.println("--- zip / merge / intersection ---");
        int[] left  = {1, 3, 5, 7, 9};
        int[] right = {2, 4, 6};
        System.out.printf("zip(%s, %s) = %s%n",
                Arrays.toString(left), Arrays.toString(right),
                Arrays.toString(zip(left, right)));
        System.out.printf("merge(%s, %s) = %s%n",
                Arrays.toString(left), Arrays.toString(right),
                Arrays.toString(merge(left, right)));
        int[] sorted1 = {1, 3, 5, 7, 9, 11};
        int[] sorted2 = {2, 3, 5, 8, 9, 13};
        System.out.printf("intersection(%s, %s) = %s%n%n",
                Arrays.toString(sorted1), Arrays.toString(sorted2),
                Arrays.toString(intersection(sorted1, sorted2)));

        // --- Remove duplicates ---
        System.out.println("--- removeDuplicates ---");
        int[] withDups = {1, 1, 2, 3, 3, 3, 4, 5, 5};
        System.out.printf("removeDuplicates(%s) = %s%n%n",
                Arrays.toString(withDups), Arrays.toString(removeDuplicates(withDups)));

        // --- Longest ascending run ---
        System.out.println("--- longestAscending ---");
        int[] sequence = {3, 5, 7, 2, 4, 6, 8, 10, 1};
        System.out.printf("longestAscending(%s) = %d%n%n",
                Arrays.toString(sequence), longestAscending(sequence));

        // --- Roman numerals ---
        System.out.println("--- Roman numerals ---");
        for (int n : new int[]{1, 4, 9, 42, 1999, 2025, 3888}) {
            String roman = romanEncode(n);
            int decoded = romanDecode(roman);
            System.out.printf("%4d → %-15s → %d%n", n, roman, decoded);
        }
        System.out.printf("%nRoundtrip test (1..4999): %s%n", testRomanConversions());
    }
}