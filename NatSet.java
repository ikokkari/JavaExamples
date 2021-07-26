import java.util.*;

// A monotonic set of natural numbers ideal for situations where these
// numbers tend to be encountered in roughly ascending order, but not
// strictly so. Many recursive sequences work this way.

public class NatSet {

    // Initial size of the boolean[] used to store membership info.
    private static final int PAGE = 1000;
    // For statistics of how often these optimizations help us.
    private long shiftCount = 0;
    public long getShiftCount() { return shiftCount; }
    
    // The set contains all natural numbers that are less than start.
    private long start = 0;
    // Array to keep track of set membership for elements i that satisfy
    // start <= i < start + n, where n is the current length of the data array.
    private boolean[] data;
    // Inside the data array, every position < pos contains the value true.
    private int pos = 0;
    // We might as well allow the outside code become aware of this fact.
    public long allTrueUpTo() { return start + pos; }
    
    public NatSet() {
        data = new boolean[PAGE];
    }
    
    public void add(long n) {
        if(n >= start) {
            // Determine the need for expanding the data array.
            int newSize = data.length;
            while(n >= start + newSize) {
                // Grow the data array exponentially, but modestly.
                newSize += data.length / 4;
            }
            // If n is past the end of data array, expand data array.
            if(newSize > data.length) {
                data = Arrays.copyOf(data, newSize);
            }
            // Update the element in the data array.
            data[(int)(n - start)] = true;
            // Update the pos counter sweeping through the data array.
            while(pos < data.length && data[pos]) { pos++; }
            // Once the first quarter of elements are true, shift the sliding window.
            if(pos > data.length / 4) {
                // Update the shifting statistics.
                shiftCount += pos;
                // Copy the rest of the array to start from the beginning.
                if (data.length - pos >= 0) {
                    System.arraycopy(data, pos, data, 0, data.length - pos);
                }
                // Update the position of the sliding window.
                start += pos;
                // Fill the portion of data array with false values again.
                Arrays.fill(data, data.length - pos, data.length, false);
                // And start sweeping the data array from the beginning again.
                pos = 0;
            }            
        }
        // No else here, since adding an element < start changes nothing.
    }
    
    public boolean contains(long n) {
        // Everything to the left of the sliding window is member.
        if(n < start) { return true; }
        // Everything to the right of the sliding window is a nonmember.
        if(n >= start + data.length) { return false; }
        // Inside the sliding window, consult the data array to get the answer.
        return data[(int)(n - start)];
    }
    
    public static void demo() {
        // Demonstration of the principle of "tortoise and hare" where two
        // position indices advance the exact same path, except that hare makes
        // two moves for every one move of tortoise. Two identical RNG's are
        // used to generate the same steps for both. The hare adds the elements
        // it steps to into the set, whereas tortoise adds every element to
        // the set as it verifies that only those elements that hare also jumped
        // into were members of the set.
        NatSet s = new NatSet();
        // Use two identical RNG's to generate the steps for tortoise and hare.
        Random rng1 = new Random(123);
        Random rng2 = new Random(123);
        int t = 0, h = 0; // Positions of tortoise and hare.
        // You can try on your computer how high you can make i go before
        // running out of heap memory needed by the data array.
        for(int i = 0; i < 100_000_000; i++) {
            // Hare moves in every round.
            h += rng1.nextInt(10) + 1;
            s.add(h);
            // Tortoise moves in every other round. 
            if(i % 2 == 1) {
                int next = t + rng2.nextInt(10) + 1;
                t++;
                while(t < next) {
                    assert !s.contains(t) : t + " should not be in the set";
                    s.add(t);
                    t++;
                }
                assert s.contains(t) : t + " should be in the set";
            }
        }
        System.out.println("Ended with hare at " + h + " and tortoise at " + t);
    }
}
