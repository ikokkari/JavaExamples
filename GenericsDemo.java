import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Demonstrate Java generics: type parameters, wildcards, upper and lower bounds,
 * and the invariance of generic types. Updated for Java 21+ with modern idioms.
 *
 * This file depends on the modernized Pair.java and the Fraction class
 * (compile all three together).
 *
 * @author Ilkka Kokkarinen
 */
public class GenericsDemo {

    // -----------------------------------------------------------------------
    // GENERIC UTILITY METHODS
    // -----------------------------------------------------------------------

    /**
     * Check whether all elements in a list are unique (no two equal elements).
     * The wildcard {@code <?>} means "any type" — we only need equals(), which
     * every Object has.
     */
    public static boolean allUnique(List<?> list) {
        // The O(n²) nested loop is intentionally naive — this is a generics demo,
        // not an algorithms lecture. (For real code, dump into a HashSet and compare sizes.)
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).equals(list.get(j))) return false;
            }
        }
        return true;
    }

    /**
     * Copy all elements from {@code src} into {@code tgt}.
     * <p>
     * The lower-bounded wildcard {@code <? super T>} means "T or any supertype
     * of T." This is the PECS principle (Producer Extends, Consumer Super):
     * {@code src} produces T elements (so it could be {@code Collection<T>} or
     * {@code Collection<? extends T>}), while {@code tgt} consumes them (so it
     * needs {@code Collection<? super T>}).
     * <p>
     * Collections already have addAll, but we write this for practice.
     */
    public static <T> void addAll(Collection<T> src, Collection<? super T> tgt) {
        for (T elem : src) {
            tgt.add(elem);
        }
    }

    /**
     * Check whether a list is sorted in ascending order. The type bound
     * {@code <T extends Comparable<T>>} restricts T to types that can
     * compare themselves — you can't call this on a {@code List<Object>}.
     */
    public static <T extends Comparable<T>> boolean isSorted(List<T> list) {
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).compareTo(list.get(i - 1)) < 0) return false;
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // COVARIANCE AND INVARIANCE
    // -----------------------------------------------------------------------
    // Even though ArrayList is a subtype of List, and Fraction is a subtype of
    // Object, it does NOT follow that ArrayList<Fraction> is a subtype of
    // List<Object>. It can't be — the latter allows you to add a String or
    // whatever else, whereas the former only accepts Fractions. Generic types
    // are INVARIANT in their type argument.

    /**
     * Accepts a {@code List<Object>} — note the concrete type argument.
     * This means we CAN add anything to the list (because Object is the
     * supertype of everything), but we CANNOT pass a {@code List<Fraction>}
     * to this method. The compiler stops us for good reason.
     */
    public static int noNines(List<Object> items) {
        int count = 0;
        for (Object item : items) {
            if (!item.toString().contains("9")) {
                count++;
            }
        }
        // Side effect to prove the point: this is legal because the list
        // is declared as List<Object>, so any Object can go in.
        items.add("Hello world!");
        // A List<Fraction> could NOT do that — and that's exactly why
        // List<Fraction> is not a subtype of List<Object>.
        return count;
    }

    /**
     * Accepts a {@code List<? extends Fraction>} — the upper-bounded wildcard.
     * This means: "a list of Fractions or any subtype of Fraction." We CAN
     * read elements and treat them as Fractions, but we CANNOT add to the list
     * (because the compiler doesn't know which specific subtype it holds).
     */
    public static int noNinesInDenominator(List<? extends Fraction> items) {
        int count = 0;
        for (Fraction f : items) {
            if (!f.getDen().toString().contains("9")) {
                count++;
            }
        }
        // items.add(new Fraction(1, 2)); // Would NOT compile — can't add to <? extends ...>
        return count;
    }

    // -----------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        // --- Pair demo (uses the modernized Pair class) ---
        var p1 = Pair.of("Hello", 42);
        var p2 = Pair.of("Hello", 42);
        var p3 = Pair.of("World", 17);
        var p4 = Pair.of(1.234, p1); // Pair<Double, Pair<String, Integer>>

        System.out.println("Pair count: " + Pair.getCount());
        System.out.println("p1.equals(p2)? " + p1.equals(p2));     // true
        System.out.println("p1.equals(p3)? " + p1.equals(p3));     // false
        // Type erasure: at runtime, all Pair objects are the same class.
        System.out.println("p1 same class as p4? " + (p1.getClass() == p4.getClass())); // true

        // A collection using wildcards to allow heterogeneous Pair content.
        // List.of (Java 9+) creates an unmodifiable list.
        List<Pair<?, ?>> pairs = List.of(p1, p2, p3, p4);
        System.out.println(pairs);
        System.out.println("All unique? " + allUnique(pairs)); // false (p1 equals p2)

        Pair<?, ?> elem = pairs.get(2);
        System.out.println("Pulled out: " + elem);
        // We can't assume anything about the types of first() and second() here.
        // They are just Objects — that's all the type system knows through <?>.

        // A more specifically typed collection — lets us make stronger assumptions.
        Set<Pair<String, Integer>> pairSet = new HashSet<>();
        pairSet.add(p1);
        pairSet.add(p2); // duplicate — silently ignored by the set
        pairSet.add(p3);
        int tally = 0;
        for (Pair<String, Integer> p : pairSet) {
            // We KNOW second() returns Integer, so this is legal without a cast.
            tally += p.second();
        }
        System.out.println("Final tally: " + tally); // 42 + 17 = 59

        // --- Comparable and type bounds ---
        // List.of creates an unmodifiable list. The element type Integer
        // implements Comparable<Integer>, so isSorted can accept it.
        var a = List.of(1, 2, 3, 5, 4);
        System.out.println("All unique? " + allUnique(a));    // true
        System.out.println("Is sorted? " + isSorted(a));      // false

        // Demonstrate addAll with lower-bounded wildcard.
        List<Object> b = new ArrayList<>();
        b.add("Hello world");
        addAll(a, b); // Integer is a subtype of Object, so this compiles.
        System.out.println("After addAll: " + b);

        // --- Upper-bounded wildcard demo ---
        var fractions = List.of(
                new Fraction(3, 19),
                new Fraction(7, 18),
                new Fraction(-1, 99),
                new Fraction(12345, 54321)
        );
        int noNinesDen = noNinesInDenominator(fractions);
        System.out.println(noNinesDen + " fractions have no 9 in denominator.");

        // Is our list of fractions sorted?
        System.out.println("Fractions sorted? " + isSorted(fractions));

        // This call would NOT compile — uncomment to convince yourself:
        // int n = noNines(fractions);
        // Error: List<Fraction> is not List<Object>. That's the whole point.
    }
}