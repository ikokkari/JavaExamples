import java.util.*;
import java.math.*; // for BigInteger

public class CollectionsDemo {
    
    private static Random rng = new Random();
    
    @SuppressWarnings("unchecked")
    public static void basicOperations() {
        TreeSet<Integer> tsd = new TreeSet<Integer>();
        
        // First, the dynamic set operations add, remove and contains.
        tsd.add(42); tsd.add(99);
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
        
        // Two collections are equal if they are of same type and have equal elements.
        TreeSet<Integer> tsd2 = new TreeSet<Integer>();
        tsd2.add(99);
        HashSet<Integer> hs = new HashSet<Integer>();
        hs.add(99);
        ArrayList<Integer> al = new ArrayList<Integer>();
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
        for(int i = 0; i < 20; i++) { ai.add(rng.nextInt(1000)); }
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
        System.out.println("99 occurs in the list " + Collections.frequency(ai, 99) + " times.");
        
        // A sneak preview of lecture 10: lambda expressions make strategy object creation easy.
        ai.clear();
        for(int i = 0; i < 20; i++) { ai.add(rng.nextInt(1000)); }
        System.out.println("\nHere are some new values for the arraylist:");
        System.out.println(ai);
        System.out.println("This arraylist sorted in reverse order:");
        Collections.sort(ai, (x, y) -> y - x);
        System.out.println(ai);
        System.out.println("This arraylist sorted in dictionary order:");
        Collections.sort(ai, (x, y) -> (x + "").compareTo(y + ""));
        System.out.println(ai);
    }
    
    // Polymorphic methods can be written to work on any collection of any type. The
    // next method works for any subtype of Iterator<Integer> that will ever exist,
    // instead of us having to write a new version of this method for each new subtype.
    // Remember the DRY Principle: Don't Repeat Yourself!
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
    
    // Speaking of iteration, you can create as many iterators to the same collection as
    // you want. Each iterator can advance (and for list iterators, retreat) independently
    // of others. (But modifying a collection while it is being iterated is a bad idea.)
    
    // Since the Iterator interface is so simple, it is easy to decorate:
    
    public static class DuplicatingIterator<E> implements Iterator<E> {
        private int dup, count;
        private E value = null;
        private Iterator<E> it;
        public DuplicatingIterator(Iterator<E> it, int dup) {
            this.it = it; this.dup = this.count = dup;
        }
        
        // The two methods of Iterator interface that we must implement.
        public E next() {
            if(count < dup) { count++; return value; } // return cached value
            else {
                value = it.next();
                count = 1;
                return value;
            }
        }
        
        public boolean hasNext() {
            if(count == dup) { return it.hasNext(); } // ask the underlying iterator
            return true; // cached element is certainly there            
        }
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
   
    public static void FibonacciDemo() {
        Iterator<BigInteger> it = new DuplicatingIterator<BigInteger>(new FibonacciIterator(), 2);
        for(int i = 0; i < 50; i++) {
            System.out.println(it.next());
        }
    }
    
    // A custom implementation of a Collection subclass, overriding only some of the
    // methods to do something a bit extra. Here is a subclass that behaves otherwise
    // like a HashSet<Integer>, except that it keeps a running total of elements that
    // have been added.
    private static class SummingHashSet extends HashSet<Integer> {
        private int total = 0; // internal tally of elements that were added
        public boolean add(Integer i) {
            boolean success = super.add(i);
            if(success) { total += i; }
            return success;
        }
        public int getTotal() { return total; }
    }
    
    // (The previous would be better done with a decorator, but the Java standard library
    // does not have a convenient Collection decorator class that we could extend, and
    // I just don't feel like writing a dummy implementation of every Collection method
    // that calls the corresponding method of the underlying collection object.)
    
    public static void demonstrateSummingHashSet(int n) {
        SummingHashSet shs = new SummingHashSet();
        for(int i = 0; i < n; i++) { shs.add(rng.nextInt(1000)); }
        System.out.println("The set contains " + shs.size() + " elements.");
        System.out.println("Its internal total is " + shs.getTotal() + ".");
        System.out.println("The external function sum returns " + sum(shs) + ".");
        System.out.println("Using a duplicating iterator times ten, we get "
          + sum(new DuplicatingIterator<Integer>(shs.iterator(), 10)) + ".");
    }
    
    // The utility interface Comparator<T> can be used to define Strategy objects
    // that control the way sorting, maximum and other order-based operations work.
    // Here a custom comparator for integers so that every odd integer is considered
    // smaller than any even integer.
    private static class OddEvenComparator implements Comparator<Integer> {
        // return neg if a < b, pos if a > b, and zero if a = b 
        public int compare(Integer a, Integer b) {
            // One is odd, the other is even
            if(a % 2 != 0 && b % 2 == 0) { return -1; }
            if(a % 2 == 0 && b % 2 != 0) { return +1; }
            // Otherwise compare normally.
            if(a < b) { return -1; }
            if(a > b) { return +1; }
            return 0;
        }
    }

    // The compare method in any Comparator subclass should semantically be
    // transitive and reflexive, but there is no way for the compiler to
    // enforce this (semantic properties of code are in general unsolvable).
    
    // Another custom comparator that compares numbers in lexicographical order.
    private static class LexicalComparator implements Comparator<Integer> {
        public int compare(Integer a, Integer b) {
            return ("" + a).compareTo("" + b); // String has internal compareTo method
        }
    }
    
    // A Comparator decorator that can be placed in front of any existing Comparator.
    // Counting comparator might be handy in measuring the behaviour of sorting algs.
    private static class CountingComparator<T> implements Comparator<T> {
        private Comparator<T> comp;
        private int count = 0;
        public CountingComparator(Comparator<T> comp) {
            this.comp = comp;
        }
        public int compare(T a, T b) {
            count++;
            return comp.compare(a, b);
        }
        public int getCount() { return count; }
    }
    
    public static void demonstrateComparators() {
        ArrayList<Integer> ai = new ArrayList<Integer>();
        for(int i = 0; i < 10; i++) { ai.add(rng.nextInt(1000)); }
        System.out.println("Initial arraylist:");
        System.out.println(ai);
        System.out.println("Sorted normally:");
        Collections.sort(ai);
        System.out.println(ai);
        System.out.println("Largest element is " + Collections.max(ai));
        
        System.out.println("Sorted with a counting oddeven Comparator:");
        CountingComparator<Integer> comp = new CountingComparator<Integer>(new OddEvenComparator());
        Collections.sort(ai, comp);
        System.out.println(ai);
        System.out.println("Sorting needed " + comp.getCount() + " element comparisons.");
        
        System.out.println("Sorted with a counting lexicographic Comparator:");
        comp = new CountingComparator<Integer>(new LexicalComparator());
        Collections.sort(ai, comp);
        System.out.println(ai);
        System.out.println("Sorting needed " + comp.getCount() + " element comparisons.");
    }
}