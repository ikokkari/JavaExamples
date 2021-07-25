import java.util.*;
import java.math.*; // for BigInteger

public class CollectionsDemo {
    
    private static final Random rng = new Random();
    
    @SuppressWarnings("unchecked")
    public static void basicOperations() {
        TreeSet<Integer> tsd = new TreeSet<>();
        
        // First, the dynamic set operations add, remove and contains.
        tsd.add(42); tsd.add(99); tsd.add(99);
        System.out.println("Our example treeset: " + tsd); // decent toString() 
        
        // Dumb way to do the following. Used here only for demonstration purposes.
        System.out.println("Looking for elements in the treeset.");
        for(int i = 0; i < 100; i++) {
            if(tsd.contains(i)) { System.out.println("Element " + i + " was found"); }
        }
        tsd.remove(42);
        System.out.println("Looking for elements in the treeset again.");
        for(int i = 0; i < 100; i++) {
            if(tsd.contains(i)) { System.out.println("Element " + i + " was found"); }
        }
        
        // Two collections are equal if they are of same general type with equal elements.
        TreeSet<Integer> tsd2 = new TreeSet<>();
        tsd2.add(99);
        HashSet<Integer> hs = new HashSet<>();
        hs.add(99);
        ArrayList<Integer> al = new ArrayList<>();
        al.add(99);
        System.out.println("tsd equals tsd2: " + tsd.equals(tsd2)); // true
        System.out.println("tsd equals hs: " + tsd.equals(hs)); // true, hs is a Set
        System.out.println("tsd equals al: " + tsd.equals(al)); // false, al is a List
        System.out.println("tsd equals \"Hello\": " + tsd.equals("Hello")); // false, for sure
        
        // Collections contain references, not actual objects.
        ArrayList<Object> ao = new ArrayList<Object>();
        ao.add(42);
        ao.add("Hello world");
        ao.add(ao); // strange but perfectly legal
        System.out.println(ao + " has size of " +  ao.size()); // implicit toString()
        ((Collection<Object>)(ao.get(2))).add(ao); // even more strange, yet still legal
        System.out.println(ao + " has size of " +  ao.size()); // implicit toString()
    }
    
    // The Collections utility class contains many useful polymorphic algorithms.
    public static void algorithmsDemo() {
        ArrayList<Integer> ai = new ArrayList<Integer>();
        for(int i = 0; i < 10; i++) { ai.add(rng.nextInt(1000)); }
        System.out.println("Initial arraylist:");
        System.out.println(ai);
        System.out.println("The largest element is " + Collections.max(ai) + ".");
        System.out.println("Rotating elements 3 steps to right produces this:");
        Collections.rotate(ai, 3); // negative offset would rotate left
        System.out.println(ai);
        
        System.out.println("Sorted arraylist:");
        Collections.sort(ai);
        System.out.println(ai);
        
        System.out.println("Shuffled arraylist:");
        Collections.shuffle(ai);
        System.out.println(ai);
        
        System.out.println("Filled arraylist:");
        Collections.fill(ai, 99);
        System.out.println(ai);
        System.out.println("Value 99 occurs in the list " + Collections.frequency(ai, 99) + " times.");
    }
    
    // Polymorphic methods can be written to work on any collection of any type. The
    // next method works for any subtype of Iterator<Integer> that will ever exist,
    // instead of us having to write a new version of this method for each new subtype.
    // Always remember the DRY Principle: Don't Repeat Yourself!
    
    public static int sum(Iterator<Integer> i) {
        int total = 0;
        while(i.hasNext()) {
            int e = i.next(); // return current element and step forward
            total += e;
        }
        return total;
    }

    // Summing through any collection is now trivial.
    public static int sum(Collection<Integer> c) {
        return sum(c.iterator());
    }    
    
    // There is no law saying that the iterator has to be "backed up" by some actual
    // collection. You can iterate through a virtual set of data, even infinite one.
    // The algorithms that use these iterators don't know the difference, which is
    // the whole point of polymorphism.
    
    public static class FibonacciIterator implements Iterator<BigInteger> {
        private BigInteger a = BigInteger.ZERO;
        private BigInteger b = BigInteger.ONE;
        public boolean hasNext() { return true; } // they never end
        public BigInteger next() {
            BigInteger c = a.add(b);
            a = b; // shift sliding window one step left
            b = c;
            return a;
        }
    }
   
