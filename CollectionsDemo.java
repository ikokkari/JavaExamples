import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class CollectionsDemo {
    
    private static final Random rng = new Random(4242);
    
    @SuppressWarnings("unchecked")
    public static void basicOperations() {
        System.out.println("Basic operations demo begins.");
        TreeSet<Integer> treeSetOne = new TreeSet<>();
        
        // First, the dynamic set operations add, remove and contains.
        treeSetOne.add(42);
        treeSetOne.add(99);
        treeSetOne.add(17); // Despite added last, will be iterated over first.
        treeSetOne.add(99); // Adding same element second time does nothing.
        System.out.println("Our example treeset: " + treeSetOne); // decent toString()
        
        // Dumb way to do the following. Used here only for demonstration purposes.
        System.out.println("Looking for elements in the treeset.");
        for(int i = 0; i < 100; i++) {
            if(treeSetOne.contains(i)) { System.out.println("Element " + i + " was found"); }
        }
        treeSetOne.remove(42);
        treeSetOne.remove(42); // Not an error, just does nothing.
        treeSetOne.remove(17);
        System.out.println("Looking for elements in the treeset again.");
        for(int i = 0; i < 100; i++) {
            if(treeSetOne.contains(i)) { System.out.println("Element " + i + " was found"); }
        }
        
        // Two collections are equal if they are of same general type with equal elements.
        TreeSet<Integer> treeSetTwo = new TreeSet<>();
        treeSetTwo.add(99);
        HashSet<Integer> hashSet = new HashSet<>();
        hashSet.add(99);
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(99);
        System.out.println("treeSetOne equals treeSetTwo: " + treeSetOne.equals(treeSetTwo)); // true
        System.out.println("treeSetOne equals hashSet: " + treeSetOne.equals(hashSet)); // true, hashSet is a Set
        System.out.println("treeSetOne equals arrayList: " + treeSetOne.equals(arrayList)); // false, arrayList is a List
        System.out.println("treeSetOne equals \"Hello\": " + treeSetOne.equals("Hello")); // false, for sure
        
        // Collections contain references, not actual objects.
        ArrayList<Object> ao = new ArrayList<Object>();
        ao.add(42);
        ao.add("Hello world");
        ao.add(ao); // This does not become an infinitely deeply nested matryoshka doll.
        System.out.println(ao + " has size of " +  ao.size()); // implicit toString()
        ((Collection<Object>)(ao.get(2))).add(ao); // even more strange, yet still legal
        System.out.println(ao + " has size of " +  ao.size()); // implicit toString()
        System.out.println("Basic operations demo ends.\n\n");
    }
    
    // The Collections utility class contains many useful polymorphic algorithms.
    public static void algorithmsDemo() {
        System.out.println("Algorithms demo begins.");
        ArrayList<Integer> ai = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            ai.add(rng.nextInt(1000));
        }
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
        System.out.println("Algorithms demo ends.\n\n");
    }
    
    // Polymorphic methods can be written to work on any collection of any type. The
    // next method works for any subtype of Iterator<Integer> that will ever exist,
    // instead of us having to write a new version of this method for each new subtype.
    // Always remember the DRY Principle: Don't Repeat Yourself!
    
    public static int sum(Iterator<Integer> iterator) {
        int total = 0;
        while(iterator.hasNext()) {
            int e = iterator.next(); // return current element and step forward
            total += e;
        }
        return total;
    }

    // Summing through any collection is now trivial.
    public static int sum(Collection<Integer> c) {
        return sum(c.iterator());
    }    
    
    // There is no law dictating that the iterator has to be "backed up" by some actual
    // collection. You can iterate through a virtual sequence of data, even infinite.
    // The algorithms that use these iterators don't know the difference nor do they
    // even have to care, which is the whole point of polymorphism.
    
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
    
    public static void fibonacciDemo() {
        System.out.println("\n\nFibonacci demo begins.");
        System.out.println("Printing first 50 Fibonacci numbers duplicated.");
        Iterator<BigInteger> it = new DuplicatingIterator<>(new FibonacciIterator(), 3);
        for(int i = 0; i < 50; i++) {
            System.out.println(it.next());
        }
        System.out.println("Fibonacci demo ends.\n\n");
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
    
    private static class LexicographicComparator implements Comparator<Integer> {
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
        System.out.println("Comparators demo begins.");
        List<Integer> ai = new ArrayList<>();
        for(int i = 0; i < 10; i++) { ai.add(rng.nextInt(1000)); }
        System.out.println("Initial arraylist:");
        System.out.println(ai);
        System.out.println("Sorted normally:");
        Collections.sort(ai);
        System.out.println(ai);
        System.out.println("Largest element is " + Collections.max(ai) + ".");
        
        System.out.println("Sorted with a counting OddEvenComparator:");
        CountingComparator<Integer> comp = new CountingComparator<>(new OddEvenComparator());
        Collections.sort(ai, comp);
        System.out.println(ai);
        System.out.println("Sorting needed " + comp.getCount() + " element comparisons.");
        
        System.out.println("Sorted with a counting lexicographic Comparator:");
        comp = new CountingComparator<>(new LexicographicComparator());
        Collections.sort(ai, comp);
        System.out.println(ai);
        System.out.println("Sorting needed " + comp.getCount() + " element comparisons.");
        System.out.println("Comparators demo ends.");
    }

    public static void main(String[] args) {
        basicOperations();
        algorithmsDemo();
        demonstrateComparators();
        fibonacciDemo();
    }
}