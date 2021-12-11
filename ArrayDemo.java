import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Demonstrate Java primitive arrays and their multitude of operations.
 * @author Ilkka Kokkarinen
 */

public class ArrayDemo {

    // When using an RNG, give it a fixed seed value to keep results consistent.
    private static final Random rng = new Random(12345);
    private static final DecimalFormat format = new DecimalFormat();
    static { // A static block of code is executed when JVM loads in class bytecode.
        format.setMaximumFractionDigits(4);
    }
    
    /**
     * Demonstrate the basic operations of primitive arrays.
     * @param n The length of array to create for the demonstration.
     */
    public static void arrayFirstDemo(int n) {
        System.out.println("Starting arrayFirstDemo.");
        // To create an array, use new followed by element type and length.
        double[] a = new double[n];
        // Alternatively, if you know what the elements are, you can list them
        // between curly braces. These elements can be arbitrary expressions
        // that will be evaluated at time the method execution gets here.
        double[] c = { 42, -99.9999, rng.nextGaussian() * 50 };
        // To process the elements, you can loop through their indices.
        for(int i = 0; i < a.length; i++) {
            // To access the i:th element of array a, use the syntax a[i].
            a[i] = rng.nextDouble();
        }
        // When you just read all elements, you can "for-each" through them.
        double sum = 0;
        for(double e: a) {
            sum += e;
        }
        // You can still be old school and loop through the indices, using
        // the index then to access the element inside the loop body.
        double sum2 = 0;
        for(int i = 0; i < a.length; i++) {
            sum2 += a[i];
        }
        System.out.println("The sum of values equals " + format.format(sum) + ".");
        // The toString method of arrays is useless: the utility class Arrays
        // has methods that in a just world would exist in arrays themselves.
        System.out.println("Method toString in Arrays is useless: " + a.toString());
        System.out.println("Elements are: " + Arrays.toString(a));
        // Of course, this method doesn't know about DecimalFormat.

        System.out.println("Finished arrayFirstDemo.\n");
        // Arrays, same as all objects in Java, get eventually garbage collected
        // once they become inaccessible from your live variables.
    }
    
    /**
     * Demonstrate some other array utility methods in {@code java.util.Arrays}.
     * @param n The length of array to create for the demonstration.
     */
    public static void demonstrateArraysMethods(int n) {
        System.out.println("Starting demonstrateArraysMethods.");
        int[] a = new int[n];
        for(int i = 0; i < n; i++) {
            a[i] = rng.nextInt(1000);
        }
        System.out.println("Original array: " + Arrays.toString(a));
        // An arbitrary subarray can be "sliced" as a separate array.
        int[] b = Arrays.copyOfRange(a, 3, n-2);
        System.out.println("The slice is: " + Arrays.toString(b));
        // Array sorting.
        Arrays.sort(a);
        System.out.println("After sorting: " + Arrays.toString(a));
        // From a sorted array, it is fast to search with binary search.
        int distinctCount = 0;
        for(int v = 0; v < 1000; v++) {
            if(Arrays.binarySearch(a, v) > -1) { distinctCount++; }
        }
        System.out.println("The array contains " + distinctCount + " different elements.");
        // There would be faster ways to detect that: that was just for demo.
        // Last, filling an arbitrary subarray with the same value:
        Arrays.fill(a, 0, n/2, -1);
        System.out.println("After fill: " + Arrays.toString(a));
        System.out.println("Finished demonstrateArraysMethods.\n");
    }
    
    /**
     * Strings are how arrays really should be in Java. In many languages they are.
     * This method demonstrates how in Java, they are not. Even though Strings have
     * plenty of functionality lacking from arrays, we can only sigh when comparing
     * their use to the simple and uniform use of all sequences in Python.
     */
    public static void stringVersusCharArray() {
        System.out.println("Starting stringVersusCharArray.");
        String s = "Hello world";
        final char[] a = s.toCharArray();
        // For Strings, length is method: for arrays, it is a field.
        System.out.println("Length of string: " + s.length());
        System.out.println("Length of array: " + a.length);
        // Indexing versus charAt method.
        System.out.println("3rd character of string: " + s.charAt(2));
        System.out.println("3rd character of array: " + a[2]);
        // copyOfRange versus substring
        System.out.println("Characters 3-6: " + s.substring(3, 7)); // one past end
        System.out.println("Characters 3-6: " + Arrays.toString(Arrays.copyOfRange(a, 3, 7)));
        // Strings are immutable in both length and content. Arrays are immutable in
        // length, but not in content, as you can reassign any element. The fact that
        // the reference is final does not make the object itself immutable.
        a[2] = '$';
        // String has a ton of cool methods (see DataDemo and the documentation
        // of String in the Java API reference). Arrays have diddly squat.
        System.out.println(Arrays.toString(a));
        System.out.println("Finished stringVersusCharArray.\n");
    }
    
