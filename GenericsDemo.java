import java.util.*;

public class GenericsDemo {

    // From Java 5 on, the Collection classes are generic. Let's see if we can do something
    // there. Here is a method that checks for any kind of List if its elements are unique,
    // that is, there are no two duplicate elements anywhere in the list.
    public static boolean allUnique(List<?> list) {
        for(int i = 0; i < list.size() - 1; i++) {
            for(int j = i+1; j < list.size(); j++) {
                if(list.get(i).equals(list.get(j))) return false;
            }
        }
        return true;        
    }
    
    // Type parameter upper and lower bounds are occasionally handy. When a method takes
    // named typed parameters not already declared in the class, we need to declare them
    // in front of the method type.
    public static <T> void addAll(Collection<T> src, Collection<? super T> tgt) {
        for(T elem: src) { tgt.add(elem); }
    }
    
    // The next method can be used to check if the list of type List<T> is sorted, but only
    // if T is some type that implements the interface Comparable<T>.
    public static <T extends Comparable<T>> boolean isSorted(List<T> list) {
        for(int i = 1; i < list.size(); i++) {
            if(list.get(i).compareTo(list.get(i-1)) < 0) return false;
        }
        return true;
    }
    
    // Demonstration how generics and type wildcards work.
    public static void main(String[] args) {
        Pair<String, Integer> p1 = new Pair<String, Integer>("Hello", 42);
        Pair<String, Integer> p2 = new Pair<String, Integer>("Hello", 42);
        Pair<String, Integer> p3 = new Pair<String, Integer>("World", 17);
        Pair<Double, Pair<String, Integer>> p4 = new Pair<>(1.234, p1);
        
        System.out.println("Pair count is now " + Pair.getCount()); // 4
        System.out.println("p1 equals p2? " + p1.equals(p2)); // true
        System.out.println("p1 equals p3? " + p1.equals(p3)); // false
        System.out.println("p1 same type as p4? "
          + (p1.getClass() == p4.getClass())); // true
        
        // A collection using type wildcards to allow heterogeneous content.
        ArrayList<Pair<?, ?>> pairs = new ArrayList<>();
        pairs.add(p1);
        pairs.add(p2);
        pairs.add(p3);
        pairs.add(p4);
        System.out.println(pairs); // entire collection
        System.out.println("All unique? " + allUnique(pairs)); // false
        Pair<?, ?> elem = pairs.get(2); // one element
        System.out.println("We pulled out the element " + elem);
        // We can't assume anything about types of first and second here.
        // They just are some kind of objects, but that's all the type
        // system knows for the purposes of runtime type safety.
        
        // A more specifically typed collection, which then allows us to
        // assume more about its element types.
        Set<Pair<String, Integer>> pairSet = new HashSet<>();
        pairSet.add(p1);
        pairSet.add(p2); // already there
        pairSet.add(p3);
        int tally = 0;
        for(Pair<String, Integer> p: pairSet) {
            // We know that getSecond() will return an integer.
            tally += p.getSecond(); // So this is legal.
        }
        System.out.println("Final tally is " + tally);
        
        // Another list, this time containing elements that are Comparable.
        List<Integer> a = Arrays.asList(1, 2, 3, 5, 4); // a vararg utility method
        System.out.println("All unique? " + allUnique(a)); // true
        System.out.println("Is sorted? " + isSorted(a)); // false
        List<Object> b = new ArrayList<Object>(); // a list of arbitrary elements
        b.add("Hello world");
        addAll(a,b); // in a call to generic method, compiler infers type parameters
        System.out.println("The list is now " + b); // [Hello World, 1, 2, 3, 4, 5];
    }
}