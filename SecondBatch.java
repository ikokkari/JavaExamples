import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * A second batch of examples demonstrating arrays, loops, and numerical
 * algorithms for the course CCPS 109.
 * Updated for Java 21+ with modern idioms and language features.
 *
 * @author Ilkka Kokkarinen
 */

public class SecondBatch {

    // -----------------------------------------------------------------------
    // Count how many elements are above the array's average.
    // Demonstrates: two-pass algorithm, for-each loop over arrays.
    // As they say in Lake Wobegon, all students are above average.
    // -----------------------------------------------------------------------

    /**
     * Count how many elements in {@code values} are strictly above the mean.
     *
     * @param values the array to examine
     * @return the number of above-average elements
     */
    public static int aboveAverage(double[] values) {
        if (values.length == 0) { return 0; }

        // First pass: compute the mean.
        double sum = 0;
        for (double v : values) { sum += v; }
        double mean = sum / values.length;

        // Second pass: count elements strictly above the mean.
        int count = 0;
        for (double v : values) {
            if (v > mean) { count++; }
        }
        return count;
    }

    // -----------------------------------------------------------------------
    // Hamming distance between two boolean arrays.
    // Demonstrates: parallel iteration with an index-based loop.
    // -----------------------------------------------------------------------

    /**
     * Count the number of positions where {@code first} and {@code second}
     * differ. Both arrays must have the same length.
     *
     * @param first  the first boolean array
     * @param second the second boolean array
     * @return the Hamming distance between the two arrays
     */
    public static int hammingDistance(boolean[] first, boolean[] second) {
        int differences = 0;
        for (int i = 0; i < first.length; i++) {
            if (first[i] != second[i]) { differences++; }
        }
        return differences;
    }

    // -----------------------------------------------------------------------
    // Population variance — an important statistical quantity that is
    // surprisingly easy to compute with a single pass.
    // Demonstrates: accumulating two running sums simultaneously.
    // -----------------------------------------------------------------------

    /**
     * Compute the population variance of the values in the array using the
     * identity Var(X) = E[X²] − (E[X])². The square root of variance
     * gives the standard deviation.
     *
     * @param values the data array
     * @return the population variance
     */
    public static double variance(double[] values) {
        if (values.length == 0) { return 0; }
        double sum = 0;
        double squareSum = 0;
        for (double v : values) {
            sum += v;
            squareSum += v * v;
        }
        double mean = sum / values.length;
        double meanOfSquares = squareSum / values.length;
        // Var(X) = E[X²] − (E[X])²
        return meanOfSquares - mean * mean;
    }

    // -----------------------------------------------------------------------
    // Polynomial evaluation — naive approach vs. Horner's rule.
    // Demonstrates: accumulator patterns, algorithmic efficiency.
    //
    // Both methods treat coeff[i] as the coefficient of x^i, so
    // coeff = {3, 2, 5} represents the polynomial 3 + 2x + 5x².
    // -----------------------------------------------------------------------

    /**
     * Evaluate a polynomial at {@code x} by computing each term from its
     * coefficient and a running power of x. Costs two multiplications and
     * one addition per coefficient.
     *
     * @param coefficients the polynomial coefficients (index = degree)
     * @param x            the point at which to evaluate
     * @return the value of the polynomial at {@code x}
     */
    public static double evaluatePolynomial(double[] coefficients, double x) {
        double result = 0;
        double powerOfX = 1; // x^0 = 1
        for (double coefficient : coefficients) {
            result += coefficient * powerOfX;
            powerOfX *= x;
        }
        return result;
    }

