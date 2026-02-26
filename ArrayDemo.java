import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

/**
 * Demonstrate Java primitive arrays and their multitude of operations,
 * contrasted with ArrayList and String. Updated for Java 21+.
 * @author Ilkka Kokkarinen
 */
public class ArrayDemo {

    // Java 17+ RandomGenerator interface: a modern replacement for java.util.Random.
    // The factory method lets you swap algorithms (L64X128MixRandom, Xoroshiro128PlusPlus,
    // etc.) without changing client code. A fixed seed keeps output reproducible.
    private static final RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");

    private static final DecimalFormat format = new DecimalFormat();
    static {
        // A static initializer block runs once when the JVM first loads this class.
        format.setMaximumFractionDigits(4);
    }

    /**
     * Demonstrate the basic operations of primitive arrays.
     * @param n The length of the array to create for the demonstration.
     */
    public static void arrayFirstDemo(int n) {
        System.out.println("Starting arrayFirstDemo.");

        // To create an array, use new followed by element type and length.
        double[] a = new double[n];

        // Alternatively, if you know what the elements are, you can list them
        // between curly braces. These can be arbitrary expressions, evaluated
        // when execution reaches this point.
        double[] c = { 42, -99.9999, rng.nextGaussian() * 50 };

        // To process elements, loop through their indices.
        for (int i = 0; i < a.length; i++) {
            // Access the i-th element with a[i].
            a[i] = rng.nextDouble();
        }

        // When you only read elements, the enhanced for-each is cleaner.
        double sum = 0;
        for (double e : a) {
            sum += e;
        }

        // You can still go old school and loop through indices explicitly.
        double sum2 = 0;
        for (int i = 0; i < a.length; i++) {
            sum2 += a[i];
        }

        // Java 8+ streams offer a third way. DoubleStream.of wraps a double[].
        // This is more idiomatic in modern Java for simple reductions.
        double sum3 = Arrays.stream(a).sum();

        System.out.println("The sum of values equals " + format.format(sum) + ".");
        System.out.println("Stream sum agrees: " + format.format(sum3));

        // The inherited toString of arrays is useless — it prints a type code and
        // hash, not the contents. The utility class Arrays has the methods that,
        // in a just world, would exist on arrays themselves.
        System.out.println("Useless .toString(): " + a.toString());
        System.out.println("Useful Arrays.toString(): " + Arrays.toString(a));
        // Of course, Arrays.toString doesn't know about DecimalFormat.

        System.out.println("Finished arrayFirstDemo.\n");
        // Arrays, like all objects in Java, are eventually garbage collected
        // once they become unreachable from live variables.
    }

    /**
     * Demonstrate some utility methods in {@code java.util.Arrays}.
     * @param n The length of the array to create for the demonstration.
     */
    public static void demonstrateArraysMethods(int n) {
        System.out.println("Starting demonstrateArraysMethods.");

        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rng.nextInt(1000);
        }
        System.out.println("Original array: " + Arrays.toString(a));

        // Slice out a subarray as a separate copy.
        int[] b = Arrays.copyOfRange(a, 3, n - 2);
        System.out.println("Slice [3, n-2): " + Arrays.toString(b));

        // Sorting — in-place, uses dual-pivot quicksort for primitives.
        Arrays.sort(a);
        System.out.println("After sorting: " + Arrays.toString(a));

        // Binary search on the sorted array. Returns a non-negative index if found,
        // or a negative value encoding the insertion point if not found.
        int distinctCount = 0;
        for (int v = 0; v < 1000; v++) {
            if (Arrays.binarySearch(a, v) >= 0) { distinctCount++; }
        }
        System.out.println("The array contains " + distinctCount + " distinct elements.");

        // A stream-based alternative: sort is already done, so just count distinct.
        long distinctCount2 = Arrays.stream(a).distinct().count();
        System.out.println("Stream distinct count agrees: " + distinctCount2);

        // Filling a subarray with a constant value.
        Arrays.fill(a, 0, n / 2, -1);
        System.out.println("After fill: " + Arrays.toString(a));

        // Arrays.mismatch (Java 9+): find first index where two arrays differ.
        // Useful for comparing arrays element-by-element without writing a loop.
        int[] c = Arrays.copyOf(a, a.length);
        c[n / 2 + 1] = 9999;
        int mismatchAt = Arrays.mismatch(a, c);
        System.out.println("First mismatch with modified copy at index: " + mismatchAt);

