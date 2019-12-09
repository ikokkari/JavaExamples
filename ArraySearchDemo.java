/**
 * Demonstrate the basic array searching algorithms.
 * @author Ilkka Kokkarinen
 */

public class ArraySearchDemo {

    /**
     * Unoptimized linear search.
     * @param a The array to search the value in.
     * @param x The element to search for.
     * @return The first index in which the element is found,
     * or -1 if that element does not exist in the array. 
     */
    public int linearSearch(int[] a, int x) {
        int i = 0;
        while(i < a.length && a[i] != x) { i++; }
        return i < a.length ? i : -1;
    }

    /**
     * Sentinel search optimizes away one comparison per array element.
     * @param a The array to search the value in.
     * @param x The element to search for.
     * @return The first index in which the element is found,
     * or -1 if that element does not exist in the array. 
     */
    public int sentinelSearch(int[] a, int x) {
        int last = a[a.length - 1];
        a[a.length - 1] = x;
        int i = 0;
        while(a[i] != x) { i++; }
        a[a.length - 1] = last;
        return (i < a.length - 1 || last == x) ? i : -1;
    }

    /**
     * Unrolled search to halve the number of bounds checks.
     * @param a The array to search the value in.
     * @param x The element to search for.
     * @return The first index in which the element is found,
     * or -1 if that element does not exist in the array. 
     */
    public int unrolledSearch(int[] a, int x) {
        int i = 0;
        if(a.length % 2 == 1) { // odd man out
            if(a[i++] == x) { return 0; }
        }
        while(i < a.length) {
            if(a[i++] == x) { return i - 1; }
            if(a[i++] == x) { return i - 1; }
        }
        return -1;
    }

    /**
     * The classic binary search for sorted arrays.
     * @param a The array to search the value in.
     * @param x The element to search for.
     * @return The first index in which the element is found. If the element
     * {@code x} is not found, returns the index where that element would go
     * to keep array in sorted order. If {@code x} is larger than the current
     * largest element of the array, returns {@code a.length} as special case.
     */
    // Binary search for sorted arrays. 
    public int binarySearch(int[] a, int x) {
        int i = 0, j = a.length - 1;
        if(a[j] < x) { return a.length; } // special case
        while(i < j) {
            int mid = (i+j) / 2;
            if(a[mid] < x) { i = mid + 1; }
            else { j = mid; }
        }
        return i;
    }
}