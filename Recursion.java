import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Recursion examples for CCPS 209, modernized for Java 21+.
 * <p>
 * The examples progress from simple structural recursion to backtracking
 * and recursive descent parsing — situations where recursion is genuinely
 * the cleanest (or only practical) approach.
 *
 * @author Ilkka Kokkarinen
 */
public class Recursion {

    // -----------------------------------------------------------------------
    // 1. THE FUNDAMENTAL INSIGHT: where you recurse determines the order.
    //    Three ways to traverse a range — the only difference is whether
    //    the work happens before, after, or around the recursive call.
    // -----------------------------------------------------------------------

    /**
     * Print integers from {@code lo} to {@code hi} in ascending order.
     * The print happens <em>before</em> the recursive call (pre-order).
     */
    public static void printAscending(int lo, int hi) {
        if (lo > hi) { return; }
        System.out.println(lo);
        printAscending(lo + 1, hi);
    }

    /**
     * Print integers from {@code lo} to {@code hi} in descending order
     * <em>without ever computing hi − 1</em>. The print happens
     * <em>after</em> the recursive call (post-order).
     */
    public static void printDescending(int lo, int hi) {
        if (lo > hi) { return; }
        printDescending(lo + 1, hi);
        System.out.println(lo);
    }

    /**
     * Print integers from {@code lo} to {@code hi} in ascending order
     * via divide-and-conquer (in-order traversal of the implicit tree).
     * Runs in O(log n) stack depth instead of O(n).
     */
    public static void printDivideAndConquer(int lo, int hi) {
        if (lo > hi) { return; }
        int mid = lo + (hi - lo) / 2; // overflow-safe midpoint
        printDivideAndConquer(lo, mid - 1);
        System.out.println(mid);
        printDivideAndConquer(mid + 1, hi);
    }

    // -----------------------------------------------------------------------
    // 2. DIGIT MANIPULATION — recursion on the structure of a number.
    // -----------------------------------------------------------------------

    /**
     * Sum the digits of {@code n} using only integer arithmetic.
     *
     * @param n any integer (negative values are handled)
     * @return the sum of the decimal digits of |n|
     */
    public static int sumOfDigits(int n) {
        if (n < 0) { return sumOfDigits(-n); }
        if (n < 10) { return n; }
        return n % 10 + sumOfDigits(n / 10);
    }

    /**
     * Reverse the decimal digits of {@code n} using tail recursion.
     * For example, {@code reverseDigits(12345) == 54321}.
     *
     * @param n a non-negative integer
     * @return the integer formed by reversing the digits of n
     */
    public static int reverseDigits(int n) {
        return reverseDigits(n, 0);
    }

    private static int reverseDigits(int n, int acc) {
        if (n == 0) { return acc; }
        return reverseDigits(n / 10, 10 * acc + n % 10);
    }

    // -----------------------------------------------------------------------
    // 3. EXPONENTIATION BY SQUARING — O(log b) multiplications.
    //    The key insight: a^b = (a^(b/2))^2 when b is even.
    // -----------------------------------------------------------------------

    /**
     * Compute {@code a} raised to the power {@code b} using repeated
     * squaring. Runs in O(log |b|) multiplications.
     *
     * @param a the base
     * @param b the exponent (may be negative)
     * @return a<sup>b</sup>
     */
    public static double power(double a, int b) {
        if (b == 0) { return 1.0; }
        if (b < 0) { return 1.0 / power(a, -b); }
        double half = power(a, b / 2);
        return (b % 2 == 0) ? half * half : a * half * half;
    }

    // -----------------------------------------------------------------------
    // 4. MERGE SORT — the textbook case where divide-and-conquer wins.
    //    Unlike finding the minimum of an array (which you can do in one
    //    pass), sorting genuinely benefits from splitting the problem.
    // -----------------------------------------------------------------------

    /**
     * Sort the subarray {@code a[lo..hi]} using merge sort.
     * Allocates a temporary array for the merge step.
     *
     * @param a  the array to sort (modified in place)
     * @param lo the first index of the subarray (inclusive)
     * @param hi the last index of the subarray (inclusive)
     */
    public static void mergeSort(int[] a, int lo, int hi) {
        if (lo >= hi) { return; }
        int mid = lo + (hi - lo) / 2;
        mergeSort(a, lo, mid);
        mergeSort(a, mid + 1, hi);
        merge(a, lo, mid, hi);
    }

