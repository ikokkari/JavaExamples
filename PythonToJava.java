// Demonstration of various things that are different in Python and Java.

// In Java, all package imports must be placed at the beginning of the file.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

/* 
 * Java program must consist of classes, one class per file. The source code for
 * the class named Foo must be stored in a file that is named precisely Foo.java.
 * From that, the compiler will produce the executable bytecode file Foo.class.
 * The compilation stage must be incurred explicitly, unlike in Python where it
 * is done silently when you execute the Python source code file.
 */

public class PythonToJava {

    // In Java, braces denote structure. Indentation is only for the human eyes,
    // the compiler does not care about it. In theory, you could write an entire
    // Java program in one horrendously long line. (Please don't ever do that.)

    // Static methods inside a class correspond to Python functions. Java is
    // explicitly typed at compile time, so that all data must be declared its
    // type at compile time, so that the compiler can enforce that all data is 
    // used only according to that declared type. This allows many errors to be
    // caught in the compile time, before the unit testing.

    public static String ryersonLetterGrade(int pct) {
        // Local variable declarations must also be explicitly typed. However,
        // a variable does not need to be initialized right away, as long as
        // every possible execution path initializes it before the use.
        String result;
        // Handle F and A levels as special cases.
        if(pct < 50) { 
            result = "F"; 
        } // condition must be inside parentheses
        else if(pct >= 90) { result = "A+"; } // else if, not elif
        else if(pct >= 85) { result = "A"; }
        else if(pct >= 80) { result = "A-"; }
        // B, C, and D levels all have the same structure.
        else { 
            int tens = pct / 10; // This is now known to be 5, 6 or 7.
            result = "DCB".substring(tens - 5, tens - 4); // (cute trick)
            int ones = pct % 10; // Integer remainder operator %, almost as in Python.
            if(ones < 3) { result += "-"; } // Shorthand a += b for a = a + b;
            else if(ones > 6) { result += "+"; }
        }
        return result;        
    }

    // In Java, an array is a homogeneous sequence whose length cannot change, akin
    // to the numpy arrays except without reshaping and always one-dimensional. In
    // the type system, Foo[] is an array whose elements are of type Foo. With arrays,
    // Foo can be either a primitive type or a class.
    public static int[] riffle(int[] items) {
        // Java does not have a polymorphic len function on sequences.
        int n = items.length / 2; // Operator / is integer division for int operands.
        int[] result = new int[items.length];
        // Java for-loop has a different purpose and syntax than Python for-loop.
        // This is roughly equal to Python loop: for i in range(0, len(items), 2)
        // to iterate through all even indices.
        for(int i = 0; i < items.length; i += 2) {
            result[i] = items[i / 2];
            result[i + 1] = items[n + i / 2];            
        }
        return result;
    }

    // In java.util, the List hierarchy offers a higher level abstraction of a
    // sequence. The ArrayList is pretty close Python lists in behaviour. However,
    // in Java generics, primitive types cannot be used as type arguments to create
    // a type instantiation of a generic class. However, Java can automatically
    // convert between primitive type (int) and its wrapper class (Integer).

    public static boolean checkPermutation(List<Integer> items, int n) {
        // Keep track of which numbers 1 to n we have already seen here.
        boolean[] alreadySeen = new boolean[n + 1];
        // Java for-each loop to iterate through the elements of array or list.
        for(int e: items) {
            // || = or, && = and, ! = not. This makes little sense, but it's how it is.
            if(e < 1 || e > n || alreadySeen[e]) { return false; }
            alreadySeen[e] = true;
        }
        return true;
    }

    // Java only has one-dimensional arrays. Arrays of higher dimension of n are
    // simulated with one-dimensional arrays whose elements are arrays of dimension
    // n-1. To access an individual row as a one-dimensional array, use one index,
    // same as with a Python list whose elements are lists.
    public static double[][] transpose(double[][] a) {
        double[][] b = new double[a[0].length][a.length];
        // Operator ++ is shorthand for adding one. Operator -- would decrement.
        for(int i = 0; i < b.length; i++) {
            for(int j = 0; j < b[i].length; j++) {
                b[i][j] = a[j][i];
            }
        }
        return b;
    }    

    // Unlike numpy arrays, two-dimensional arrays Java can be ragged, which means
    // different rows can have different lengths. Each row is a separate 1D array
    // object anyway and not restricted by the lengths of other row arrays.
    public static int[][] pascalTriangle(int n) {
        // Create just the array of arrays, but not the individual rows.
        int[][] result = new int[n][];
        // Create and fill the individual rows.
        for(int row = 0; row < n; row++) {
            result[row] = new int[row + 1];
            result[row][0] = result[row][row] = 1;
            for(int col = 1; col < row; col++) {
                result[row][col] = result[row-1][col-1] + result[row-1][col];
            }
        }
        return result;
    }

    // Java has a while-loop and for-loop, but then also a do-while loop that Python
    // does not have. In practice it is quite rare, maybe about 2% of all loops. But
    // when it is time to use it, then it is time to use it. Here is a blast from the
    // ancient past, Heron's algorithm for numerical approximation of square roots.
    public static double heronRoot(double x, boolean verbose) {
        double guess = x / 2; // we have to start from somewhere
        double prev;
        do {
            if(verbose) { System.out.println("Current guess is " + guess); }
            // Current guess becomes the previous guess.
            prev = guess;
            // Calculate a new, more accurate guess.
            guess = (guess + x / guess) / 2;
        } while(guess != prev); // Not a while-loop, but part of do-while.
        if(verbose) { System.out.println("Returning result " + guess); }
        return guess;
    }