    // Since the Iterator<E> interface is so simple, it is also simple to decorate:
    
    public static class DuplicatingIterator<E> implements Iterator<E> {
        private final int dup; // how many times each value should be duplicated
        private int count; // how many times current value has been duplicated
        private E value = null; // the current value that is being duplicated
        private final Iterator<E> client;
        public DuplicatingIterator(Iterator<E> client, int dup) {
            this.client = client;
            this.dup = this.count = dup;
        }
        
        // The two methods of Iterator interface that we must implement.
        public E next() {
            if(count < dup) { 
                count++; return value; // return cached value
            } 
            else {
                value = client.next(); // consult client for the next value
                count = 1;
                return value;
            }
        }
        
        public boolean hasNext() {
            // If the current element has been given out enough times, this iterator
            // has a next value if and only if its client has a next value.
            if(count == dup) { return client.hasNext(); }
            // When in the middle of duplication, there is always a next value.
            return true;            
        }
    }
    
    public static void FibonacciDemo() {
        Iterator<BigInteger> it = new DuplicatingIterator<>(new FibonacciIterator(), 3);
        for(int i = 0; i < 50; i++) {
            System.out.println(it.next());
        }
    }
    
    // The utility interface Comparator<T> can be used to define Strategy objects
    // that control the way sorting, maximum and other order-based operations work.
    // Here is a wacky custom comparator for integers so that every odd integer is
    // smaller than any even integer.
    
    private static class OddEvenComparator implements Comparator<Integer> {
        // return neg if a < b, pos if a > b, and zero if a = b 
        public int compare(Integer a, Integer b) {
            // One is odd, the other is even
            if(a % 2 != 0 && b % 2 == 0) { return -1; }
            if(a % 2 == 0 && b % 2 != 0) { return +1; }
            // Otherwise compare normally.
            return a.compareTo(b);
        }
    }

    // Another custom comparator that compares numbers in lexicographical order.
    
    private static class LexicalComparator implements Comparator<Integer> {
        public int compare(Integer a, Integer b) {
            return ("" + a).compareTo("" + b); // String has internal compareTo method
        }
    }
    
    // A Comparator decorator that can be placed in front of any existing Comparator.
    // Counting comparator might be handy in measuring various sorting algorithms.
    
    private static class CountingComparator<T> implements Comparator<T> {
        private final Comparator<T> client;
        private int count = 0;
        public CountingComparator(Comparator<T> client) {
            this.client = client;
        }
        public int compare(T a, T b) {
            count++;
            return client.compare(a, b);
        }
        // Decorators may also define brand new methods and functionality. After all,
        // a polymorphic method that receives a Comparator<T> should not care what
        // other functionality these objects have in addition to those required by
        // the Comparator<T> interface.
        public int getCount() { return count; }
    }
    
    public static void demonstrateComparators() {
        ArrayList<Integer> ai = new ArrayList<>();
        for(int i = 0; i < 10; i++) { ai.add(rng.nextInt(1000)); }
        System.out.println("Initial arraylist:");
        System.out.println(ai);
        System.out.println("Sorted normally:");
        Collections.sort(ai);
        System.out.println(ai);
        System.out.println("Largest element is " + Collections.max(ai) + ".");
        
        System.out.println("Sorted with a counting oddeven Comparator:");
        CountingComparator<Integer> comp = new CountingComparator<>(new OddEvenComparator());
        ai.sort(comp);
        System.out.println(ai);
        System.out.println("Sorting needed " + comp.getCount() + " element comparisons.");
        
        System.out.println("Sorted with a counting lexicographic Comparator:");
        comp = new CountingComparator<>(new LexicalComparator());
        ai.sort(comp);
        System.out.println(ai);
        System.out.println("Sorting needed " + comp.getCount() + " element comparisons.");
    }
    
    // Since BlueJ method parameter dialog is so painful for complex types, here is how you
    // would write a test method to leisurely create the argument given to methods that
    // expect parameters of such complex types.
    
    /*
    public static void testSum() {
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(42); al.add(99); al.add(17);
        System.out.println(sum(al));
    }
    */
    
}