    private static void merge(int[] a, int lo, int mid, int hi) {
        // If the two halves are already in order, skip the merge.
        if (a[mid] <= a[mid + 1]) { return; }
        int[] temp = Arrays.copyOfRange(a, lo, mid + 1);
        int i = 0, j = mid + 1, k = lo;
        while (i < temp.length && j <= hi) {
            a[k++] = (temp[i] <= a[j]) ? temp[i++] : a[j++];
        }
        // Copy any remaining elements from the left half.
        while (i < temp.length) {
            a[k++] = temp[i++];
        }
        // Right half elements are already in place — no copy needed.
    }

    // -----------------------------------------------------------------------
    // 5. SUBSET SUM — the classic "include or exclude" recursion.
    //    This is where recursion feels inevitable: the problem structure
    //    is a binary tree of choices, and recursion mirrors it exactly.
    // -----------------------------------------------------------------------

    /**
     * Determine whether some subset of the first {@code n} elements
     * of {@code a} sums to exactly {@code goal}.
     *
     * @param a    the array of available values
     * @param goal the target sum
     * @param n    how many elements (from the front) to consider
     * @return true if such a subset exists
     */
    public static boolean subsetSum(int[] a, int goal, int n) {
        if (goal == 0) { return true; }
        if (n == 0) { return false; }
        // Either include a[n-1] in the subset, or don't.
        return subsetSum(a, goal - a[n - 1], n - 1)
            || subsetSum(a, goal, n - 1);
    }

    /**
     * Count how many subsets of the first {@code n} elements of {@code a}
     * sum to exactly {@code goal}.
     */
    public static int subsetSumCount(int[] a, int goal, int n) {
        if (goal == 0) { return 1; }
        if (n == 0) { return 0; }
        return subsetSumCount(a, goal - a[n - 1], n - 1)
             + subsetSumCount(a, goal, n - 1);
    }

    // -----------------------------------------------------------------------
    // 6. PERMUTATION GENERATION — backtracking that's actually useful.
    //    Generates all n! orderings of a list. The pattern generalizes
    //    to any problem where you build a solution by making a sequence
    //    of constrained choices (scheduling, anagrams, Sudoku, etc.).
    // -----------------------------------------------------------------------

    /**
     * Return all permutations of the given list.
     *
     * @param items the elements to permute
     * @param <T>   the element type
     * @return a list of all permutations, each as a new list
     */
    public static <T> List<List<T>> permutations(List<T> items) {
        var results = new ArrayList<List<T>>();
        permuteHelper(new ArrayList<>(items), 0, results);
        return results;
    }

