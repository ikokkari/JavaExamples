import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Many array problems are easy to solve in {@code O(n^2)} time with the "Shlemiel"
 * approach, but with some clever thinking, they can be turned into {@code O(n)} time
 * single pass algorithms.
 * @author Ilkka Kokkarinen
 */
public class Shlemiel {
    
    /**
     * Given an integer array {@code a}, create the accumulation array {@code b}
     * where each element equals the sum of elements in a up to that position.
     * @param a The original array to accumulate.
     * @return The accumulation array of the original array.
     */
    public static int[] accumulateShlemiel(int[] a) {
        int[] b = new int[a.length];
        for(int i = 0; i < b.length; i++) {
            int sum = 0;
            for(int j = 0; j <= i; j++) {
                sum += a[j];
            }
            b[i] = sum;
        }
        return b;
    }
    
    /**
     * Given an integer array {@code a}, create the accumulation array {@code b}
     * where each element equals the sum of elements in a up to that position.
     * @param a The original array to accumulate.
     * @return The accumulation array of the original array.
     */
    public static int[] accumulate(int[] a) {
        int[] b = new int[a.length];
        if(b.length == 0) { return b; }
        b[0] = a[0];
        for(int i = 1; i < a.length; i++) {
            b[i] = b[i-1] + a[i];
        }
        return b;
    }
    
    /**
     * Given an integer array {@code a}, find and return the length of its
     * longest contiguous strictly ascending subarray.
     * @param a The array to search the ascending subarray in.
     * @return The length of the longest ascending subarray.
     */
    public static int longestAscendingSubarrayShlemiel(int[] a) {
        if(a.length == 0) { return 0; }
        int max = 1;        
        for(int i = 0; i < a.length - 1; i++) {
            int j = i + 1;
            int len = 1;
            while(j < a.length && a[j] > a[j-1]) { j++; len++; }
            if(len > max) { max = len; }
        }
        return max;
    }
    
    /**
     * Given an integer array {@code a}, find and return the length of its
     * longest contiguous strictly ascending subarray.
     * @param a The array to search the ascending subarray in.
     * @return The length of the longest ascending subarray.
     */
    public static int longestAscendingSubarray(int[] a) {
        if(a.length == 0) { return 0; }
        int curr = 1; // The length of the current ascension.
        int max = 1; // The longest ascension that we have seen so far.
        for(int i = 1; i < a.length; i++) { // Start from second element
            if(a[i] > a[i-1]) {
                curr++;
                if(curr > max) { max = curr; }
            }
            else {
                curr = 1;
            }
        }
        return max;
    }
        
    /**
     * Given an integer array {@code a} guaranteed to be sorted in ascending order,
     * determine whether it contains two elements whose sum equals goal value {@code x}.
     * @param a The array to search the two elements in.
     * @return Whether two such elements exist.
     */
    public static boolean twoSummingElementsShlemiel(int[] a, int x) {
        for(int i = 0; i < a.length; i++) {
            for(int j = i + 1; j < a.length && a[i] + a[j] <= x; j++) {
                if(a[i] + a[j] == x) { return true; }
            }
        }
        return false;
    }
    
    /**
     * Given an integer array {@code a} guaranteed to be sorted in ascending order,
     * determine whether it contains two elements whose sum equals goal value {@code x}.
     * @param a The array to search the two elements in.
     * @return Whether two such elements exist.
     */
    public static boolean twoSummingElements(int[] a, int x) {
        int i = 0, j = a.length - 1;
        // Invariant: If such a pair exists in array, such pair exists between indices i and j, inclusive.
        while(i < j) {
            int sum = a[i] + a[j];
            if(sum == x) { return true; }
            else if(sum < x) { i++; } // smallest element is too small to work, advance left
            else { j--; } // largest element too large to work, advance right            
        }
        return false;        
    }
    
    // Puzzle for thought: suppose the previous task was to find three summing elements
    // that add up to x. Shlemiel would solve this in O(n^3) time. Can you solve this in
    // O(n^2) time? How about O(n) time?
    
    /**
     * Evaluate the polynomial at the given point x.
     * @param coefficients The coefficients of the polynomial.
     * @param x The point in which to evaluate the polynomial.
     * @return The value of the polynomial at point {@code x}.
     */
    public static double evaluatePolynomialShlemiel(double[] coefficients, double x) {
        double sum = 0;
        for(int i = 0; i < coefficients.length; i++) {
            double pow = 1;
            // Evaluate each term from scratch in the linear inner loop.
            for(int j = 0; j < i; j++) { pow = pow * x; }
            sum += coefficients[i] * pow;
        }
        return sum;
    }
    
