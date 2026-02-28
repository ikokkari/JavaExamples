// Demonstration of various things that are different in Python and Java.
// Updated for Java 21+ with modern idioms and language features.

// In Java, all package imports must be placed at the beginning of the file.
// Since Java 5, you can use static imports to bring in individual static methods.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
 * A Java program consists of classes, one public class per file. The source code
 * for the class named Foo must be stored in a file named precisely Foo.java.
 * The compiler produces the executable bytecode file Foo.class from that source.
 * Unlike Python, where compilation happens silently at execution time, Java
 * requires an explicit compilation step before you can run the program.
 */

public class PythonToJava {

    // In Java, braces denote structure. Indentation is only for human eyes —
    // the compiler does not care about it. In theory, you could write an entire
    // Java program on one horrendously long line. (Please don't ever do that.)

    // Static methods inside a class correspond to Python functions. Java is
    // explicitly typed at compile time: all data must be declared with a type
    // so the compiler can enforce correct usage. This catches many errors at
    // compile time, before the actual unit testing takes place.

    // -----------------------------------------------------------------------
    // TMU letter grade conversion using if-else chains.
    // Demonstrates: if-else (not elif), String operations, integer arithmetic.
    // -----------------------------------------------------------------------

    public static String tmuLetterGrade(int percent) {
        // Local variables must be explicitly typed. However, a variable does not
        // need to be initialized immediately, as long as every possible execution
        // path initializes it before its first use.
        String grade;

        // Handle F and A levels as special cases.
        if (percent < 50) {
            grade = "F";
        } // The condition must be inside parentheses.
        else if (percent >= 90) { grade = "A+"; } // Note: else if, not elif.
        else if (percent >= 85) { grade = "A"; }
        else if (percent >= 80) { grade = "A-"; }
        // The B, C, and D levels all share the same internal structure.
        else {
            int tens = percent / 10; // Now known to be 5, 6, or 7.
            grade = "DCB".substring(tens - 5, tens - 4); // (cute indexing trick)
            int ones = percent % 10; // Integer remainder operator %, as in Python.
            if (ones < 3) { grade += "-"; } // Shorthand a += b for a = a + b
            else if (ones > 6) { grade += "+"; }
        }
        return grade;
    }

    // -----------------------------------------------------------------------
    // The same conversion rewritten with a Java 21 switch expression.
    // Demonstrates: switch expression with arrow syntax, yield for blocks,
    // and how switch can replace long if-else ladders more readably.
    // -----------------------------------------------------------------------

    public static String tmuLetterGradeSwitch(int percent) {
        // A switch expression returns a value, unlike the older switch statement.
        // The arrow syntax (->) eliminates the need for break statements.
        return switch (percent / 10) {
            case 10, 9 -> {
                // When the body needs multiple statements, use a block with yield.
                yield (percent >= 90) ? "A+" : "A";
            }
            case 8 -> (percent >= 85) ? "A" : "A-";
            case 7 -> {
                int ones = percent % 10;
                yield "B" + suffixForOnes(ones);
            }
            case 6 -> {
                int ones = percent % 10;
                yield "C" + suffixForOnes(ones);
            }
            case 5 -> {
                int ones = percent % 10;
                yield "D" + suffixForOnes(ones);
            }
            // The default branch is required: the compiler insists that a switch
            // expression covers all possible input values exhaustively.
            default -> "F";
        };
    }

    // Small helper to determine the +/- suffix from the ones digit.
    private static String suffixForOnes(int ones) {
        if (ones < 3) { return "-"; }
        if (ones > 6) { return "+"; }
        return "";
    }

    // -----------------------------------------------------------------------
    // Riffle shuffle of an array.
    // Demonstrates: array creation, C-style for loop vs. Python's range().
    // -----------------------------------------------------------------------

    // In Java, an array is a homogeneous, fixed-length sequence — similar to a
    // numpy array, but always one-dimensional and without reshaping. In the type
    // system, Foo[] is an array whose elements are of type Foo.

