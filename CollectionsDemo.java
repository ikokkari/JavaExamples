import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

/**
 * Demonstrate Java Collections Framework: sets, lists, iterators, comparators,
 * and the polymorphic algorithms that tie them together. Updated for Java 21+.
 * @author Ilkka Kokkarinen
 */
public class CollectionsDemo {

    private static final RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");

    @SuppressWarnings("unchecked")
    public static void basicOperations() {
        System.out.println("Basic operations demo begins.");

        // TreeSet keeps its elements in sorted order (red-black tree internally).
        var treeSetOne = new TreeSet<Integer>();

        // The three fundamental dynamic set operations: add, remove, contains.
        treeSetOne.add(42);
        treeSetOne.add(99);
        treeSetOne.add(17); // Despite added last, will be iterated over first.
        treeSetOne.add(99); // Adding a duplicate does nothing — sets have no repeats.
        System.out.println("Our example treeset: " + treeSetOne); // sensible toString()

        // Dumb way to do the following. Used here only for demonstration purposes.
        System.out.println("Looking for elements in the treeset.");
        for (int i = 0; i < 100; i++) {
            if (treeSetOne.contains(i)) {
                System.out.println("Element " + i + " was found");
            }
        }

        treeSetOne.remove(42);
        treeSetOne.remove(42); // Not an error, just does nothing.
        treeSetOne.remove(17);
        System.out.println("Looking for elements in the treeset again.");
        for (int i = 0; i < 100; i++) {
            if (treeSetOne.contains(i)) {
                System.out.println("Element " + i + " was found");
            }
        }

        // Two collections are equal if they are the same general type with equal
        // elements. The contract: Set.equals(Set) compares as sets, List.equals(List)
        // compares as lists, but Set.equals(List) is always false.
        var treeSetTwo = new TreeSet<Integer>();
        treeSetTwo.add(99);
        var hashSet = new HashSet<Integer>();
        hashSet.add(99);
        var arrayList = new ArrayList<Integer>();
        arrayList.add(99);
        System.out.println("treeSetOne equals treeSetTwo: " + treeSetOne.equals(treeSetTwo));
        System.out.println("treeSetOne equals hashSet: " + treeSetOne.equals(hashSet));
        System.out.println("treeSetOne equals arrayList: " + treeSetOne.equals(arrayList));
        System.out.println("treeSetOne equals \"Hello\": " + treeSetOne.equals("Hello"));

        // --- Unmodifiable factory methods (Java 9+) ---
        // Set.of and List.of create compact, immutable collections. Use them when
        // you know the elements up front and don't need mutation.
        Set<Integer> immutableSet = Set.of(10, 20, 30);
        List<String> immutableList = List.of("alpha", "bravo", "charlie");
        System.out.println("Immutable set: " + immutableSet);
        System.out.println("Immutable list: " + immutableList);
        // immutableSet.add(40);  // Would throw UnsupportedOperationException!

        // Set.copyOf freezes a mutable set into an unmodifiable snapshot.
        Set<Integer> frozen = Set.copyOf(treeSetOne);
        treeSetOne.add(1000);
        System.out.println("Original mutated: " + treeSetOne);
        System.out.println("Frozen snapshot untouched: " + frozen);

        // Collections contain references, not actual objects. This means you can
        // create circular structures — they won't become infinite matryoshka dolls.
        var ao = new ArrayList<Object>();
        ao.add(42);
        ao.add("Hello world");
        ao.add(ao); // ao now contains itself!
        System.out.println(ao + " has size of " + ao.size());
        ((Collection<Object>) (ao.get(2))).add(ao); // even stranger, yet legal
        System.out.println(ao + " has size of " + ao.size());

        System.out.println("Basic operations demo ends.\n");
    }

    /**
     * The Collections utility class contains many useful polymorphic algorithms.
     * We also show their modern stream-based equivalents side by side.
     */
    public static void algorithmsDemo() {
        System.out.println("Algorithms demo begins.");

        var ai = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            ai.add(rng.nextInt(1000));
        }
        System.out.println("Initial arraylist:");
        System.out.println(ai);