    /**
     * Evaluate the polynomial at the given point x.
     * @param coefficients The coefficients of the polynomial.
     * @param x The point in which to evaluate the polynomial.
     * @return The value of the polynomial at point {@code x}.
     */
    public static double evaluatePolynomialLinear(double[] coefficients, double x) {
        double sum = 0, pow = 1;
        // Two multiplications and one add per coefficient.
        for(double coeff: coefficients) {
            sum += coeff * pow; // Add the current power to the result...
            pow = pow * x; // and use it to compute the next power
        }
        return sum;
    }
    
    /**
     * Evaluate the polynomial at the given point x.
     * @param coefficients The coefficients of the polynomial.
     * @param x The point in which to evaluate the polynomial.
     * @return The value of the polynomial at point {@code x}.
     */
    public static double evaluatePolynomialHorner(double[] coefficients, double x) {
        double sum = 0;
        // One multiplication and one add per coefficient.
        for(int i = coefficients.length - 1; i >= 0; i--) {
            sum = sum * x + coefficients[i];
        }
        // This is also more numerically stable when using floating point.
        return sum;
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
        Arrays.sort(a); // O(n log n) stage dominates asymptotic running time
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
        // array to remember which values we have already seen. (Also, allocate
        // one more element than needed to eliminate need for integer subtract.)
        boolean[] alreadySeen = new boolean[n + 1];
        for(int e : a) {
            if(e < 1 || e > n || alreadySeen[e]) { return false; }
            alreadySeen[e] = true;
        }
        return true;
    }        
    
    /**
     * Remove all strings from the given arraylist of strings whose length is shorter than {@code len}.
     * @param strings The arraylist of strings to process.
     * @param len The threshold length for a string to remain in the list.
     */
    public static void removeShortStringsShlemiel(ArrayList<String> strings, int len) {
        int i = 0;
        while(i < strings.size()) {
            if(strings.get(i).length() < len) { strings.remove(i); } // remove from middle is O(n)
            else { i++; } // advance only if skipping the current element
        }
        // Total worst case running time is O(n) * O(n) = O(n ^ 2)
    }
    
    /**
     * Remove all strings from the given arraylist of strings whose length is shorter than {@code len}.
     * @param strings The arraylist of strings to process.
     * @param len The threshold length for a string to remain in the list.
     */
    public static void removeShortStrings(ArrayList<String> strings, int len) {
        ArrayList<String> tmp = new ArrayList<String>();
        for(String e : strings) { // total of O(n) over n rounds
            if(e.length() >= len) { tmp.add(e); } // O(1) amortized add to end
        }
        strings.clear(); // O(n) (references are set to null to enable garbage collection)
        strings.addAll(tmp); // O(n)
        // Total worst case running time is 3 * O(n) = O(n)
    }

    /**
     * Determine whether the array {@code items} contains a strict majority element.
     * @param items The array from which the look for the majority element.
     * @return Whether there exists a majority element.
     */
    public static boolean hasMajorityShlemiel(int[] items) {
        for(int e: items) {
            int edge = 0;
            for(int ee: items) {
                edge += (ee == e? +1 : -1);
            }
            if(edge > 0) { return true; }
        }
        return false;
    }

    /**
     * Determine whether the array {@code items} contains a strict majority element.
     * @param items The array from which the look for the majority element.
     * @return Whether there exists a majority element.
     */
    public static boolean hasMajorityHashMap(int[] items) {
        HashMap<Integer, Integer> counters = new HashMap<>();
        for(int e: items) {
            counters.put(e, counters.getOrDefault(e, 0) + 1);
        }
        for(int e: counters.keySet()) {
            if(counters.get(e) * 2 > items.length) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether the array {@code items} contains a strict majority element.
     * @param items The array from which the look for the majority element.
     * @return Whether there exists a majority element.
     */
    public static boolean hasMajorityLinear(int[] items) {
        int count = 0, curr = 0;
        for(int e: items) {
            if(count == 0) {
                curr = e; count = 1;
            }
            else {
                count += (curr == e ? +1 : -1);
            }
        }
        // At this point, if there is a majority element, curr is it. So let's check.
        int edge = 0;
        for(int e: items) {
            edge += (curr == e ? +1 : -1);
        }
        return edge > 0;
    }
}