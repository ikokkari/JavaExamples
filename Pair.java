import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashSet;

// An immensely useful class that for some reason is missing from
// the Java standard library. (Google "why java has no pair class")

public final class Pair<T, U> {
    
    // A generic class can have static members, but they can't
    // use the type arguments in any way (because of erasure).
    private static int count = 0;
    public static int getCount() { return count; }
    
    // Type arguments can be used in instance fields and methods
    // as placeholders for actual type arguments given later by
    // the users of this class.
    private T first;
    private U second;
    
    public Pair(T first, U second) {
        count++;
        this.first = first;
        this.second = second;
    }
    
    public T getFirst() { return first; }
    public U getSecond() { return second; }

    // Uncomment these two lines if you prefer a mutable pair.
    // public void setFirst(T first) { this.first = first; }
    // public void setSecond(U second) { this.second = second; }
    
    @Override public String toString() {
        return "[" + getFirst() + ", " + getSecond() + "]";
    }
    
    @Override public boolean equals(Object other) {
        if(other instanceof Pair) { // The most we can check at runtime.
            Pair otherPair = (Pair) other; // Downcast to Pair to access its getFirst and getSecond.
            return this.getFirst().equals(otherPair.getFirst()) &&
                    this.getSecond().equals(otherPair.getSecond());
        }
        else { return false; }
    }
    
    @Override public int hashCode() {
        // When creating hash functions, bit shifts and xor are your helpful friends.
        // Hash code of object is computed based on precisely those fields that can
        // affect the equality comparison of those objects under the equals method.
        int h1 = getFirst().hashCode();
        int h2 = getSecond().hashCode();
        // Swap top and bottom halves of h1 so that pairs (a, b) and (b, a) will hash
        // differently. (This part is optional, but can't really hurt us either.)
        h1 = (h1 >> 16) | ((h1 & 0xFFFF) << 16);
        // Combine the hash codes of these fields with the exclusive or operator ^.
        int result = h1 ^ h2;
        // Last, use the bitwise and to ensure that the highest (sign) bit is zero.
        return result & 0x7FFFFFFF;
    }
    
    // Read all words from War and Peace and count how many hash codes we get for
    // the (word, idx) pairs generated from that data. The higher that number is,
    // the better your hash function works in practice with hash sets and maps.
    public static void main(String[] args) throws IOException {
        HashSet<Integer> seen = new HashSet<>();
        int wordNo = 0;
        try(Scanner sc = new Scanner(new File("warandpeace.txt"))) {        
            while(sc.hasNextLine()) {
                for(String word: sc.nextLine().split(" ")) {
                    seen.add(new Pair<>(word, wordNo++).hashCode());
                }
            }
        }
        System.out.println("We got " + seen.size() + " different hash codes for " + wordNo + " words.");
    }
}