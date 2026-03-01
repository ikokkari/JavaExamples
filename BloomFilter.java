import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * A <b>Bloom filter</b> — a probabilistic set that is extremely space-efficient
 * but allows a small chance of false positives. False negatives are impossible:
 * if the filter says "no", the element was definitely never added.
 * <p>
 * This makes Bloom filters ideal for situations where you need to quickly reject
 * definite non-members (spell checkers, network routers, database query planners)
 * and can tolerate the occasional false alarm that gets verified by a slower
 * exact lookup.
 *
 * <h3>How it works</h3>
 * The filter maintains a bit array of {@code m} bits, all initially zero, and
 * {@code k} independent hash functions. To <b>add</b> an element, compute all
 * {@code k} hashes and set those bit positions to 1. To <b>query</b>, compute
 * the same {@code k} hashes and check whether all those bits are 1. If any bit
 * is 0, the element was definitely never added. If all are 1, the element is
 * <em>probably</em> present — but those bits might have been set by other elements.
 *
 * <h3>Theoretical false positive rate</h3>
 * After inserting {@code n} elements, the probability of a false positive is
 * approximately {@code (1 - e^(-kn/m))^k}. The optimal number of hash functions
 * for a given {@code m/n} ratio is {@code k = (m/n) * ln(2) ≈ 0.693 * m/n}.
 *
 * <h3>Implementation notes</h3>
 * <ul>
 *   <li>Uses {@link BitSet} instead of {@code boolean[]} — 8× more space-efficient,
 *       since each boolean in an array occupies a full byte.</li>
 *   <li>Hash functions use the form {@code h_i(x) = (a_i * x + b_i) >>> 1 % m},
 *       with unsigned shift to avoid the {@code Math.abs(Integer.MIN_VALUE)} bug
 *       that plagues many Bloom filter implementations.</li>
 *   <li>Accepts a {@link ToIntFunction} instead of {@code Function<E, Integer>}
 *       to avoid autoboxing overhead on every hash computation.</li>
 * </ul>
 *
 * @param <E> the element type
 * @author Ilkka Kokkarinen
 */
public class BloomFilter<E> {

    private final BitSet bits;
    private final int bitCount;             // m — number of bits
    private final int[] hashMultipliers;    // a_i coefficients
    private final int[] hashOffsets;        // b_i coefficients
    private final ToIntFunction<E> toInt;   // element → int conversion
    private int elementCount;               // n — elements added so far

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    /**
     * Create a Bloom filter with {@code k} hash functions and {@code m} bits.
     *
     * @param hashFunctionCount the number of hash functions (k)
     * @param bitCount          the number of bits in the filter (m)
     * @param toInt             converts an element to an int for hashing;
     *                          if null, {@code Object.hashCode()} is used
     */
    public BloomFilter(int hashFunctionCount, int bitCount, ToIntFunction<E> toInt) {
        this.bitCount = bitCount;
        this.bits = new BitSet(bitCount);
        this.toInt = toInt;
        this.elementCount = 0;

        // Initialize the hash function family h_i(x) = a_i * x + b_i.
        // We use a seeded Random for reproducible behavior in tests.
        var rng = new Random(12345);
        hashMultipliers = new int[hashFunctionCount];
        hashOffsets = new int[hashFunctionCount];
        for (int i = 0; i < hashFunctionCount; i++) {
            // Odd multipliers guarantee they are coprime with any power-of-two
            // table size, giving better bit dispersion.
            hashMultipliers[i] = rng.nextInt() | 1;
            hashOffsets[i] = rng.nextInt();
        }
    }

    /** Convenience constructor that uses {@code hashCode()} for hashing. */
    public BloomFilter(int hashFunctionCount, int bitCount) {
        this(hashFunctionCount, bitCount, null);
    }

    /**
     * Create a Bloom filter sized for an expected number of elements and
     * a desired false positive probability. Computes optimal {@code m} and
     * {@code k} automatically.
     *
     * @param expectedElements the expected number of elements to insert
     * @param falsePositiveRate the desired false positive probability (e.g. 0.01)
     * @param toInt             element-to-int conversion (null for hashCode)
     */
    public BloomFilter(int expectedElements, double falsePositiveRate, ToIntFunction<E> toInt) {
        this(optimalK(expectedElements, optimalM(expectedElements, falsePositiveRate)),
                optimalM(expectedElements, falsePositiveRate),
                toInt);
    }

    // -----------------------------------------------------------------------
    // Optimal sizing formulas
    // -----------------------------------------------------------------------

    /** Optimal bit count: m = -n * ln(p) / (ln(2))² */
    private static int optimalM(int expectedElements, double falsePositiveRate) {
        double m = -expectedElements * Math.log(falsePositiveRate) / (Math.log(2) * Math.log(2));
        return Math.max(64, (int) Math.ceil(m));
    }

    /** Optimal hash function count: k = (m/n) * ln(2) */
    private static int optimalK(int expectedElements, int bitCount) {
        double k = ((double) bitCount / expectedElements) * Math.log(2);
        return Math.max(1, (int) Math.round(k));
    }

    // -----------------------------------------------------------------------
    // Core operations
    // -----------------------------------------------------------------------

    /**
     * Compute a non-negative hash bucket index for hash function {@code i}.
     * Uses unsigned right shift ({@code >>>}) instead of {@code Math.abs()}
     * to avoid the bug where {@code Math.abs(Integer.MIN_VALUE)} returns
     * a negative number (Integer.MIN_VALUE itself).
     */
    private int hashBucket(int hash, int i) {
        return ((hashMultipliers[i] * hash + hashOffsets[i]) >>> 1) % bitCount;
    }

    /** Convert an element to its integer hash seed. */
    private int elementHash(E element) {
        return (toInt != null) ? toInt.applyAsInt(element) : element.hashCode();
    }

