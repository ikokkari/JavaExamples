public class Recursion {

    /**
     * Output the numbers in given integer range.
     * @param start The start value of the range.
     * @param end The end value of the range.
     */
    public void printNumbers(int start, int end) {
        if(start > end) { return; }
        System.out.println(start);
        printNumbers(start+1, end);
    }

    /**
     * Output the numbers in given integer range.
     * @param start The start value of the range.
     * @param end The end value of the range.
     */
    public void printNumbersOtherWay(int start, int end) {
        if(start > end) { return; }
        int mid = (start + end) / 2;
        printNumbersOtherWay(start, mid - 1);
        System.out.println(mid);
        printNumbersOtherWay(mid + 1, end);
    }

    /**
     * Output the numbers in given integer range in descending order.
     * @param start The start value of the range.
     * @param end The end value of the range.
     */
    public void printNumbersReverse(int start, int end) {
        if(start > end) { return; }
        printNumbersReverse(start+1, end);
        System.out.println(start);
    }

    /**
     * Find the smallest element in the given subarray.
     * @param a The array to search the smallest element in.
     * @param start The start index of the subarray.
     * @param end The end index of the subarray.
     */
    public int min(int[] a, int start, int end) {
        if(start == end) { return a[start]; }
        int mid = (start + end) / 2;
        int mleft = min(a, start, mid);
        int mright = min(a, mid + 1, end);
        return Math.min(mleft, mright);
    }

    /**
     * Compute {@code a} raised to the power of {@code b}.
     * @param a The base of the exponentiation.
     * @param b The exponent.
     * @return {@code a} raised to the power of {@code b}.
     */
    public double power(double a, int b) {
        if(b == 0) { return 1.0; }
        if(b < 0) { return 1.0 / power(a, -b); }
        if(b % 2 == 0) { return power(a * a, b / 2); }
        return a * power(a, b - 1);
    }

    private double total = 0.0; // Shared fields outside recursion... just say no!

    /**
     * Compute the harmonic sum of the first {@code n} integers. This solution is made
     * intentionally bad to illustrate an important point about recursion.
     * @param n The number of terms in the harmonic sum.
     * @return The harmonic sum up to the term {@code n}.
     */
    public double harmonicSum(int n) {
        if(n < 1) { return total; }
        total += 1.0 / n;
        return harmonicSum(n - 1);
    }
    // You need to remember to set such field back to zero between each call, and even
    // then this does not work in multithreaded programming (see 209) where multiple
    // execution threads can be calling this method simultaneously, since using fields
    // for which only one copy exists makes the method not re-entrant.

    /**
     * Using only integer arithmetic, compute the sum of digits of parameter {@code n}.
     * @param n The integers whose digits we sum.
     * @return The sum of digits of the parameter integer.
     */
    public int sumOfDigits(int n) {
        if(n < 0) { return sumOfDigits(-n); }
        if(n < 10) { return n; }
        else { return n % 10 + sumOfDigits(n / 10); }
    }

    /**
     * Using only integer arithmetic, reverse the digits of parameter {@code n}.
     * For example, 12345 would become 54321.
     * @param n The integers whose digits we reverse.
     * @return The number constructed from reversing the digits of the original number.
     */
    public int reverseDigits(int n, int acc) {
        if(n < 1) { return acc; }
        else return reverseDigits(n / 10, 10 * acc + n % 10);
    }

    /**
     * Given an integer array and a goal value, determine whether there exists a subset
     * of the first {@code n} elements in that array that add up exactly to the goal.
     * @param a The array to search the subset in.
     * @param goal The goal value to fulfill.
     * @param n The length of the array prefix that we are constrained into.
     * @return {@code true} if such a subset exists, {@code false} otherwise.
     */
    public boolean subsetSum(int[] a, int goal, int n) {
        if(goal == 0) { return true; }
        if(n == 0) { return false; }
        return subsetSum(a, goal - a[n-1], n - 1) || subsetSum(a, goal, n - 1);
    }
    
    /**
     * Given an integer array and a goal value, count how many subsets of the first
     * {@code n} elements in that array exist whose elements add up exactly to the goal.
     * @param a The array to search the subset in.
     * @param goal The goal value to fulfill.
     * @param n The length of the array prefix that we are constrained into.
     * @return The number of such subsets in the first {@code n} elements in the array.
     */
    public int subsetSumCount(int[] a, int goal, int n) {
        if(goal == 0) { return 1; }
        if(n == 0) { return 0; }
        return subsetSumCount(a, goal - a[n-1], n - 1) + subsetSumCount(a, goal, n - 1);
    }

    /**
     * The classic n-queens problem. How many ways can you place {@code n} chess queens
     * on an n*n chessboard so that no two queens are on the same row, column or diagonal?
     * The solution is the first backtracking algorithm that you must teach by law.
     * @param n The number of queens to place.
     * @return The count of possible ways.
     */
    public int nQueens(int n) {
        queenLevelCount = new int[n + 1];
        forwardCheckingCutoffCount = new int[n + 1];
        int result = nQueens(n, new boolean[n], new boolean[2*n], new boolean[2*n]);
        System.out.println("Calls per level:");
        int sum = 0;
        for(int i = n; i >= 0; i--) {
            System.out.println(queenLevelCount[i] + " calls and " +
            forwardCheckingCutoffCount[i] + " cutoffs on level " + i + ".");
            sum += queenLevelCount[i];
        }
        System.out.println("Total number of calls was " + sum + ".");
        return result;
    }
    
    private int[] queenLevelCount;
    private int[] forwardCheckingCutoffCount;
    
    // Utility method to use for forward checking: Does the given row still have any
    // places possible to place the queen?
    private boolean queenStillPossible(int row, boolean[] cols, boolean[] ne, boolean[] se) {
        for(int col = 0; col < cols.length; col++) {
            if(!(cols[col] || ne[cols.length - row + col] || se[row + col])) {
                return true;
            }
        }
        return false;
    }

    // How many ways can you place the remaining n queens, with rows etc. already taken?
    // Just like in Subset Sum, the answer and the running time are exponential as function
    // of n. Try values 10, 12, 14. For how big n can you still wait for the result?
    private int nQueens(int n, boolean[] cols, boolean[] ne, boolean[] se) {
        queenLevelCount[n]++;
        if(n == 0) { return 1; } // All queens successfully placed.
        // Loop through all the possibilities to place the current queen in row n-1.
        int row = n - 1; // The row in which the current queen will be placed.
        int sum = 0;
        // Utilize the mirror symmetry when placing the queen on the first row, to
        // cut the running time of recursion in half.
        int limit = (n == cols.length - 1) ? cols.length / 2 + cols.length % 2 : cols.length;
        for(int col = 0; col < limit; col++) {
            if(cols[col]) { continue; } // This column was already taken.
            if(ne[cols.length - row + col]) { continue; } // This diagonal was already taken.
            if(se[row + col]) { continue; } // This diagonal was already taken.
            // Place the queen in position (row, col) and update the arrays.
            cols[col] = true;
            ne[cols.length - row + col] = true;
            se[row + col] = true;
            // Recursively add up all the possible ways to place the remaining n - 1 queens.
            if(n < 1 || n >= cols.length / 2 - 1 || n < cols.length / 4 || queenStillPossible(1, cols, ne, se)) {
                int ways = nQueens(n - 1, cols, ne, se);
                // Symmetry adjustment for the first queen.
                sum += (n == cols.length - 1 && col < cols.length / 2) ? 2 * ways: ways;
            }
            else {
                forwardCheckingCutoffCount[n]++; 
            }
            // Undo the placement of the current queen before backtracking.
            cols[col] = false;
            ne[cols.length - row + col] = false;
            se[row + col] = false;
        }
        return sum;
    }
}