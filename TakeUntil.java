import java.util.*;
import java.util.stream.*;
import java.util.function.*;

/* Adapted from Saint Hill's answer in Stack Overflow. Create a class
   that can be used to convert any stream, finite or infinite, into
   another stream that stops at the first element that satisfies the
   given predicate. */

public class TakeUntil<T> implements Iterator<T> {

    private Iterator<T> it; // The iterator used to access the stream elements.
    private Predicate<T> pr; // The predicate used as stopping criterion.
    private volatile T next; // The next element of the stream, cached inside this object.
    private volatile boolean terminated = false; // Has the stopping criterion has been reached?
    
    // Constructor for the class.
    public TakeUntil(Stream<T> s, Predicate<T> pr) {
        this.it = s.iterator();
        this.pr = pr;
        this.next = null;
    }
    
    // Is there at least one more element coming?
    public boolean hasNext() {
        // Has the stopping criterion been reached?
        if(terminated) { return false; }
        // Is there a cached element?
        if(next != null) { return true; }
        // Otherwise, ask the element from the iterator...
        if(it.hasNext()) {
            next = it.next();
            // ... and check whether that element satisfies the stopping criterion.
            if(terminated = pr.test(next)) { return false; }
        }
        return true;
    }
    
    // Return the next element of the stream. 
    public T next() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        T result = next;
        next = null;
        return result;
    }
    
    // Utility method to use this class to convert any Stream<T> into another Stream<T>
    // that terminates at the first element that satisfies the given predicate.
    public static <T> Stream<T> stream(Stream<T> s, Predicate<T> pred) {
        TakeUntil<T> tu = new TakeUntil<T>(s, pred);
        Spliterator<T> split = Spliterators.spliterator(tu, Integer.MAX_VALUE, Spliterator.ORDERED);
        return StreamSupport.stream(split, false);
    }
    
    // For demonstration purposes, take numbers from the stream of consecutive positive
    // integers until you get to one that is greater than 10.
    public static void demo() {
        TakeUntil.stream(IntStream.range(1, 1_000_000_000).boxed(), x -> x > 10)
        .forEach(System.out::println);
    }  
}