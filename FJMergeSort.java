import java.util.concurrent.RecursiveAction;

/* Sort the given array using merge sort with recursive calls
 * executed in parallel, to demonstrate Java 7 ForkJoinTask. */

public class FJMergeSort extends RecursiveAction {

    private static final int PARCUTOFF = 300; // adjust to taste
    private static final int MSCUTOFF = 50;
    private final int low;
    private final int high;
    private final int[] a;
    private final int[] b;
    
    // Sort the subarray (low..high) of array a, using another array
    // b of the same length as the temporary workspace.
    public FJMergeSort(int[] a, int[] b, int low, int high) {
        this.a = a; this.b = b; this.low = low; this.high = high;
    }
    
    // The important method of every ForkJoinTask.
    @Override public void compute() {
        mergeSort(low, high);
    }
  
    // Recursive mergesort with the left half sorted in a new task
    // in parallel while the right half is sorted in this same task.
    private void mergeSort(int low, int high) { 
        if(high - low > MSCUTOFF) {
            int mid = (low + high) / 2;
            if(mid - low > PARCUTOFF) { // split to parallel task
                FJMergeSort left = new FJMergeSort(a, b, low, mid);
                left.fork(); // starts a new task in parallel
                mergeSort(mid + 1, high);
                left.join(); // waits for that task to finish
            }
            else { // too small for the parallelism overhead to pay off
                mergeSort(low, mid);
                mergeSort(mid + 1, high);
            }
            // either way, merge the sorted subarrays
            merge(low, mid, high);
        }
        else { // too small for mergesort overhead to pay off
            insertionSort(low, high);
        }
    }
    
    // Merge the sorted subarrays (low..mid) and (mid+1..high).
    private void merge(int low, int mid, int high) {
        int i = low, j = mid + 1, loc = low;
        while(i <= mid && j <= high) {
            if(a[i] <= a[j]) { b[loc++] = a[i++]; }
            else { b[loc++] = a[j++]; }
        }
        while(i <= mid) { b[loc++] = a[i++]; }
        while(j <= high) { b[loc++] = a[j++]; }
        System.arraycopy(b, low, a, low, high - low + 1);
    }
    
    // Small subarrays are best sorted with simple insertion sort.
    private void insertionSort(int low, int high) {
        for(int i = low + 1; i <= high; i++) {
            int x = a[i];
            int j = i;
            while(j > low && a[j-1] > x) {
                a[j] = a[j-1];
                j--;
            }
            a[j] = x;
        }
    }
}