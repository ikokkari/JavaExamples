import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashSet;

// An immensely useful class that for some reason is missing from
// the Java standard library. (Google "why java has no pair class")

public class Pair<T, U> {
    
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
        count++; this.first = first; this.second = second;
    }
    
    public T getFirst() { return first; }
    public U getSecond() { return second; }
    public void setFirst(T first) { this.first = first; }
    public void setSecond(U second) { this.second = second; }
    
    @Override public String toString() {
        return "[" + getFirst() + ", " + getSecond() + "]";
    }
    
    @Override public boolean equals(Object o) {
        if(o instanceof Pair) { // the most we can check at runtime
            Pair p = (Pair) o; // downcast to known type
            return this.getFirst().equals(p.getFirst()) &&
              this.getSecond().equals(p.getSecond());
        }
        else { return false; }
    }
    
    @Override public int hashCode() {
        // When creating hash functions, bit shifts and xor are your helpful friends.
        // Hash code of object is computed based on precisely those fields that can
        // affect the equality comparison of those objects inside the equals method.
        int f1 = getFirst().hashCode();
        int f2 = getSecond().hashCode();
        // Swap top and bottom nybbles so that pairs (a, b) and (b, a) hash differently.
        f1 = (f1 >> 16) | (f1 << 16); 
        // Operator ^ is the bitwise exclusive or.
        int result = (f1 ^ f2);
        // Last, use bitwise and to ensure that the highest (sign) bit is zero.
        return result & 0x7fffffff;  
    }
    
    // Read the words from War and Peace and count how many different hash codes we
    // get for the (word, idx) pairs generated from that data.
    public static void sampleHashCodes() throws IOException {
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