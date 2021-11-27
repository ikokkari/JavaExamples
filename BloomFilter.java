import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

// A probabilistic monotonic set that is extremely space efficient even for large n,
// but allows for a small chance of false positives. False negatives are impossible.
// This is good enough for many practical applications of such a space-efficient set.

public class BloomFilter<E> {

    // Needed to randomly initialize the parameters as and bs.
    private static final Random rng = new Random();
    
    // Family of multiplicative hash functions h(x) = (a*x + b) % M
    private final int[] as;
    private final int[] bs;
    private final boolean[] bits;
    
    // Function to convert element into an integer. If null, the result of
    // calling the method hashCode() is used instead.
    private final Function<E, Integer> conv;
    
    /**
     * Constructor to initialize the Bloom filter.
     * @param k The number of hash functions to use.
     * @param m The number of bits to use to store the element hashes.
     * @param conv The function to convert each element into an integer.
     */
    public BloomFilter(int k, int m, Function<E, Integer> conv) {
        this.conv = conv;
        bits = new boolean[m];
        as = new int[k];
        bs = new int[k];
        for(int i = 0; i < k; i++) {
            as[i] = rng.nextInt();
            bs[i] = rng.nextInt();
        }
    }
    
    public BloomFilter(int k, int m) { this(k, m, null); }
    
    /**
     * Add the given element to this Bloom filter.
     * @param elem The new element to be added.
     */
    public void add(E elem) {
        int h = conv == null ? elem.hashCode() : conv.apply(elem);
        for(int i = 0; i < as.length; i++) {
            bits[Math.abs(as[i] * h + bs[i]) % bits.length] = true;
        }
    }
    
    /**
     * Check if the given element has been added to this Bloom filter.
     * @param elem The element to search for.
     * @return Whether the element is currently in this Bloom filter.
     */
    public boolean probablyContains(E elem) {
        int h = conv == null ? elem.hashCode() : conv.apply(elem);
        for(int i = 0; i < as.length; i++) {
            if(!bits[Math.abs(as[i] * h + bs[i]) % bits.length]) {
                return false; // negative answer is guaranteed correct 
            }
        }
        return true; // true with high probability
    }
    
    // A utility method needed for the demonstration done in the main method.
    private static String createWord(Set<String> already) {
        StringBuilder word;
        do {
            word = new StringBuilder();
            int len = rng.nextInt(10) + 2;
            for(int j = 0; j < len; j++) {
                word.append((char) ('a' + rng.nextInt(26)));
            }
        } while(already.contains(word.toString()));
        return word.toString();
    }
    
    // For amusement and demonstration purposes.
    public static void main(String[] args) {
        // Our Bloom filter uses 128 kilobytes (plus some spare change) to store
        // the bits, the size of element type E makes no difference to anything.
        // Also try out how changing k and m affects the false positive percentage.
        BloomFilter<String> wordBloom = new BloomFilter<>(20, 128 * 1024 * 8);
        // The hash set will take a lot more memory than 128 kb to store the same
        // strings in the 100% accurately retrievable and exact fashion.
        HashSet<String> wordHash = new HashSet<>();
        
        // Populate both collections with the same set of randomly created "words".
        for(int i = 0; i < 50000; i++) {
            String word = createWord(wordHash);
            wordBloom.add(word);
            wordHash.add(word);
        }
        // Just to make sure that our filter works, check that it says yes to all added words.
        for(String word: wordHash) {
            if(!wordBloom.probablyContains(word)) {
                System.out.println("Bloom filter implementation is broken!");
                return;
            }
        }
        
        // Create one million random "words" that are different from our earlier words, and
        // count for how many of those new words our Bloom filter returns a false positive.
        int falsePosCount = 0;
        for(int i = 0; i < 1000000; i++) {
            String word = createWord(wordHash);
            if(wordBloom.probablyContains(word)) { falsePosCount++; }
        }
        double falsePosProb = falsePosCount / 1000000.0;
        System.out.printf("Got %d false positives out of one million words, so P(posAns | neg) = %.6f.\n",
            falsePosCount, falsePosProb);
    }
}