    /**
     * Add an element to the filter. After this call, {@code probablyContains}
     * is guaranteed to return {@code true} for this element.
     *
     * @param element the element to add
     */
    public void add(E element) {
        int hash = elementHash(element);
        for (int i = 0; i < hashMultipliers.length; i++) {
            bits.set(hashBucket(hash, i));
        }
        elementCount++;
    }

    /**
     * Query whether an element is probably in the filter.
     * <ul>
     *   <li>{@code false} → the element was <b>definitely never added</b></li>
     *   <li>{@code true}  → the element was <b>probably added</b>
     *       (with a small false positive probability)</li>
     * </ul>
     *
     * @param element the element to look up
     * @return whether the element is probably present
     */
    public boolean probablyContains(E element) {
        int hash = elementHash(element);
        for (int i = 0; i < hashMultipliers.length; i++) {
            if (!bits.get(hashBucket(hash, i))) {
                return false; // Definitely not present — guaranteed correct.
            }
        }
        return true; // Probably present — small chance of false positive.
    }

    // -----------------------------------------------------------------------
    // Diagnostics
    // -----------------------------------------------------------------------

    /** Return the number of elements that have been added. */
    public int size() { return elementCount; }

    /** Return the fraction of bits that are currently set to 1. */
    public double fillRatio() {
        return (double) bits.cardinality() / bitCount;
    }

    /**
     * Return the theoretical false positive probability given the current
     * number of inserted elements: {@code (1 - e^(-kn/m))^k}.
     */
    public double theoreticalFalsePositiveRate() {
        int k = hashMultipliers.length;
        double exponent = -(double) k * elementCount / bitCount;
        return Math.pow(1 - Math.exp(exponent), k);
    }

    @Override
    public String toString() {
        return "BloomFilter[k=%d, m=%,d bits (%,d KB), n=%,d, fill=%.1f%%, theoretical FP=%.6f]"
                .formatted(hashMultipliers.length, bitCount, bitCount / 8 / 1024,
                        elementCount, fillRatio() * 100, theoreticalFalsePositiveRate());
    }

    // -----------------------------------------------------------------------
    // Random word generator for testing.
    // -----------------------------------------------------------------------

    private static String createUniqueWord(Random rng, Set<String> existing) {
        String word;
        do {
            int length = rng.nextInt(10) + 2;
            var sb = new StringBuilder(length);
            for (int j = 0; j < length; j++) {
                sb.append((char) ('a' + rng.nextInt(26)));
            }
            word = sb.toString();
        } while (existing.contains(word));
        return word;
    }

    // -----------------------------------------------------------------------
    // Main — demonstrate, verify, and measure the Bloom filter.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        var rng = new Random(42);
        int insertCount = 50_000;
        int probeCount = 1_000_000;

        System.out.println("=== Bloom Filter Demonstration ===\n");

        // --- Manual configuration ---
        System.out.println("--- Manual configuration: k=20, m=128 KB ---\n");
        var manualFilter = new BloomFilter<String>(20, 128 * 1024 * 8);
        runExperiment(manualFilter, insertCount, probeCount, rng);

        // --- Auto-configured for 1% false positive rate ---
        System.out.println("\n--- Auto-configured for 1% false positive rate ---\n");
        var autoFilter1 = new BloomFilter<String>(insertCount, 0.01, null);
        runExperiment(autoFilter1, insertCount, probeCount, new Random(42));

        // --- Auto-configured for 0.1% false positive rate ---
        System.out.println("\n--- Auto-configured for 0.1% false positive rate ---\n");
        var autoFilter01 = new BloomFilter<String>(insertCount, 0.001, null);
        runExperiment(autoFilter01, insertCount, probeCount, new Random(42));

        // --- Space comparison ---
        System.out.println("\n--- Space comparison ---\n");
        System.out.printf("  Bloom filter (128 KB config): %,d bytes%n", 128 * 1024);
        // Rough estimate: HashSet stores String references (~40 bytes per entry
        // for the hash table overhead, plus the String objects themselves).
        System.out.printf("  HashSet (same %,d words):     ~%,d bytes (estimated)%n",
                insertCount, insertCount * 80);
        System.out.println("  The Bloom filter uses roughly " +
                (insertCount * 80) / (128 * 1024) + "× less memory.");
    }

    /**
     * Run a complete insert → verify → probe experiment on the given filter.
     */
    private static void runExperiment(BloomFilter<String> filter,
                                      int insertCount, int probeCount, Random rng) {
        var exactSet = new HashSet<String>(insertCount * 2);

        // Insert words into both the Bloom filter and a HashSet.
        for (int i = 0; i < insertCount; i++) {
            String word = createUniqueWord(rng, exactSet);
            filter.add(word);
            exactSet.add(word);
        }

        System.out.println("  " + filter);

        // Verify: no false negatives allowed.
        boolean allFound = exactSet.stream().allMatch(filter::probablyContains);
        System.out.println("  All inserted words found (no false negatives): " + allFound);
        if (!allFound) {
            System.out.println("  ERROR: Bloom filter implementation is broken!");
            return;
        }

        // Probe with words that were never inserted and count false positives.
        int falsePositives = 0;
        for (int i = 0; i < probeCount; i++) {
            String word = createUniqueWord(rng, exactSet);
            if (filter.probablyContains(word)) { falsePositives++; }
        }
        double observedRate = (double) falsePositives / probeCount;

        System.out.printf("  False positives: %,d / %,d probes%n", falsePositives, probeCount);
        System.out.printf("  Observed  FP rate: %.6f%n", observedRate);
        System.out.printf("  Theoretical FP rate: %.6f%n", filter.theoreticalFalsePositiveRate());
    }
}