    private static <T> void permuteHelper(List<T> items, int start,
                                           List<List<T>> results) {
        if (start == items.size()) {
            results.add(List.copyOf(items)); // snapshot the current order
            return;
        }
        for (int i = start; i < items.size(); i++) {
            swap(items, start, i);
            permuteHelper(items, start + 1, results);
            swap(items, start, i);  // undo — the backtracking step
        }
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

    // -----------------------------------------------------------------------
    // 7. TOWERS OF HANOI — compact, elegant, and every CS student
    //    should see it at least once. The recursive structure is:
    //    move n-1 disks out of the way, move the big one, then move
    //    n-1 disks back on top.
    // -----------------------------------------------------------------------

    /**
     * Print the moves to solve the Towers of Hanoi for {@code n} disks.
     *
     * @param n    the number of disks
     * @param from the source peg name
     * @param to   the destination peg name
     * @param via  the auxiliary peg name
     */
    public static void hanoi(int n, String from, String to, String via) {
        if (n == 0) { return; }
        hanoi(n - 1, from, via, to);
        System.out.printf("Move disk %d: %s -> %s%n", n, from, to);
        hanoi(n - 1, via, to, from);
    }

    // -----------------------------------------------------------------------
    // 8. N-QUEENS — the canonical backtracking problem.
    //    Place n queens on an n×n board so that no two attack each other.
    //    Uses boolean arrays for columns and both diagonals to check
    //    conflicts in O(1). Includes mirror-symmetry optimization
    //    and forward checking on the first available row.
    // -----------------------------------------------------------------------

    /**
     * Count the number of solutions to the n-queens problem and print
     * statistics about the search tree.
     *
     * @param n the board size and number of queens
     * @return the number of distinct solutions
     */
    public static int nQueens(int n) {
        var stats = new SearchStats(n);
        int result = nQueens(n, new boolean[n], new boolean[2 * n],
                             new boolean[2 * n], stats);
        stats.print();
        return result;
    }

    /**
     * Internal bookkeeping for the n-queens search. A record is the
     * natural choice: it's just a named tuple for the counters.
     */
    private record SearchStats(int[] callsAtLevel, int[] cutoffsAtLevel) {
        SearchStats(int n) {
            this(new int[n + 1], new int[n + 1]);
        }

        void print() {
            int total = 0;
            System.out.println("Calls per level:");
            for (int i = callsAtLevel.length - 1; i >= 0; i--) {
                System.out.printf("  Level %2d: %,10d calls, %,10d cutoffs%n",
                        i, callsAtLevel[i], cutoffsAtLevel[i]);
                total += callsAtLevel[i];
            }
            System.out.printf("Total calls: %,d%n", total);
        }
    }

    /**
     * Can the given {@code row} still accept a queen, given the columns
     * and diagonals already claimed? Used for forward checking.
     */
    private static boolean rowHasOpening(int row, boolean[] cols,
                                          boolean[] ne, boolean[] se) {
        int n = cols.length;
        for (int col = 0; col < n; col++) {
            if (!cols[col] && !ne[n - row + col] && !se[row + col]) {
                return true;
            }
        }
        return false;
    }

    private static int nQueens(int remaining, boolean[] cols,
                                boolean[] ne, boolean[] se,
                                SearchStats stats) {
        stats.callsAtLevel[remaining]++;
        if (remaining == 0) { return 1; }

        int n = cols.length;
        int row = remaining - 1;
        int count = 0;

        // On the first row, exploit left-right mirror symmetry:
        // only try columns in the left half, then double the count.
        int colLimit = (remaining == n)
                ? (n + 1) / 2   // ceil(n/2) columns
                : n;

        for (int col = 0; col < colLimit; col++) {
            if (cols[col] || ne[n - row + col] || se[row + col]) {
                continue;
            }

            // Place queen at (row, col).
            cols[col] = true;
            ne[n - row + col] = true;
            se[row + col] = true;

            // Forward checking: does the next row still have a legal square?
            if (remaining == 1 || rowHasOpening(row - 1, cols, ne, se)) {
                int ways = nQueens(remaining - 1, cols, ne, se, stats);
                // Double the count for the symmetric first-row placements.
                boolean isFirstRowLeftHalf = (remaining == n) && (col < n / 2);
                count += isFirstRowLeftHalf ? 2 * ways : ways;
            } else {
                stats.cutoffsAtLevel[remaining]++;
            }

            // Undo placement before trying the next column.
            cols[col] = false;
            ne[n - row + col] = false;
            se[row + col] = false;
        }
        return count;
    }

    // -----------------------------------------------------------------------
    // 9. RECURSIVE DESCENT PARSER — where recursion isn't just convenient,
    //    it's the natural and essentially only clean solution.
    //    Parses and evaluates arithmetic expressions like "3 + 4 * (2 - 1)".
    //
    //    Grammar (in order of increasing precedence):
    //      expression  = term (('+' | '-') term)*
    //      term        = factor (('*' | '/') factor)*
    //      factor      = '-' factor | '(' expression ')' | NUMBER
    // -----------------------------------------------------------------------

    /**
     * Evaluate an arithmetic expression given as a string.
     * Supports {@code +}, {@code -}, {@code *}, {@code /}, unary minus,
     * and parentheses with the standard precedence rules.
     *
     * @param input the expression to evaluate, e.g. {@code "3 + 4 * 2"}
     * @return the result of evaluating the expression
     * @throws IllegalArgumentException if the expression is malformed
     */
    public static double evaluate(String input) {
        var parser = new ExpressionParser(input);
        double result = parser.parseExpression();
        if (parser.hasMore()) {
            throw new IllegalArgumentException(
                "Unexpected character at position " + parser.pos
                + ": '" + input.charAt(parser.pos) + "'"
            );
        }
        return result;
    }

    /**
     * A tiny recursive descent parser. Packaged as a static inner class
     * so the parsing state (input string + current position) lives in
     * one place rather than being threaded through every call.
     */
    private static final class ExpressionParser {
        private final String input;
        private int pos;

        ExpressionParser(String input) {
            this.input = input;
            this.pos = 0;
        }

        boolean hasMore() {
            skipSpaces();
            return pos < input.length();
        }

        // expression = term (('+' | '-') term)*
        double parseExpression() {
            double value = parseTerm();
            while (hasMore()) {
                char op = input.charAt(pos);
                if (op != '+' && op != '-') { break; }
                pos++;
                double right = parseTerm();
                value = (op == '+') ? value + right : value - right;
            }
            return value;
        }

        // term = factor (('*' | '/') factor)*
        private double parseTerm() {
            double value = parseFactor();
            while (hasMore()) {
                char op = input.charAt(pos);
                if (op != '*' && op != '/') { break; }
                pos++;
                double right = parseFactor();
                value = (op == '*') ? value * right : value / right;
            }
            return value;
        }

        // factor = '-' factor | '(' expression ')' | NUMBER
        private double parseFactor() {
            skipSpaces();
            if (pos >= input.length()) {
                throw new IllegalArgumentException(
                    "Unexpected end of expression");
            }
            char ch = input.charAt(pos);

            // Unary minus.
            if (ch == '-') {
                pos++;
                return -parseFactor();
            }

            // Parenthesized sub-expression — and here the recursion
            // back to parseExpression() is what makes the whole thing work.
            if (ch == '(') {
                pos++;
                double value = parseExpression();
                skipSpaces();
                if (pos >= input.length() || input.charAt(pos) != ')') {
                    throw new IllegalArgumentException(
                        "Missing closing parenthesis");
                }
                pos++;
                return value;
            }

            // Number literal (integer or decimal).
            if (Character.isDigit(ch) || ch == '.') {
                int start = pos;
                while (pos < input.length()
                       && (Character.isDigit(input.charAt(pos))
                           || input.charAt(pos) == '.')) {
                    pos++;
                }
                return Double.parseDouble(input.substring(start, pos));
            }

            throw new IllegalArgumentException(
                "Unexpected character at position " + pos + ": '" + ch + "'");
        }

        private void skipSpaces() {
            while (pos < input.length() && input.charAt(pos) == ' ') {
                pos++;
            }
        }
    }

    // -----------------------------------------------------------------------
    // 10. THE ANTI-PATTERN: mutable shared state in recursion.
    //     Shown deliberately as a "don't do this" example so students
    //     understand why every example above passes state through
    //     parameters and return values instead.
    // -----------------------------------------------------------------------

    private double total = 0.0; // Shared field — just say no!

    /**
     * Compute the harmonic sum 1 + 1/2 + 1/3 + ... + 1/n.
     * <p>
     * <strong>This is intentionally bad code.</strong> It mutates a field
     * instead of using the return value, so: (1) you must remember to
     * reset {@code total} between calls, and (2) it is not thread-safe —
     * two threads calling this simultaneously will corrupt each other's
     * results. See the correct version below.
     */
    public double harmonicSumBad(int n) {
        if (n < 1) { return total; }
        total += 1.0 / n;
        return harmonicSumBad(n - 1);
    }

    /**
     * Compute the harmonic sum correctly: no shared state, re-entrant,
     * and obviously correct by structural induction.
     *
     * @param n the number of terms
     * @return 1 + 1/2 + ... + 1/n, or 0 if n &lt; 1
     */
    public static double harmonicSum(int n) {
        if (n < 1) { return 0.0; }
        return 1.0 / n + harmonicSum(n - 1);
    }

    // -----------------------------------------------------------------------
    // Quick demo so students can run the file directly.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== Digit operations ===");
        System.out.println("sumOfDigits(12345) = " + sumOfDigits(12345));
        System.out.println("reverseDigits(12345) = " + reverseDigits(12345));

        System.out.println("\n=== Power ===");
        System.out.println("power(2, 10) = " + power(2, 10));
        System.out.println("power(2, -3) = " + power(2, -3));

        System.out.println("\n=== Merge sort ===");
        int[] data = {5, 3, 8, 1, 9, 2, 7, 4, 6};
        System.out.println("Before: " + Arrays.toString(data));
        mergeSort(data, 0, data.length - 1);
        System.out.println("After:  " + Arrays.toString(data));

        System.out.println("\n=== Subset sum ===");
        int[] values = {3, 7, 1, 8, 4};
        System.out.println("Can make 12 from {3,7,1,8,4}? "
                + subsetSum(values, 12, values.length));
        System.out.println("Ways to make 12: "
                + subsetSumCount(values, 12, values.length));

        System.out.println("\n=== Permutations of [A, B, C] ===");
        permutations(List.of("A", "B", "C")).forEach(System.out::println);

        System.out.println("\n=== Towers of Hanoi (3 disks) ===");
        hanoi(3, "left", "right", "middle");

        System.out.println("\n=== Expression parser ===");
        String expr = "3 + 4 * (2 - 1) / 0.5";
        System.out.printf("'%s' = %.2f%n", expr, evaluate(expr));

        System.out.println("\n=== Harmonic sum ===");
        System.out.printf("H(10) = %.6f%n", harmonicSum(10));

        System.out.println("\n=== 8-Queens ===");
        System.out.println("Solutions: " + nQueens(8));
    }
}
