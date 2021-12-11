import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 * Many array problems are easy to solve in {@code O(n^2)} quadratic time with a
 * "Shlemiel" approach, but with some clever thinking, they can be turned into
 * linear time {@code O(n)} single pass algorithms. This class illustrates some
 * techniques to achieve this.
 * @author Ilkka Kokkarinen
 */

public class Shlemiel {
    
    /**
     * Given an integer array {@code a}, create the accumulation array {@code b}
     * whose each element equals the sum of elements in a up to that position.
     * @param a The original array to accumulate.
     * @return The accumulation array of the original array.
     */
    public static int[] accumulateShlemiel(int[] a) {
        int[] b = new int[a.length];
        for(int i = 0; i < b.length; i++) {
            int sum = 0;
            for(int j = 0; j <= i; j++) {
                sum += a[i];
            }
            b[i] = sum;
        }
        return b;
    }
    
    /**
     * Given an integer array {@code a}, create the accumulation array {@code b}
     * whose each element equals the sum of elements in a up to that position.
     * @param a The original array to accumulate.
     * @return The accumulation array of the original array.
     */
    public static int[] accumulate(int[] a) {
        int[] b = new int[a.length];
        b[0] = a[0];
        for(int i = 1; i < a.length; i++) {
            b[i] = b[i-1] + a[i];
        }
        return b;
    }
    
    /**
     * Compute the n:th Fibonacci number using exponential recursion, therefore
     * computing the same subproblems over and over again.
     * @param n The position of the Fibonacci number to compute.
     * @return The n:th Fibonacci number.
     */
    public static int fibonacciRec(int n) {
        if(n < 2) { return 1; }
        else { return fibonacciRec(n-1) + fibonacciRec(n-2); }
    }
    
    /**
     * Compute the n:th Fibonacci number using a dynamic programming loop that
     * fills in the array of solutions to subproblems in some order that guarantees
     * that whenever the loop comes to given position, the elements that the recursion
     * would require to have already been computed. Recursive calls can now be turned
     * into simple constant-time array lookups.
     * @param n The position of the Fibonacci number to compute.
     * @return The n:th Fibonacci number.
     */
    public static int fibonacciDyn(int n) {
        int[] fib = new int[n+1];
        fib[0] = fib[1] = 1;
        for(int i = 2; i <= n; i++) {
            fib[i] = fib[i-1] + fib[i-2];
        }
        return fib[n];
    }
    
    /**
     * Checks whether there exists some index {@code i} into the array {@code a} so that
     * the sum of elements in the left subarray from beginning to {@code i} equals the
     * sum of elements in the right subarray from index {@code i + 1} to the end. 
     * @param a The array to determine if such splitting index exists.
     * @return {@code true} if at least one such index exists, {@code false} otherwise.
     */
    public static boolean canBalanceShlemiel(int[] a) {
        for(int i = 0; i < a.length; i++) { // possible splitting points
            int leftSum = 0, rightSum = 0;
            for(int j = 0; j <= i; j++) { leftSum += a[j]; }
            for(int j = i+1; j < a.length; j++) { rightSum += a[j]; }
            if(leftSum == rightSum) { return true; }
        }
        return false;
    }
    
    /**
     * Checks whether there exists some index {@code i} into the array {@code a} so that
     * the sum of elements in the left subarray from beginning to {@code i} equals the
     * sum of elements in the right subarray from index {@code i + 1} to the end. 
     * @param a The array to determine if such splitting index exists.
     * @return {@code true} if at least one such index exists, {@code false} otherwise.
     */
    public static boolean canBalance(int[] a) {
        int leftSum = 0, rightSum = 0;
        for(int e: a) { rightSum += e; }
        for (int j : a) {
            leftSum += j; // O(1) update of leftSum and rightSum, instead of O(n)
            rightSum -= j;
            if (leftSum == rightSum) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Given an {@code n}-element integer array, determine whether it contains
     * each number from 1 to {@code n} exactly once.
     * @param a The array to check.
     * @param n The largest value to look for.
     * @return {@code true} if each number occurs exactly once, {@code false} otherwise.
     */
    public static boolean containsAllNumbersShlemiel(int[] a, int n) {
        for(int i = 1; i <= n; i++) { // numbers to look for
            boolean found = false;
            for(int e: a) {
                if(e == i) { found = true; break; }
            }
            if(!found) { return false; }
        }
        return true;
    }
    
    /**
     * Given an {@code n}-element integer array, determine whether it contains
     * each number from 1 to {@code n} exactly once.
     * @param a The array to check.
     * @param n The largest value to look for.
     * @return {@code true} if each number occurs exactly once, {@code false} otherwise.
     */
    public static boolean containsAllNumbersSorting(int[] a, int n) {
        // We can do better by sorting the array first.
        Arrays.sort(a); // O(n log n) stage dominates
        for(int i = 0; i < n; i++) { // O(n)
            if(a[i] != i+1) { return false; }
        }
        return true;
    }
    
    /**
     * Given an {@code n}-element integer array, determine whether it contains
     * each number from 1 to {@code n} exactly once.
     * @param a The array to check.
     * @param n The largest value to look for.
     * @return {@code true} if each number occurs exactly once, {@code false} otherwise.
     */
    public static boolean containsAllNumbersLinear(int[] a, int n) {
        // For a linear time solution, trade memory for time: use a boolean
        // array to remember which values we have already seen. Allocate
        // one more element than needed to save two subtractions per round.
        boolean[] alreadySeen = new boolean[n + 1];
        for(int e : a) {
            if(e < 1 || e > n || alreadySeen[e]) { return false; }
            alreadySeen[e] = true;
        }
        return true;
    }
    
    /**
     * Remove all strings from the given arraylist of strings whose length is shorter than len.
     * @param a The arraylist of strings to process.
     * @param len The threshold length for a string to remain in the list.
     */
    public static void removeShortStringsShlemiel(ArrayList<String> a, int len) {
        int i = 0;
        while(i < a.size()) {
            if(a.get(i).length() < len) {
                a.remove(i); // removing element from middle takes linear time
            }
            else { i++; }
        }
        // Total worst case running time is O(n) * O(n) = O(n^2)
    }
    
    /**
     * Remove all strings from the given arraylist of strings whose length is shorter than len.
     * @param a The arraylist of strings to process.
     * @param len The threshold length for a string to remain in the list.
     */
    public static void removeShortStrings(ArrayList<String> a, int len) {
        ArrayList<String> tmp = new ArrayList<>();
        for(String e : a) { // total of O(n) over n rounds
            if(e.length() >= len) { tmp.add(e); } // O(1) amortized
        }
        a.clear(); // O(n) (references are set to null to let garbage collection proceed)
        a.addAll(tmp); // O(n)
        // Total worst case running time is O(n) + O(n) + O(n) = O(n)
    }
}