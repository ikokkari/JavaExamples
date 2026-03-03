import java.util.BitSet;

/**
 * A monotonic set of natural numbers ideal for situations where these
 * numbers tend to be encountered in roughly ascending order, but not
 * strictly so. Many recursive sequences work this way.
 *
 * <p>Modernized for Java 21+: the underlying storage is a {@link BitSet}
 * (1 bit per element) instead of a {@code boolean[]} (1 byte per element),
 * giving an immediate 8× memory improvement. The sliding-window compaction
 * strategy is preserved but operates on the {@code BitSet} via bulk
 * {@code get(fromIndex, toIndex)} sub-range extraction.
 *
 * @author Ilkka Kokkarinen
 */
public class NatSet {

    /** Initial capacity of the sliding window, in bits. */
    private static final int PAGE = 1024; // a round power of two suits BitSet well

    // --- Sliding window state ---

    /** The set implicitly contains all natural numbers less than {@code start}. */
    private long start = 0;

    /**
     * Membership data for elements {@code n} satisfying
     * {@code start <= n < start + capacity}. Bit {@code (n - start)}
     * is set iff {@code n} has been added.
     */
    private BitSet data;

    /** Current allocated capacity of the window (in bits). */
    private int capacity;

    /**
     * All bits in {@code data} with index strictly less than {@code pos}
     * are known to be set. This is the sweep cursor for the compaction check.
     */
    private int pos = 0;

    // --- Statistics ---

    private long shiftCount = 0;

    public long getShiftCount() { return shiftCount; }

    /**
     * Every natural number strictly less than this value is a member.
     * Useful for callers who want to exploit the monotonic property.
     */
    public long allTrueUpTo() { return start + pos; }

    public NatSet() {
        capacity = PAGE;
        data = new BitSet(capacity);
    }

    /**
     * Add the natural number {@code n} to the set. If {@code n} is
     * already implicitly contained (i.e. less than {@code start}),
     * this is a no-op.
     *
     * @param n the natural number to add
     */
    public void add(long n) {
        if (n < start) { return; } // already implicitly a member

        // Grow the window capacity if needed.
        int index = (int) (n - start);
        if (index >= capacity) {
            // Grow modestly but at least enough.
            while (index >= capacity) {
                capacity += capacity / 4;
            }
            // BitSet grows automatically on set(), but we track capacity
            // ourselves for the compaction threshold calculation.
        }

        data.set(index);

        // The first clear bit can only advance when we fill the exact
        // gap at pos. In all other cases, pos is unchanged.
        if (index == pos) {
            pos = data.nextClearBit(pos);
        }

        // Once the first quarter of the window is all-true, compact.
        if (pos > capacity / 4) {
            shiftCount += pos;
            // Shift the window: extract the sub-range [pos, capacity) as
            // a new BitSet, then update bookkeeping.
            data = data.get(pos, Math.max(pos, data.length()));
            start += pos;
            capacity -= pos;
            // Ensure capacity doesn't shrink below PAGE.
            if (capacity < PAGE) { capacity = PAGE; }
            pos = 0;
        }
    }

    /**
     * Test whether the natural number {@code n} is a member of the set.
     *
     * @param n the natural number to query
     * @return {@code true} if {@code n} is in the set
     */
    public boolean contains(long n) {
        if (n < start) { return true; }             // left of window: implicit member
        int index = (int) (n - start);
        if (index >= capacity) { return false; }     // right of window: not yet added
        return data.get(index);
    }

    // --- Demo: tortoise and hare verification ---

    public static void main(String[] args) {
        // Demonstration of the "tortoise and hare" principle where two
        // position indices advance the exact same path, except that hare
        // makes two moves for every one move of tortoise. Two identical
        // RNGs generate the same steps for both. The hare adds elements
        // as it goes; the tortoise verifies membership and fills gaps.
        var s = new NatSet();
        var hareRng = new java.util.SplittableRandom(123);
        var tortoiseRng = new java.util.SplittableRandom(123);

        long t = 0, h = 0;
        for (int i = 0; i < 100_000_000; i++) {
            h += hareRng.nextInt(1, 11);  // 1..10 inclusive
            s.add(h);
            if (i % 2 == 1) {
                long next = t + tortoiseRng.nextInt(1, 11);
                t++;
                while (t < next) {
                    assert !s.contains(t) : t + " should not be in the set";
                    s.add(t);
                    t++;
                }
                assert s.contains(t) : t + " should be in the set";
            }
        }
        System.out.println("Ended with hare at " + h + " and tortoise at " + t);
        System.out.println("Shift count: " + s.getShiftCount());
    }
}