    public static int[] riffle(int[] items) {
        // Java does not have a polymorphic len(); arrays use the .length field.
        int half = items.length / 2; // Operator / is integer division for int operands.
        var result = new int[items.length];

        // Java for-loop syntax differs from Python. This is roughly equivalent
        // to Python: for i in range(0, len(items), 2)
        for (int i = 0; i < items.length; i += 2) {
            result[i] = items[i / 2];
            result[i + 1] = items[half + i / 2];
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Verify that a list is a valid permutation of 1..n.
    // Demonstrates: List<Integer>, for-each loop, boolean operators.
    // -----------------------------------------------------------------------

    // The java.util List hierarchy offers higher-level sequence abstractions.
    // ArrayList is closest to Python lists in behaviour. However, Java generics
    // cannot use primitive types as type arguments: you write List<Integer>, not
    // List<int>. Java autoboxes between int and Integer transparently.

    public static boolean isValidPermutation(List<Integer> items, int n) {
        // Track which numbers from 1 to n we have already encountered.
        var alreadySeen = new boolean[n + 1];

        // Java for-each loop iterates through elements of an array or Iterable.
        for (int element : items) {
            // || means or, && means and, ! means not.
            if (element < 1 || element > n || alreadySeen[element]) {
                return false;
            }
            alreadySeen[element] = true;
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Transpose a 2D matrix.
    // Demonstrates: 2D arrays (arrays of arrays), nested loops.
    // -----------------------------------------------------------------------

    // Java only has one-dimensional arrays. Higher dimensions are simulated as
    // arrays of arrays. To access a single row as a 1D array, use one index.

    public static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        var transposed = new double[cols][rows];

        for (int row = 0; row < cols; row++) {
            for (int col = 0; col < rows; col++) {
                transposed[row][col] = matrix[col][row];
            }
        }
        return transposed;
    }

    // -----------------------------------------------------------------------
    // Pascal's triangle as a ragged 2D array.
    // Demonstrates: ragged arrays (rows of different lengths).
    // -----------------------------------------------------------------------

    // Unlike numpy arrays, Java 2D arrays can be ragged — each row is a separate
    // 1D array object that can have its own length.

    public static int[][] pascalTriangle(int numRows) {
        // Create just the outer array (array of row references), not the rows.
        var triangle = new int[numRows][];

        for (int row = 0; row < numRows; row++) {
            triangle[row] = new int[row + 1];
            // Both endpoints of each row are always 1.
            triangle[row][0] = 1;
            triangle[row][row] = 1;
            // Each interior element is the sum of the two elements above it.
            for (int col = 1; col < row; col++) {
                triangle[row][col] = triangle[row - 1][col - 1]
                        + triangle[row - 1][col];
            }
        }
        return triangle;
    }

    // -----------------------------------------------------------------------
    // Heron's algorithm for square root approximation.
    // Demonstrates: do-while loop, method overloading (simulating defaults).
    // -----------------------------------------------------------------------

    // Java has while and for loops, but also a do-while loop that Python lacks.
    // It is quite rare (well under 2% of all loops), but when you need it, you
    // need it. Here is a blast from the ancient past: Heron's algorithm for
    // numerically approximating square roots.

    public static double heronSquareRoot(double x, boolean verbose) {
        double guess = x / 2; // We have to start from somewhere.
        double previousGuess;
        do {
            if (verbose) {
                System.out.println("Current guess is " + guess);
            }
            previousGuess = guess;
            // The new guess is the average of the current guess and x/guess.
            guess = (guess + x / guess) / 2;
        } while (guess != previousGuess); // This while belongs to the do-while.

        if (verbose) {
            System.out.println("Returning result " + guess);
        }
        return guess;
    }

    // Java does not offer default or keyword arguments. Instead, variations
    // are expressed with method overloading: the same method name can be bound
    // to multiple implementations that differ by their parameter types or count.

    public static double heronSquareRoot(double x) {
        return heronSquareRoot(x, false);
    }

    // -----------------------------------------------------------------------
    // Days in a month using a switch expression (Java 14+).
    // Demonstrates: switch expression, ternary operator, multiple case labels.
    // -----------------------------------------------------------------------

    public static int daysInMonth(int month, boolean leapYear) {
        return switch (month) {
            // Multiple case labels sharing the same result can be combined.
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 2 -> leapYear ? 29 : 28; // Ternary: condition ? ifTrue : ifFalse
            case 4, 6, 9, 11 -> 30;
            default -> throw new IllegalArgumentException(
                    "Invalid month number: " + month
            );
        };
    }

    // -----------------------------------------------------------------------
    // "Count and say" sequence encoding (run-length encoding of digits).
    // Demonstrates: StringBuilder, charAt, ternary operator.
    // -----------------------------------------------------------------------

    // Same as in Python, Java strings are immutable. For the common operation
    // of building up a string by appending pieces, use the mutable StringBuilder.

    public static String countAndSay(String digits) {
        var result = new StringBuilder();
        // We pretend that every string has a sentinel '$' before and after it.
        // This avoids special-casing the first and last digit.
        int count = 0;
        char previousDigit = '$';

        for (int i = 0; i <= digits.length(); i++) {
            // Ternary selection to handle the sentinel at the end.
            char currentDigit = (i < digits.length()) ? digits.charAt(i) : '$';

            if (currentDigit == previousDigit) {
                count++; // Shorthand for count += 1
            } else {
                if (count > 0) {
                    result.append(count);
                    result.append(previousDigit);
                }
                count = 1;
                previousDigit = currentDigit;
            }
        }
        // Convert the mutable StringBuilder to an immutable String.
        return result.toString();
    }

    // -----------------------------------------------------------------------
    // All distinct cyclic shifts of a string, in sorted order.
    // Demonstrates: TreeSet for automatic sorted deduplication, List.copyOf.
    // -----------------------------------------------------------------------

    // The java.util Set hierarchy contains set implementations. The two most
    // important are HashSet (hash table, unordered) and TreeSet (balanced BST,
    // guarantees sorted iteration). Their internal mechanisms will be explored
    // in a data structures course, but we can use them without knowing the
    // internals — just as you can drive a car without understanding combustion.

    public static List<String> allCyclicShifts(String text) {
        var shifts = new TreeSet<String>();

        for (int i = 0; i < text.length(); i++) {
            shifts.add(text.substring(i) + text.substring(0, i));
        }
        // List.copyOf creates an unmodifiable list from any collection.
        // Since we used TreeSet, the iteration order is guaranteed to be sorted.
        return List.copyOf(shifts);
    }

    // -----------------------------------------------------------------------
    // Bonus: a stream-based version of cyclic shifts, for comparison.
    // Demonstrates: IntStream, map, collect, and functional style in Java.
    // -----------------------------------------------------------------------

    public static List<String> allCyclicShiftsStream(String text) {
        return IntStream.range(0, text.length())
                .mapToObj(i -> text.substring(i) + text.substring(0, i))
                .collect(Collectors.toCollection(TreeSet::new))
                .stream()
                .toList(); // Since Java 16, .toList() returns an unmodifiable list.
    }

    // -----------------------------------------------------------------------
    // Main method: the starting point when this class is run as a program.
    // Similar in spirit to Python's: if __name__ == '__main__':
    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        // --- Letter grade conversion ---
        System.out.println("A grade of 65% becomes " + tmuLetterGrade(65) + " as a letter.");
        System.out.println("Using switch version: " + tmuLetterGradeSwitch(65));

        // Verify both implementations agree on all possible percentages.
        boolean gradesAgree = IntStream.rangeClosed(0, 100)
                .allMatch(p -> tmuLetterGrade(p).equals(tmuLetterGradeSwitch(p)));
        System.out.println("Both grading methods agree on 0-100: " + gradesAgree);

        // --- Riffle shuffle ---
        int[] numbers = {42, 8, 99, -5, 7, 13};

        // Java arrays lack useful methods. The utility class Arrays fills that gap.
        System.out.printf("%nContents of array are initially %s.%n", Arrays.toString(numbers));
        var riffled = riffle(numbers);
        System.out.printf("Once riffled, these elements are %s.%n", Arrays.toString(riffled));
        riffled = riffle(riffled);
        System.out.printf("Riffled again, these elements are %s.%n", Arrays.toString(riffled));

        // --- Permutation checking ---
        // Java does not have Python list comprehensions, but streams can fill
        // that role. Here we create a mutable list of 1..10 for shuffling.
        var permutation = IntStream.rangeClosed(1, 10)
                .boxed() // Convert IntStream to Stream<Integer>
                .collect(Collectors.toCollection(ArrayList::new));

        System.out.println("\nChecking out some permutations.");
        for (int trial = 0; trial < 5; trial++) {
            Collections.shuffle(permutation);
            System.out.println(permutation + " is permutation: "
                    + isValidPermutation(permutation, 10));
        }

        // --- 2D matrix transpose ---
        var rng = new Random(12345);
        System.out.println("\nTo demonstrate 2D arrays, transpose a matrix.");
        double[][] matrix = {
                {1.0, -2.2, 5.1, 99},
                {-4.3, 1.234, Math.sqrt(7), rng.nextDouble()}
        };
        System.out.println("Matrix rows before transpose:");
        System.out.println(Arrays.deepToString(matrix));
        System.out.println("Matrix rows after transpose:");
        System.out.println(Arrays.deepToString(transpose(matrix)));

        // --- Pascal's triangle ---
        System.out.println("\nThe first 15 rows of Pascal's triangle.");
        int[][] pascal = pascalTriangle(15);
        for (int[] row : pascal) {
            for (int value : row) {
                System.out.printf("%-5d", value);
            }
            System.out.println();
        }

        // --- Heron's square root ---
        System.out.println("\nSquare root of 2.0 equals about "
                + heronSquareRoot(2.0) + ".");

        // --- Days in month (switch expression) ---
        System.out.println("\nDays in February (leap year): " + daysInMonth(2, true));
        System.out.println("Days in February (non-leap): " + daysInMonth(2, false));
        System.out.println("Days in July: " + daysInMonth(7, false));

        // --- Count and say ---
        String digits = "333388822211177";
        System.out.println("\n\"Count and say\" for " + digits + " produces "
                + countAndSay(digits) + ".");

        // --- Cyclic shifts ---
        String[] testStrings = {"01001", "010101", "hello", "xxxxxxx"};
        System.out.println("\nAll cyclic shifts of some example strings, in sorted order.");
        for (String text : testStrings) {
            System.out.println(text + ": " + allCyclicShifts(text));
        }
        // Show that the stream version produces identical results.
        System.out.println("\nStream version agrees: "
                + Arrays.stream(testStrings)
                .allMatch(s -> allCyclicShifts(s).equals(allCyclicShiftsStream(s))));
    }
}