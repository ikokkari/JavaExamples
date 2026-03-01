import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An infinite, lazy iterator that produces <em>Hamming numbers</em> (also known
 * as regular numbers or 5-smooth numbers) in ascending order. A Hamming number
 * is a positive integer whose only prime factors are 2, 3, and 5.
 * <p>
 * The sequence begins: 1, 2, 3, 4, 5, 6, 8, 9, 10, 12, 15, 16, 18, 20, ...
 * <p>
 * The implementation maintains a priority queue frontier: each time a Hamming
 * number is yielded, its multiples by 2, 3, and 5 are added to the frontier
 * (if not already present). The smallest frontier element is always the next
 * Hamming number — a neat application of best-first search.
 * <p>
 * Implementing {@link Iterable} means you can use this class in a for-each
 * loop, and {@link #stream()} provides access to the full power of the
 * Stream API for filtering, mapping, and collecting.
 *
 * <pre>{@code
 *     // Print the first 20 Hamming numbers:
 *     var hamming = new HammingNumbers();
 *     hamming.stream().limit(20).forEach(System.out::println);
 *
 *     // Use in a for-each loop (careful — it's infinite!):
 *     for (BigInteger h : new HammingNumbers()) {
 *         if (h.bitLength() > 100) break;
 *         System.out.println(h);
 *     }
 * }</pre>
 *
 * @author Ilkka Kokkarinen
 */
public class HammingNumbers implements Iterable<BigInteger> {

    // The three prime factors that define Hamming numbers.
    private static final List<BigInteger> FACTORS = List.of(
            BigInteger.valueOf(2),
            BigInteger.valueOf(3),
            BigInteger.valueOf(5)
    );

    // -----------------------------------------------------------------------
    // Iterable implementation — each call to iterator() produces a fresh,
    // independent iterator that starts from the beginning (1, 2, 3, ...).
    // -----------------------------------------------------------------------

    @Override
    public Iterator<BigInteger> iterator() {
        return new HammingIterator();
    }

    /**
     * Return an infinite sequential {@link Stream} of Hamming numbers.
     * Use {@code .limit(n)} to take a finite prefix.
     *
     * @return a stream of Hamming numbers in ascending order
     */
    public Stream<BigInteger> stream() {
        // Spliterators.spliteratorUnknownSize + StreamSupport is the standard
        // recipe for turning an Iterator into a Stream.
        return StreamSupport.stream(spliterator(), false);
    }

    // -----------------------------------------------------------------------
    // The iterator itself — maintains the priority queue frontier.
    // -----------------------------------------------------------------------

    private static class HammingIterator implements Iterator<BigInteger> {

        // The search frontier, ordered by natural ordering of BigInteger.
        private final PriorityQueue<BigInteger> frontierQueue = new PriorityQueue<>();
        // A parallel set for O(1) duplicate detection.
        private final HashSet<BigInteger> frontierSet = new HashSet<>();

        HammingIterator() {
            // Seed the frontier with 1, the first Hamming number.
            frontierQueue.add(BigInteger.ONE);
            frontierSet.add(BigInteger.ONE);
        }

        /**
         * An infinite iterator always has a next element.
         */
        @Override
        public boolean hasNext() {
            return true; // The Hamming sequence is infinite.
        }

        /**
         * Return the next Hamming number in ascending order, and expand the
         * frontier by adding its multiples by 2, 3, and 5.
         *
         * @return the next Hamming number
         */
        @Override
        public BigInteger next() {
            if (frontierQueue.isEmpty()) {
                // Defensive — should never happen with correct usage.
                throw new NoSuchElementException("Hamming frontier exhausted");
            }

            // Pop the smallest element — this is the next Hamming number.
            BigInteger current = frontierQueue.poll();
            frontierSet.remove(current);

            // Expand: multiply by each factor and add to the frontier if new.
            for (BigInteger factor : FACTORS) {
                BigInteger candidate = current.multiply(factor);
                if (frontierSet.add(candidate)) {
                    // Set.add returns true if the element was not already present.
                    frontierQueue.add(candidate);
                }
            }

            return current;
        }
    }

    // -----------------------------------------------------------------------
    // Convenience: compute the n-th Hamming number (1-indexed).
    // -----------------------------------------------------------------------

    /**
     * Return the {@code n}-th Hamming number (1-indexed), so
     * {@code nth(1) = 1}, {@code nth(2) = 2}, {@code nth(6) = 6}, etc.
     *
     * @param n the 1-based index
     * @return the n-th Hamming number
     * @throws IllegalArgumentException if n is less than 1
     */
    public BigInteger nth(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("Index must be >= 1, got: " + n);
        }
        return stream().skip(n - 1).findFirst().orElseThrow();
    }

    // -----------------------------------------------------------------------
    // Main — demonstrate various ways to use the iterator.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        var hamming = new HammingNumbers();

        // --- For-each loop (the simplest usage) ---
        System.out.println("First 20 Hamming numbers (for-each loop):");
        int count = 0;
        for (BigInteger h : hamming) {
            System.out.print(h + " ");
            if (++count >= 20) break;
        }
        System.out.println("\n");

        // --- Stream with limit ---
        System.out.println("First 20 Hamming numbers (stream):");
        hamming.stream()
                .limit(20)
                .forEach(h -> System.out.print(h + " "));
        System.out.println("\n");

        // --- Finding a specific one ---
        System.out.println("The 1000th Hamming number:");
        long startTime = System.currentTimeMillis();
        BigInteger h1000 = hamming.nth(1000);
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("%s (%d ms)%n%n", h1000, elapsed);

        // --- Stream filtering: Hamming numbers that are also perfect squares ---
        System.out.println("First 15 Hamming numbers that are perfect squares:");
        hamming.stream()
                .filter(h -> {
                    BigInteger sqrt = h.sqrt();
                    return sqrt.multiply(sqrt).equals(h);
                })
                .limit(15)
                .forEach(h -> System.out.print(h + " "));
        System.out.println("\n");

        // --- Demonstrate that each iterator() call is independent ---
        System.out.println("Two independent iterators:");
        var iter1 = hamming.iterator();
        var iter2 = hamming.iterator();
        System.out.print("iter1: ");
        for (int i = 0; i < 10; i++) { System.out.print(iter1.next() + " "); }
        System.out.print("\niter2: ");
        for (int i = 0; i < 10; i++) { System.out.print(iter2.next() + " "); }
        System.out.println();
    }
}