        System.out.println("The largest element is " + Collections.max(ai) + ".");
        // Stream equivalent — same result, different idiom.
        ai.stream().max(Comparator.naturalOrder())
                .ifPresent(m -> System.out.println("Stream max agrees: " + m));

        // Rotate elements: positive offset rotates right, negative rotates left.
        System.out.println("Rotating 3 steps right:");
        Collections.rotate(ai, 3);
        System.out.println(ai);

        System.out.println("Sorted:");
        Collections.sort(ai);
        System.out.println(ai);

        // Java 21 SequencedCollection: getFirst(), getLast(), reversed().
        // These replace the old get(0) and get(list.size()-1) gymnastics.
        System.out.println("First (smallest): " + ai.getFirst());
        System.out.println("Last (largest):   " + ai.getLast());
        System.out.println("Reversed view:    " + ai.reversed());

        System.out.println("Shuffled:");
        Collections.shuffle(ai);
        System.out.println(ai);

        System.out.println("Filled with 99:");
        Collections.fill(ai, 99);
        System.out.println(ai);
        System.out.println("Value 99 occurs " + Collections.frequency(ai, 99) + " times.");

        System.out.println("Algorithms demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // POLYMORPHIC ITERATOR METHODS
    // -----------------------------------------------------------------------
    // The next method works for any subtype of Iterator<Integer> that will ever
    // exist, instead of us having to write a new version for each new subtype.
    // Always remember the DRY Principle: Don't Repeat Yourself!

    public static int sum(Iterator<Integer> iterator) {
        int total = 0;
        while (iterator.hasNext()) {
            total += iterator.next(); // return current element and advance
        }
        return total;
    }

    // Summing through any collection is now trivially delegated.
    public static int sum(Collection<Integer> c) {
        return sum(c.iterator());
    }

    // -----------------------------------------------------------------------
    // VIRTUAL ITERATORS
    // -----------------------------------------------------------------------
    // There is no law that an iterator must be "backed up" by some actual collection.
    // You can iterate through a virtual, computed, even infinite sequence. The
    // algorithms that consume these iterators don't know the difference — and don't
    // have to care. That is the whole point of polymorphism.

    public static class FibonacciIterator implements Iterator<BigInteger> {
        private BigInteger a = BigInteger.ZERO;
        private BigInteger b = BigInteger.ONE;

        @Override
        public boolean hasNext() { return true; } // Fibonacci numbers never end.

        @Override
        public BigInteger next() {
            BigInteger c = a.add(b);
            a = b; // slide the window one step forward
            b = c;
            return a;
        }
    }

    // Since Iterator<E> is such a simple interface, it is also simple to decorate.
    // This decorator repeats each value from the underlying iterator a fixed number
    // of times before advancing to the next one.

    public static class DuplicatingIterator<E> implements Iterator<E> {
        private final int dup;   // how many times each value is repeated
        private int count;       // how many times the current value has been emitted
        private E value = null;  // the current value being repeated
        private final Iterator<E> client;

        public DuplicatingIterator(Iterator<E> client, int dup) {
            this.client = client;
            this.dup = this.count = dup;
        }

        @Override
        public E next() {
            if (count < dup) {
                count++;
                return value; // return the cached value
            } else {
                value = client.next(); // advance the underlying iterator
                count = 1;
                return value;
            }
        }

        @Override
        public boolean hasNext() {
            // In the middle of a duplication run, there's always a next value.
            // Otherwise, delegate to the underlying iterator.
            return count < dup || client.hasNext();
        }
    }

    public static void fibonacciDemo() {
        System.out.println("Fibonacci demo begins.");
        System.out.println("First 50 Fibonacci numbers, each tripled:");
        Iterator<BigInteger> it = new DuplicatingIterator<>(new FibonacciIterator(), 3);
        for (int i = 0; i < 50; i++) {
            System.out.println(it.next());
        }

        // Modern alternative: Stream.iterate (Java 9+) can express the same
        // Fibonacci sequence without writing a class at all. The seed is a
        // two-element array used as a sliding window.
        System.out.println("\nFirst 10 Fibonacci numbers via Stream.iterate:");
        Stream.iterate(
                        new BigInteger[]{BigInteger.ZERO, BigInteger.ONE},
                        pair -> new BigInteger[]{pair[1], pair[0].add(pair[1])}
                )
                .limit(10)
                .map(pair -> pair[1]) // extract the "current" value
                .forEach(System.out::println);

        System.out.println("Fibonacci demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // COMPARATORS: Strategy objects that control sorting and ordering.
    // -----------------------------------------------------------------------

    // A wacky comparator where every odd integer is smaller than any even integer.
    // Written as a full class to show the classic pre-lambda approach.

    private static class OddEvenComparator implements Comparator<Integer> {
        // Return negative if a < b, positive if a > b, zero if equal.
        @Override
        public int compare(Integer a, Integer b) {
            boolean aOdd = a % 2 != 0, bOdd = b % 2 != 0;
            if (aOdd && !bOdd) return -1; // odd < even
            if (!aOdd && bOdd) return +1; // even > odd
            return a.compareTo(b);        // same parity: natural order
        }
    }

    // A Comparator decorator that counts how many comparisons are performed.
    // Handy for measuring sorting algorithms in class.

    private static class CountingComparator<T> implements Comparator<T> {
        private final Comparator<T> client;
        private int count = 0;

        public CountingComparator(Comparator<T> client) {
            this.client = client;
        }

        @Override
        public int compare(T a, T b) {
            count++;
            return client.compare(a, b);
        }

        // Decorators may define brand-new methods. A polymorphic method that
        // receives a Comparator<T> doesn't care what other functionality these
        // objects have beyond the Comparator interface.
        public int getCount() { return count; }
    }

    public static void demonstrateComparators() {
        System.out.println("Comparators demo begins.");

        var ai = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) { ai.add(rng.nextInt(1000)); }
        System.out.println("Initial arraylist:");
        System.out.println(ai);

        // --- Classic approach: Collections.sort ---
        Collections.sort(ai);
        System.out.println("Sorted naturally: " + ai);
        System.out.println("Largest element: " + Collections.max(ai));

        // --- Modern approach: List.sort (Java 8+) is preferred over Collections.sort ---
        // It sorts the list in place, same as before, but called on the list itself.
        var comp = new CountingComparator<>(new OddEvenComparator());
        ai.sort(comp);
        System.out.println("Sorted with counting OddEvenComparator: " + ai);
        System.out.println("Comparisons needed: " + comp.getCount());

        // --- Lambda comparators (Java 8+) ---
        // For simple comparators, you don't need a whole class. A lambda suffices.
        // This is the lexicographic comparator from the original, now a one-liner.
        var lexComp = new CountingComparator<Integer>(
                (a, b) -> a.toString().compareTo(b.toString())
        );
        ai.sort(lexComp);
        System.out.println("Sorted lexicographically (lambda): " + ai);
        System.out.println("Comparisons needed: " + lexComp.getCount());

        // --- Comparator.comparing (Java 8+) ---
        // The most modern idiom: build comparators from key-extraction functions.
        // Read this as "compare integers by their string representation."
        ai.sort(Comparator.comparing(Object::toString));
        System.out.println("Sorted via Comparator.comparing: " + ai);

        // Chained comparators: first by parity (even before odd), then by value.
        // thenComparing builds compound sort keys, analogous to Python's tuple keys.
        Comparator<Integer> parityThenValue = Comparator
                .comparingInt((Integer x) -> x % 2)  // 0 (even) before 1 (odd)
                .thenComparingInt(x -> x);            // then by natural order
        ai.sort(parityThenValue);
        System.out.println("Sorted even-first, then by value: " + ai);

        // reversed() flips any comparator. These compose freely.
        ai.sort(parityThenValue.reversed());
        System.out.println("Reversed: " + ai);

        System.out.println("Comparators demo ends.\n");
    }

    public static void main(String[] args) {
        basicOperations();
        algorithmsDemo();
        demonstrateComparators();
        fibonacciDemo();
    }
}