        System.out.println("Finished demonstrateArraysMethods.\n");
    }

    /**
     * Strings are how arrays really should be in Java. In many languages they are.
     * This method demonstrates that in Java, they are not. Even though Strings have
     * plenty of functionality that arrays lack, we can only sigh when comparing
     * their use to the simple and uniform sequences in Python.
     */
    public static void stringVersusCharArray() {
        System.out.println("Starting stringVersusCharArray.");

        String s = "Hello world";
        final char[] a = s.toCharArray();

        // For String, length is a method; for arrays, it is a field. Sigh.
        System.out.println("Length of string: " + s.length());
        System.out.println("Length of array:  " + a.length);

        // Indexing: charAt vs []. Same concept, different syntax.
        System.out.println("3rd character of string: " + s.charAt(2));
        System.out.println("3rd character of array:  " + a[2]);

        // Slicing: substring vs copyOfRange, both half-open [start, end).
        System.out.println("Characters 3-6: " + s.substring(3, 7));
        System.out.println("Characters 3-6: "
                + Arrays.toString(Arrays.copyOfRange(a, 3, 7)));

        // Strings are immutable in both length and content. Arrays are immutable
        // only in length — you can reassign any element. The keyword "final" on
        // the reference does NOT make the object immutable; it only prevents
        // reassigning the reference variable itself.
        a[2] = '$';
        System.out.println("After mutation: " + Arrays.toString(a));
        // s.charAt(2) = '$';  // Would not compile — Strings are truly immutable.

        // Some modern String niceties worth knowing:
        // strip() (Java 11) is Unicode-aware, unlike the older trim().
        System.out.println("  padded  ".strip() + " (stripped)");
        // repeat() (Java 11) replaces manual StringBuilder loops.
        System.out.println("ha".repeat(5));
        // isBlank() (Java 11) checks for empty or all-whitespace.
        System.out.println("Is \"  \" blank? " + "  ".isBlank());

        System.out.println("Finished stringVersusCharArray.\n");
    }

    /**
     * {@code ArrayList} is a class in the Java Collections Framework that behaves
     * like an array but can grow. (Internally it reallocates a bigger backing array
     * when the current one fills up.) Its syntax differs slightly from raw arrays,
     * as demonstrated below.
     * @param n The number of elements to add for the demonstration.
     */
    public static void arrayListDemo(int n) {
        System.out.println("Starting arrayListDemo.");

        // ArrayList is a generic class: the type argument specifies the element type.
        // For primitives like int and double, use the corresponding wrapper classes.
        var a1 = new ArrayList<Integer>(); // 'var' infers ArrayList<Integer> (Java 10+).
        var a2 = new ArrayList<Double>();

        // Every ArrayList starts empty — you add elements to it.
        for (int i = 0; i < n; i++) {
            a1.add(rng.nextInt(1000));  // autoboxing: int -> Integer
            a2.add(rng.nextGaussian()); // autoboxing: double -> Double
        }

        // ArrayList (like all Collection classes) has a sensible toString and equals,
        // unlike primitive arrays.
        System.out.println("a1 = " + a1);
        System.out.println("a2 = " + a2);

        // Access by position uses get and set (not [] brackets).
        int sum = 0;
        for (int i = 0; i < a1.size(); i++) {
            sum += a1.get(i); // auto-unboxing: Integer -> int
        }
        // The for-each loop works for all Collection classes.
        int sum2 = 0;
        for (int e : a1) { sum2 += e; }
        // Stream reduction — the modern third option.
        int sum3 = a1.stream().mapToInt(Integer::intValue).sum();
        System.out.println("Element sums: " + sum + ", " + sum2 + ", " + sum3);

        a1.set(3, -999);
        System.out.println("After set(3, -999): " + a1);

        // The Collections utility class is analogous to Arrays.
        System.out.println("Largest in a1: " + Collections.max(a1));
        Collections.shuffle(a1);
        System.out.println("After shuffle: " + a1);

        // --- Unmodifiable lists (Java 9+) ---
        // List.of creates a truly immutable list. Use it when you don't need mutation.
        // This is the closest Java gets to Python's tuples.
        List<String> immutable = List.of("alpha", "bravo", "charlie");
        System.out.println("Immutable list: " + immutable);
        // immutable.add("delta"); // throws UnsupportedOperationException at runtime!

        // List.copyOf freezes a mutable list into an unmodifiable snapshot.
        List<Integer> frozen = List.copyOf(a1);
        a1.set(0, 0);  // mutate the original...
        System.out.println("Original a1 changed: " + a1.getFirst()); // Java 21 getFirst()
        System.out.println("Frozen copy untouched: " + frozen.getFirst());

        // --- SequencedCollection (Java 21) ---
        // Java 21 added getFirst(), getLast(), and reversed() to List.
        // These replace the awkward get(0) and get(list.size()-1) idioms.
        System.out.println("First of a1: " + a1.getFirst());
        System.out.println("Last of a1:  " + a1.getLast());
        System.out.println("Reversed a1: " + a1.reversed());

        System.out.println("Finished arrayListDemo.\n");
    }

    /**
     * Showcase the behaviour of two-dimensional arrays in Java.
     * @param n The height and width of the array to create.
     */
    public static void twoDeeArraysDemo(int n) {
        System.out.println("Starting twoDeeArraysDemo.");

        // A 2D array in Java is really a 1D array of 1D arrays. But you will
        // never go badly wrong if you think of it as a rectangular grid.
        char[][] a = new char[n][n];

        // To visit every cell, use two nested loops.
        for (int row = 0; row < a.length; row++) {
            for (int col = 0; col < a[row].length; col++) {
                a[row][col] = (char) (rng.nextInt(20000) + 50);
            }
        }

        // deepToString reveals the 1D-of-1D structure.
        System.out.println("2D array via deepToString:");
        System.out.println(Arrays.deepToString(a));

        // Extract one row — just use a single index.
        System.out.println("Middle row: " + Arrays.toString(a[n / 2]));

        // You can reassign an individual row. After this, the "grid" doesn't
        // need to remain rectangular — rows can have different lengths.
        // This is called a "ragged array" (or "jagged array").
        a[n / 2] = new char[8];
        Arrays.fill(a[n / 2], '$');
        System.out.println("After replacing the middle row:");
        for (int row = 0; row < a.length; row++) {
            System.out.println(row + ": " + Arrays.toString(a[row]));
        }

        // deepEquals compares 2D (or deeper) arrays element-by-element.
        // Plain equals on arrays compares only reference identity — yet
        // another reason to reach for the Arrays utility class.
        char[][] copy = new char[a.length][];
        for (int i = 0; i < a.length; i++) {
            copy[i] = Arrays.copyOf(a[i], a[i].length);
        }
        System.out.println("Deep copy equals original? " + Arrays.deepEquals(a, copy));
        copy[0][0] = '!';
        System.out.println("After mutation: still equal? " + Arrays.deepEquals(a, copy));

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