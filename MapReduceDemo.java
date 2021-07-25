import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.math.*;

public class MapReduceDemo {

    // IntPredicate object defined as a lambda: the compiler expands
    // all that anonymous subclass boilerplate rigmarole automatically.
    // This predicate is stateless, so it can be used in a parallel
    // stream of integers as well as sequential stream.
    private static final IntPredicate primeTester = n -> {
        if(n < 2) { return false; }
        if(n == 2) { return true; }
        if(n % 2 == 0) { return false; }
        for(int i = 3; i * i <= n; i += 2) {
            if(n % i == 0) { return false; }
        }
        return true;
    }; 

    public static void main(String[] args) {
        // Compute the sum of squares of 100 smallest distinct prime numbers
        // generated from the floors of the decimal numbers generated from
        // the given rng, but excluding the first ten.
        System.out.println("The sum of asked squares is " + 
            new Random(12345).doubles()
            .map(x -> Math.sqrt(x * 1_000_000 + 3))
            .mapToInt(x -> (int)Math.floor(x))
            .filter(primeTester) // longer way: .filter(x -> primeTester.test(x))
            .skip(10) // skip the first 10
            .limit(90) // and take the first 90 of the rest
            .map(e -> e * e) // square the numbers
            .reduce(0, Integer::sum) // and add them up
        );
        
        // Many other standard classes in Java are retrofitted to generate streams.
        List<Integer> nums = Arrays.asList(16, 15, 22, 9, 7, 82, 17);
        System.out.println("The first prime number in the list is " + nums.stream()
        .mapToInt(Integer::intValue)
        .filter(primeTester)
        .findFirst() // An OptionalInt, as stream could be empty (this one isn't)
        .orElse(-1) // This is an OptionalInt method, not a Collector
        );
        
        nums = Arrays.asList(4, 8, 15); // Let's try the same thing with no primes.
        System.out.println("The first prime number in the list is " + nums.stream()
        .mapToInt(x -> x)
        .filter(primeTester)
        .findFirst()
        .orElse(-1));
        
        System.out.println("Lazy evaluation with long, even infinite streams");
        Stream.generate( () -> 42 ) // A stateless infinite stream 42, 42, 42, ...
            .limit(10) // that we cap to the maximum length of 10
            .forEach(System.out::println); // to print out the elements
        
        // A huge stream 0, 1, 2, ... , 1,000,000,000. No problem, since Java 8
        // streams are evaluated in a lazy fashion.
        IntStream is = IntStream.rangeClosed(0, 1_000_000_000)
            .filter(primeTester.negate()); // All predicates have negate() as a default method
            
        // Notice how the stream has been defined, but no computation takes place
        // yet. The stream itself is an object, just sitting there in the memory,
        // and it can be assigned to a variable, passed to a method, or returned
        // as a result. Attaching some collector to the end of the stream will then
        // actually launch the evaluation by requesting elements from the stream.
        
        System.out.println("\nThe first 20 nonprimes are " +
            is.limit(20).boxed().collect(Collectors.toList())
        );
        
        // Infinite stream of random numbers. Again, laziness prevents this computation
        // from falling into an infinite loop.
        System.out.println("Here are some filtered random numbers.");
        Random rng = new Random();
        System.out.println(
            Stream.generate(rng::nextDouble)
            .filter(x -> x < 0.5)
            .limit(10)
            .map(Object::toString)
            .collect(Collectors.joining(", ", "<< ", " >>")) // French style quote marks
        );

        // The interface Supplier can be used the same way as generators of other languages.
        // Suppliers can then be turned into infinite streams. Here is a supplier of Fibonacci
        // numbers. Since Fibonacci numbers grow exponentially, use BigInteger representation. 
        class FibSupplier implements Supplier<BigInteger> {
            BigInteger a = BigInteger.ZERO; // Internal state of the supplier.
            BigInteger b = BigInteger.ONE;
            public BigInteger get() { // The method called to supply the next element.
                BigInteger result = a;
                BigInteger c = a.add(b);
                a = b;
                b = c;
                return result;
            }
        }
        
        // Stateful predicates with fields cannot be implement as lambdas. But they are
        // still classes, so we can write them explicitly as such. Also, the result of
        // the predicate can depend on things other than the element, although such a
        // stateful predicate would be nondeterministic if used in a parallel stream.
        class CountPredicate<T> implements Predicate<T> {
            private final int limit;
            private int count;
            public CountPredicate(int limit) {
                this.limit = limit;
            }
            public boolean test(T value) {
                count = (count + 1) % limit;
                return (count == 0);
            }
        }

        // Combine the previous two classes to create a stream of skipped Fibonacci numbers.
        System.out.println("Here is every fifth Fibonacci number:");
        Stream.generate(new FibSupplier())
            //.parallel() // uncomment this line for some goofy nondeterminism
            .filter(new CountPredicate<>(5))
            .limit(100)
            .forEach(System.out::println); // method reference with :: operator    
        
        // flatMap is a handy stream operator to expand individual elements to many elements.
        System.out.println("Prefix of the \"pyramid series\" generated with flatMap: ");
        // Generate the stream 1, 2, 2, 3, 3, 3, 4, 4, 4, 4, ...
        IntStream.rangeClosed(1, 1_000_000_000) // enough
        .flatMap(e -> IntStream.rangeClosed(1, e).map(x -> e))
        .limit(50)
        .forEach(x -> System.out.print(x + " "));
    }    
}