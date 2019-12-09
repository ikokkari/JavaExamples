 import java.util.*; // for ArrayList

public class FirstBatch {

    // So that the code will compile, an example implementation of isFunny.
    public static boolean isFunny(int n) {
        return n % 3 == 0;
    }
    
    public static int countFunnies(int[] a) {
        int count = 0;
        for(int e: a) { // We care about the elements only, not their indices.
            if(isFunny(e)) { count++; }
        }
        return count;
    }
    
    public static int[] collectFunnies(int[] a) {
        int count = countFunnies(a);
        int[] b = new int[count];
        int loc = 0; // The index where the next funny element goes.
        for(int e: a) {
            if(isFunny(e)) {
                b[loc++] = e;
            }
        }
        return b;
    }
    
    // A utility method needed in the later selection sort method.
    public static int locationOfSmallest(int[] a, int start, int end) {
        int m = start;
        for(int i = start + 1; i <= end; i++) {
            if(a[i] < a[m]) { m = i; }
        }
        return m;
    }
 
    // Our first (and worst) sorting algorithm in the course.
    public static void selectionSort(int[] a) {
        for(int i = 0; i < a.length - 1; i++) {
            int j = locationOfSmallest(a, i, a.length - 1);
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
    }

    // Three methods of similar spirit and style coming up.
    public static int[] zip(int[] a, int[] b) {
        int i = 0, loc = 0;
        int[] c = new int[a.length + b.length];
        while(i < a.length && i < b.length) {
            c[loc++] = a[i];
            c[loc++] = b[i++];
        }
        while(i < a.length) { c[loc++] = a[i++]; }
        while(i < b.length) { c[loc++] = b[i++]; }
        return c;
    }
    
    public static int[] merge(int[] a, int[] b) {
        int i = 0, j = 0, loc = 0;
        int[] c = new int[a.length + b.length];
        while(i < a.length && j < b.length) {
            if(a[i] <= b[j]) { c[loc++] = a[i++]; }
            else { c[loc++] = b[j++]; }
        }
        while(i < a.length) { c[loc++] = a[i++]; }
        while(j < b.length) { c[loc++] = b[j++]; }
        return c;
    }
    
    public static int[] intersection(int[] a, int[] b) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int i = 0, j = 0;
        while(i < a.length && j < b.length) {
            if(a[i] < b[j]) { i++; }
            else if(a[i] > b[j]) { j++; }
            else {
                result.add(a[i]);
                i++; j++;
            }
        }
        // Even though Java converts int to Integer automatically,
        // this conversion is only for scalars, not for arrays.
        int[] c = new int[result.size()];
        for(i = 0; i < c.length; i++) { c[i] = result.get(i); }
        return c;
    }
    
    public static int[] removeDuplicates(int[] a) {
        if(a.length == 0) { return a; }
        int count = 1;
        for(int i = 1; i < a.length; i++) {
            if(a[i] != a[i-1]) { count++; }
        }
        int[] b = new int[count];
        int loc = 1;
        b[0] = a[0];
        for(int i = 1; i < a.length; i++) {
            if(a[i] != a[i-1]) { b[loc++] = a[i]; }
        }
        return b;
    }
    
    // This problem demonstrates how to write one-pass methods through array
    // using state variables that are updated in each step.
    public static int longestAscending(int[] a) {
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
 
    // Adapted (and stylistically much improved) from Rosetta Code.
    
    private static String[] symbolArr = {
        "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
    };
    
    private static int[] valueArr = {
        1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
    };
    
    public static String romanEncode(int n) {
        if(n < 1) { throw new IllegalArgumentException("Cannot convert " + n + " to Roman"); }
        StringBuilder result = new StringBuilder();
        int idx = 0;
        while(n > 0) {
            while(n >= valueArr[idx]) {
                result.append(symbolArr[idx]);
                n -= valueArr[idx];
            }
            idx++;
        }
        return result.toString();
    }
    
    private static String symbols = "MDCLXVI";
    private static int[] values = { 1000, 500, 100, 50, 10, 5, 1 };
    
    public static int romanDecode(String roman) {
        int result = 0;
        int prev = 0;
        roman = roman.toUpperCase(); // Canonical form
        for(int idx = roman.length() - 1; idx >= 0; idx--) {
            int curr = symbols.indexOf(roman.charAt(idx));
            if(curr == -1) {
                throw new IllegalArgumentException("Illegal character " + roman.charAt(idx));
            }
            curr = values[curr];
            result += curr < prev ? -curr : +curr; // IX vs. XI distinction
            prev = curr;
        }
        return result;
    }
    
    public static void testRomanConversions() {
        for(int n = 1; n < 5000; n++) {
            assert n == romanDecode(romanEncode(n));
        }
    }
    
    
}