    /**
     * {@code ArrayList} is a class in Java Collection Framework (more in 209) that
     * is like an array, but can grow. (It quietly reallocates a bigger array when
     * its current array fills up.) Their syntax is slightly different from arrays,
     * though, as demonstrated by the code of this method.
     * @param n The length of array to create for the demonstration.
     */
    public static void arrayListDemo(int n) {
        System.out.println("Starting arrayListDemo.");
        // ArrayList is a generic class: you give it a type argument to tell what
        // kind of elements it stores. For builtin types such as int and double, you
        // must use the corresponding wrapper classes.
        ArrayList<Integer> a1 = new ArrayList<>(); // Compiler fills in the <> diamond.
        ArrayList<Double> a2 = new ArrayList<>();
        // Every ArrayList object starts as empty: you need to add elements to it.
        for(int i = 0; i < n; i++) {
            a1.add(rng.nextInt(1000));
            a2.add(rng.nextGaussian());
        }
        // ArrayList, same as other Collection classes, has decent toString and equals.
        System.out.println("ArrayList a1 equals " + a1 + ".");
        System.out.println("ArrayList a2 equals " + a2 + ".");
        // Accessing elements by position is done with the methods get and set.
        int sum = 0;
        for(int i = 0; i < a1.size(); i++) {
            sum += a1.get(i);
        }
        // The for-each loop also works for all Collection classes.
        int sum2 = 0;
        for(int e: a1) { sum2 += e; }
        System.out.println("Element sum equals " + sum + " or " + sum2);
        a1.set(3, -999);
        System.out.println("After set, ArrayList a1 equals " + a1 + ".");
        // The class Collections is analogous to Arrays utility class.
        System.out.println("Largest element of a1 is " + Collections.max(a1) + ".");
        Collections.shuffle(a1, rng);
        System.out.println("After shuffle, a1 equals " + a1 + ".");
        System.out.println("Finished arrayListDemo.\n");
    }
    
    /**
     * Showcase the behaviour of two-dimensional arrays in Java.
     * @param n The length and width of the array to create for the demonstration.
     */
    public static void twoDeeArraysDemo(int n) {
        System.out.println("Starting twoDeeArraysDemo.");
        // A two-dimensional array is really a 1D array of 1D arrays. But you
        // will never go badly wrong if you think of it and use it as a 2D grid.
        char[][] a = new char[n][n];
        // To loop through its indices, use two nested loops.
        for(int row = 0; row < a.length; row++) {
            for(int col = 0; col < a[row].length; col++) {
                a[row][col] = (char)(rng.nextInt(20000) + 50);
            }
        }
        // To convert a 2D array into a String, use Arrays.deepToString. This
        // brings forth the fact that a 2D array is a 1D array of 1D arrays.
        System.out.println("Printing out the 2D array with Arrays.deepToString:");
        System.out.println(Arrays.deepToString(a));
        // To extract an individual row, use one index instead of two.
        System.out.println("Middle row is: " + Arrays.toString(a[n/2]));
        // You can even reassign an individual row. Now the array doesn't need
        // to remain a grid whose rows are of equal length.
        a[n/2] = new char[8];
        Arrays.fill(a[n/2], '$');
        System.out.println("After filling the middle, the contents of array a are now:");
        for(int row = 0; row < a.length; row++) {
            System.out.println(row + ": " + Arrays.toString(a[row]));
        }
        System.out.println("Finished twoDeeArraysDemo.\n");
    }

    public static void main(String[] args) {
        final int N = 10;
        arrayFirstDemo(N);
        demonstrateArraysMethods(N);
        stringVersusCharArray();
        arrayListDemo(N);
        twoDeeArraysDemo(N);
        System.out.println("And we are all done!");
    }
}