    /**
     * Evaluate a polynomial at {@code x} using Horner's rule, which
     * processes coefficients from highest degree down. This saves one
     * multiplication per round and is also more numerically stable
     * with floating-point arithmetic.
     *
     * @param coefficients the polynomial coefficients (index = degree)
     * @param x            the point at which to evaluate
     * @return the value of the polynomial at {@code x}
     */
    public static double evaluateHorner(double[] coefficients, double x) {
        double result = coefficients[coefficients.length - 1];
        for (int i = coefficients.length - 2; i >= 0; i--) {
            result = result * x + coefficients[i];
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Random sampling without replacement — rejection approach.
    // Demonstrates: do-while loop, boolean tracking array.
    //
    // Simple but can be slow when k is close to a.length, because
    // nearly every random index will already be taken.
    // -----------------------------------------------------------------------

    /**
     * Draw {@code sampleSize} elements from {@code population} without
     * replacement, using rejection sampling.
     *
     * @param population the array to sample from
     * @param sampleSize how many elements to draw
     * @param rng        the random number generator to use
     * @return an array of {@code sampleSize} sampled elements
     */
    public static double[] rejectionSample(double[] population, int sampleSize, Random rng) {
        var alreadyTaken = new boolean[population.length];
        var sample = new double[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            int index;
            do {
                index = rng.nextInt(population.length);
            } while (alreadyTaken[index]);
            alreadyTaken[index] = true;
            sample[i] = population[index];
        }
        return sample;
    }

    // -----------------------------------------------------------------------
    // Reservoir sampling — an elegant online algorithm.
    // Demonstrates: online/streaming algorithms, probabilistic reasoning.
    //
    // Each element of the input has exactly k/n probability of appearing
    // in the final sample, even though we never know n in advance. This
    // is one of those algorithms that seems almost too clever to be correct.
    // -----------------------------------------------------------------------

    /**
     * Draw {@code sampleSize} elements from {@code population} without
     * replacement using reservoir sampling. Unlike rejection sampling, this
     * works in a single pass and is efficient regardless of the ratio of
     * sample size to population size. Assumes {@code sampleSize <= population.length}.
     *
     * @param population the array to sample from
     * @param sampleSize how many elements to draw
     * @param rng        the random number generator to use
     * @return an array of {@code sampleSize} sampled elements
     */
    public static double[] reservoirSample(double[] population, int sampleSize, Random rng) {
        var reservoir = new double[sampleSize];
        // Fill the reservoir with the first sampleSize elements.
        System.arraycopy(population, 0, reservoir, 0, sampleSize);
        // Each subsequent element may replace a random reservoir entry.
        for (int i = sampleSize; i < population.length; i++) {
            int slot = rng.nextInt(i + 1);
            if (slot < sampleSize) {
                reservoir[slot] = population[i];
            }
        }
        return reservoir;
    }

    // -----------------------------------------------------------------------
    // The coupon collector problem — a classic probability puzzle.
    // Demonstrates: nested simulation loops, Monte Carlo estimation.
    //
    // If there are n distinct coupon types and you get one uniformly at
    // random each day, on average how many days until you have them all?
    // The theoretical answer is n * H(n) where H(n) is the n'th harmonic
    // number, approximately n * ln(n) + 0.5772*n.
    // -----------------------------------------------------------------------

    /**
     * Estimate the expected number of draws needed to collect all {@code n}
     * distinct coupons, by averaging over many independent trials.
     *
     * @param couponCount the number of distinct coupon types
     * @param trials      the number of simulation trials
     * @param rng         the random number generator to use
     * @return the estimated average number of draws to complete the collection
     */
    public static double couponCollector(int couponCount, int trials, Random rng) {
        long totalDraws = 0;
        for (int trial = 0; trial < trials; trial++) {
            var collected = new boolean[couponCount];
            int remaining = couponCount;
            int draws = 0;
            while (remaining > 0) {
                int coupon = rng.nextInt(couponCount);
                draws++;
                if (!collected[coupon]) {
                    collected[coupon] = true;
                    remaining--;
                }
            }
            totalDraws += draws;
        }
        return (double) totalDraws / trials;
    }

    // -----------------------------------------------------------------------
    // Theoretical expected value for the coupon collector problem, for
    // comparison against our simulation results.
    // -----------------------------------------------------------------------

    private static double couponCollectorExpected(int n) {
        // n * H(n) where H(n) = 1 + 1/2 + 1/3 + ... + 1/n
        double harmonicSum = IntStream.rangeClosed(1, n)
                .mapToDouble(i -> 1.0 / i)
                .sum();
        return n * harmonicSum;
    }

    // -----------------------------------------------------------------------
    // Main method — exercise each example and display results.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        var rng = new Random(42);

        // --- Above average ---
        System.out.println("--- aboveAverage ---");
        double[] data = {10, 20, 30, 40, 50};
        System.out.printf("Data: %s%n", Arrays.toString(data));
        System.out.printf("Above average: %d out of %d%n%n",
                aboveAverage(data), data.length);

        // --- Hamming distance ---
        System.out.println("--- hammingDistance ---");
        boolean[] bits1 = {true, false, true, true, false};
        boolean[] bits2 = {true, true, false, true, false};
        System.out.printf("bits1: %s%n", Arrays.toString(bits1));
        System.out.printf("bits2: %s%n", Arrays.toString(bits2));
        System.out.printf("Hamming distance: %d%n%n", hammingDistance(bits1, bits2));

        // --- Variance ---
        System.out.println("--- variance ---");
        double[] uniform = {1, 2, 3, 4, 5};
        System.out.printf("Variance of %s = %.4f%n%n",
                Arrays.toString(uniform), variance(uniform));

        // --- Polynomial evaluation ---
        System.out.println("--- Polynomial evaluation ---");
        // Coefficients {3, 2, 5} represent 3 + 2x + 5x².
        double[] poly = {3, 2, 5};
        double x = 4.0;
        System.out.printf("Polynomial: 3 + 2x + 5x²%n");
        System.out.printf("Naive  eval at x=%.1f: %.4f%n", x, evaluatePolynomial(poly, x));
        System.out.printf("Horner eval at x=%.1f: %.4f%n%n", x, evaluateHorner(poly, x));

        // --- Sampling ---
        System.out.println("--- Rejection sampling vs. reservoir sampling ---");
        double[] population = DoubleStream.iterate(1, v -> v + 1)
                .limit(20)
                .toArray();
        System.out.printf("Population: %s%n", Arrays.toString(population));
        System.out.printf("Rejection sample (5): %s%n",
                Arrays.toString(rejectionSample(population, 5, rng)));
        System.out.printf("Reservoir sample (5): %s%n%n",
                Arrays.toString(reservoirSample(population, 5, rng)));

        // --- Coupon collector ---
        System.out.println("--- Coupon collector problem ---");
        for (int n : new int[]{5, 10, 20, 50}) {
            double simulated = couponCollector(n, 100_000, rng);
            double expected = couponCollectorExpected(n);
            System.out.printf("n=%2d: simulated=%.2f, theoretical=%.2f%n",
                    n, simulated, expected);
        }
    }
}