    // Java does not offer default or keyword arguments. Instead, variations are
    // expressed with method overloading inside the same class. Unlike Python, where
    // each name is bound to exactly one object, the same method name can be bound
    // to multiple implementations that differ by their parameter types.

    public static double heronRoot(double x) {
        return heronRoot(x, false);
    }

    // Java and similar languages have a switch-statement for long if-else ladders.
    // Java 15 introduces switch expressions. Uncomment to experience the freedom.
    /*
    public static int daysInMonth(int m, boolean leapYear) {
        return switch (m) {
            // Multiple cases that share a body can be combined together.
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 2 ->
                    // Python equivalent: d = 29 if leapYear else 28
                    (leapYear ? 29 : 28); // Java ternary selection COND ? POS : NEG
            case 4, 6, 9, 11 -> 30;
            default -> // Kind of "none of the above" option.
                    0;
        };
    }
    */

    // Same as in Python, Java strings are immutable. For the common operation of
    // building up a string by appending stuff piecemeal, use mutable StringBuilder.
    public static String countAndSay(String digits) {
        StringBuilder result = new StringBuilder();
        // We pretend that every string has '$' before and after it. This way
        // we don't have to handle the first or the last digit as a special case.
        int count = 0; // How many times we have seen the current digit.
        char prev = '$'; // The previous digit seen before the current one.        
        for(int i = 0; i <= digits.length(); i++) {
            // Ternary selection again.
            char curr = i < digits.length() ? digits.charAt(i): '$';
            if(curr == prev) { count++; } // count++ is shorthand for count += 1.
            else {
                if(count > 0) {
                    result.append(count); result.append(prev);
                }
                count = 1; prev = curr;
            }
        }
        // Convert mutable StringBuilder into an immutable String when finished.
        return result.toString();
    }

    // java.util.Set hierarchy contains set implementations. The two most important
    // of those are HashSet (hash table, unsorted iteration) and TreeSet (binary 
    // search tree, guarantees sorted order of iteration through elements). Their
    // internal mechanism will be explored in 305, but we can still use them even
    // if we don't know how they work inside, the same way you are allowed to drive
    // a car even if you don't know how a combustion engine actually works.
    public static List<String> allCyclicShifts(String text) {
        // Keep track of which strings have already been produced.
        TreeSet<String> alreadySeen = new TreeSet<>();
        // Iterate through all possible cutting points.
        for(int i = 0; i < text.length(); i++) {
            alreadySeen.add(text.substring(i) + text.substring(0, i));            
        }
        // Java collection instances can be created from existing instances. Since
        // we used a TreeSet instead of HashSet, iteration guarantees sorted order.
        return new ArrayList<>(alreadySeen);
    }

    // The starting point of execution when a Java class is run as a standalone program.
    // Similar to spirit to Python construct if __name__ == '__main__':
    public static void main(String[] args) {
        // Declaration of an array with immediate initialization.
        int[] a = {42, 8, 99, -5, 7, 13};
        // Java arrays are stupid and have no useful methods. That is why the
        // standard library has utility class Arrays for dealing with arrays.
        System.out.printf("Contents of array are initially %s.\n", Arrays.toString(a));
        int[] ar = riffle(a);
        System.out.printf("Once riffled, these elements are %s.\n", Arrays.toString(ar));
        ar = riffle(ar);
        System.out.printf("Riffled again, these elements are %s.\n", Arrays.toString(ar));

        List<Integer> p = new ArrayList<>(); // Empty list
        // (Java does not have Python list comprehensions.)
        for(int i = 0; i < 10; i++) { p.add(i + 1); } // Add elements to it.
        System.out.println("\nChecking out some permutations.");
        for(int i = 0; i < 5; i++) {
            Collections.shuffle(p);
            System.out.println(p + " is permutation: " + checkPermutation(p, 10));
        }

        Random rng = new Random(12345);
        System.out.println("\nTo demonstrate 2D arrays, transpose a 2D matrix.");
        double[][] mat = { {1.0, -2.2, 5.1, 99}, {-4.3, 1.234, Math.sqrt(7), rng.nextDouble()} };
        System.out.println("Matrix rows before transpose:");
        System.out.println(Arrays.deepToString(mat));
        System.out.println("Matrix rows after transpose:");
        System.out.println(Arrays.deepToString(transpose(mat)));

        System.out.println("\nThe first 15 rows of Pascal's triangle.");
        int[][] pascal = pascalTriangle(15);
        for (int[] ints : pascal) {
            for (int anInt : ints) {
                // C-style printf for formatted output exactly as given.
                System.out.printf("%-5d", anInt);
            }
            System.out.println();
        }

        System.out.println("\nSquare root of 2.0 equals about " + heronRoot(2.0) + ".");

        String digits = "333388822211177";
        System.out.println("\n\"Count and say\" for digits " + digits + " produces "
            + countAndSay(digits) + ".");

        String[] pats = {"01001", "010101", "hello", "xxxxxxx"};
        System.out.println("\nAll cyclic shifts of some example strings, in sorted order.");
        for(String pat: pats) {
            System.out.println(pat + ": " + allCyclicShifts(pat));
        }
    }
}

// no longer inside the class PythonToJava