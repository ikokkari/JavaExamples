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
        int f1 = getFirst().hashCode();
        int f2 = getSecond().hashCode();
        f1 = (f1 >> 16) | (f1 << 16);
        return f1 ^ f2; // Not exponentiation, but bitwise xor
    }
    
    public static void sampleHashCodes() {
        java.util.HashSet<Integer> seen = new java.util.HashSet<>();
        java.util.Random rng = new java.util.Random(12345);
        for(int i = 0; i < 100000; i++) {
            int a = rng.nextInt();
            int b = rng.nextInt();
            seen.add(new Pair<Integer, Integer>(a, b).hashCode());            
        }
        System.out.println("Seen " + seen.size() + " hash codes.");
    }
}