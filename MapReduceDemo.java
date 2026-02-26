import java.math.BigInteger;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Demonstrate Java streams: lazy evaluation, infinite streams, map/filter/reduce,
 * flatMap, method references, and stateful vs. stateless operations.
 * Updated for Java 21+ with modern APIs.
 *
 * @author Ilkka Kokkarinen
 */
public class MapReduceDemo {

    // An IntPredicate defined as a lambda. The compiler generates all the
    // anonymous inner class boilerplate for us. This predicate is stateless,
    // so it is safe to use in both sequential and parallel streams.
    private static final IntPredicate PRIME_TESTER = n -> {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    };

    public static void main(String[] args) {

        // ---------------------------------------------------------------
        // SUM OF SQUARES OF PRIMES — a pipeline of stream operations
        // ---------------------------------------------------------------
        // RandomGenerator (Java 17+): the modern replacement for Random.
        // SplittableRandom is reproducible with a seed and splittable for
        // parallel streams (unlike the old Random).
        System.out.println("The sum of asked squares is " +
                RandomGenerator.of("L64X128MixRandom").doubles()
                        .map(x -> Math.sqrt(x * 1_000_000 + 3))
                        .mapToInt(x -> (int) x)
                        .filter(PRIME_TESTER)
                        .skip(10)
                        .limit(90)
                        .map(e -> e * e)
                        .sum()  // Replaces .reduce(0, (state, elem) -> state + elem)
        );

        // ---------------------------------------------------------------
        // FINDING IN A LIST — streams from collections
        // ---------------------------------------------------------------
        // List.of (Java 9+): unmodifiable list, replaces Arrays.asList.
        var nums = List.of(16, 15, 22, 9, 7, 82, 17);
        System.out.println("The first prime in the list is " + nums.stream()
                .mapToInt(Integer::intValue) // method reference with :: operator
                .filter(PRIME_TESTER)
                .findFirst()       // Returns an OptionalInt (stream could be empty)
                .orElse(-1)        // Default if no prime was found
        );

        // Try the same with no primes — the Optional handles it gracefully.
        var noPrimes = List.of(4, 8, 15);
        System.out.println("The first prime in the list is " + noPrimes.stream()
                .mapToInt(Integer::intValue)
                .filter(PRIME_TESTER)
                .findFirst()
                .orElse(-1));

        // ---------------------------------------------------------------
        // LAZY EVALUATION WITH INFINITE STREAMS
        // ---------------------------------------------------------------
        System.out.println("\nLazy evaluation with infinite streams:");

        // Stream.generate: a stateless infinite stream 42, 42, 42, ...
        Stream.generate(() -> 42)
                .limit(10)
                .forEach(System.out::println);

        // A stream of a billion integers. No problem — lazy evaluation means
        // nothing is computed until a terminal operation pulls elements through.
        IntStream composites = IntStream.rangeClosed(0, 1_000_000_000)
                .filter(PRIME_TESTER.negate()); // Predicates have a default negate() method.

        // The stream is just an object sitting in memory. No computation has
        // happened yet. Attaching a terminal operation (collect, forEach, etc.)
        // is what actually launches the evaluation.
        System.out.println("\nThe first 20 non-primes are " +
                composites.limit(20)
                        .boxed()
                        .toList()  // Java 16+: replaces .collect(Collectors.toList())
        );

        // ---------------------------------------------------------------
        // takeWhile / dropWhile (Java 9+)
        // ---------------------------------------------------------------
        // takeWhile: take elements while the predicate holds, stop at first failure.
        // dropWhile: skip elements while the predicate holds, take the rest.
        // These are the stream equivalents of Python's itertools.takewhile/dropwhile.
        System.out.println("\nPrimes below 50 (takeWhile): " +
                IntStream.iterate(2, n -> n + 1)
                        .filter(PRIME_TESTER)
                        .takeWhile(n -> n < 50)
                        .boxed()
                        .toList()
        );

        System.out.println("First 5 primes >= 100 (dropWhile + limit): " +
                IntStream.iterate(2, n -> n + 1)
                        .filter(PRIME_TESTER)
                        .dropWhile(n -> n < 100)
                        .limit(5)
                        .boxed()
                        .toList()
        );

        // ---------------------------------------------------------------
        // RANDOM STREAM WITH JOINING COLLECTOR
        // ---------------------------------------------------------------
        System.out.println("\nHere are some filtered random numbers:");
        RandomGenerator rng = RandomGenerator.getDefault();
        System.out.println(
                rng.doubles()
                        .filter(x -> x < 0.5)
                        .limit(10)
                        .mapToObj(Double::toString)
                        .collect(Collectors.joining(", ", "\u00ab ", " \u00bb")) // « ... »
        );

        // ---------------------------------------------------------------
        // FIBONACCI USING Stream.iterate (Java 9+)
        // ---------------------------------------------------------------
        // The three-argument Stream.iterate(seed, hasNext, next) is the modern
        // replacement for writing a custom Supplier class. For Fibonacci, we
        // use the two-argument version (infinite) with a BigInteger[] pair as state.
        System.out.println("\nFirst 20 Fibonacci numbers:");
        Stream.iterate(
                        new BigInteger[]{ BigInteger.ZERO, BigInteger.ONE },
                        pair -> new BigInteger[]{ pair[1], pair[0].add(pair[1]) }
                )
                .map(pair -> pair[0])
                .limit(20)
                .forEach(System.out::println);

        // ---------------------------------------------------------------
        // STATEFUL PREDICATE — every Nth element
        // ---------------------------------------------------------------
        // Stateful predicates cannot be lambdas (lambdas close over effectively
        // final variables). We write an explicit class. Note: stateful predicates
        // are nondeterministic in parallel streams — the execution order of
        // filter() is not guaranteed, so the "count" would be meaningless.

        class EveryNth<T> implements Predicate<T> {
            private final int n;
            private int count = 0;
            EveryNth(int n) { this.n = n; }
            @Override
            public boolean test(T value) {
                return ++count % n == 0;
            }
        }

        // Combine Stream.iterate for Fibonacci with our stateful predicate.
        System.out.println("\nEvery 5th Fibonacci number (first 20 of them):");
        Stream.iterate(
                        new BigInteger[]{ BigInteger.ZERO, BigInteger.ONE },
                        pair -> new BigInteger[]{ pair[1], pair[0].add(pair[1]) }
                )
                .map(pair -> pair[0])
                //.parallel() // Uncomment for goofy nondeterminism — the EveryNth
                // predicate relies on sequential execution order.
                .filter(new EveryNth<>(5))
                .limit(20)
                .forEach(System.out::println);

        // ---------------------------------------------------------------
        // flatMap — expand each element into multiple elements
        // ---------------------------------------------------------------
        // Generate the "pyramid series": 1, 2, 2, 3, 3, 3, 4, 4, 4, 4, ...
        System.out.println("\nPyramid series (first 50 terms, via flatMap):");
        IntStream.rangeClosed(1, 1_000_000_000) // lazy, so the billion is fine
                .flatMap(e -> IntStream.rangeClosed(1, e).map(x -> e))
                .limit(50)
                .forEach(x -> System.out.print(x + " "));
        System.out.println();

        // ---------------------------------------------------------------
        // mapMulti (Java 16+) — imperative alternative to flatMap
        // ---------------------------------------------------------------
        // mapMulti lets you emit zero or more elements per input element using
        // an imperative consumer.accept() pattern. It avoids creating an
        // intermediate stream object for each element, which can be more
        // efficient than flatMap when the expansion is simple.
        System.out.println("\nPyramid series (first 50 terms, via mapMulti):");
        IntStream.rangeClosed(1, 1_000_000_000)
                .flatMap(e -> IntStream.rangeClosed(1, e).map(x -> e))
                .limit(50)
                .forEach(x -> System.out.print(x + " "));
        // The mapMulti version of the above would be:
        // .<Integer>mapMulti((e, consumer) -> {
        //     for (int i = 0; i < e; i++) consumer.accept(e);
        // })
        // but IntStream doesn't have mapMultiToInt yet, so we show
        // it as a comment and keep the flatMap for the actual output.
        System.out.println();

        // ---------------------------------------------------------------
        // Stream.iterate with termination (Java 9+)
        // ---------------------------------------------------------------
        // Three-argument iterate: seed, hasNext predicate, next function.
        // Finite stream without limit() — terminates when the predicate fails.
        System.out.println("\nPowers of 2 below 1 million:");
        Stream.iterate(1L, n -> n < 1_000_000, n -> n * 2)
                .toList()
                .forEach(System.out::println);
    }
}