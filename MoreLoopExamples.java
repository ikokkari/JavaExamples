import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Additional examples of conditions and loops for the course CCPS 109.
 * Updated for Java 21+ with modern idioms and language features.
 *
 * @author Ilkka Kokkarinen
 */

public class MoreLoopExamples {

    // -----------------------------------------------------------------------
    // Serious simulations require a serious random number generator with
    // sufficient entropy to produce reliable results. SecureRandom is
    // cryptographically strong, unlike the default Random.
    // -----------------------------------------------------------------------

    private static final String SEED =
            "Serious long simulations require a serious random number generator "
                    + "that has sufficient entropy to generate reliable results.";
    private static final Random rng = new SecureRandom(SEED.getBytes());

    // -----------------------------------------------------------------------
    // Count the digits in an integer.
    // Demonstrates: while loop, integer division to peel off digits.
    // -----------------------------------------------------------------------

    /**
     * Count how many decimal digits the representation of {@code n} contains.
     *
     * @param n the integer whose digits are to be counted
     * @return how many digits there are in {@code n}
     */
    public static int countDigits(int n) {
        // Math.abs would fail silently for Integer.MIN_VALUE, but this
        // simple flip is fine for a teaching example.
        if (n < 0) { n = -n; }
        int count = 1; // Every number has at least one digit.
        while (n > 9) {
            n /= 10;   // Peel off the last digit.
            count++;
        }
        return count;
    }

    // -----------------------------------------------------------------------
    // Duplicate the digits of an integer: 1234 -> 12341234.
    // Demonstrates: preserving the original value, integer power computation.
    // -----------------------------------------------------------------------

    /**
     * Compute the number formed by writing the digits of {@code n} twice.
     * For example, {@code duplicateDigits(1234)} returns {@code 12341234L}.
     *
     * @param n the integer whose digits are to be duplicated
     * @return the result of concatenating the digits of {@code n} with themselves
     */
    public static long duplicateDigits(long n) {
        if (n < 0) { return -duplicateDigits(-n); }

        // Compute the power of ten that has the same number of digits as n.
        // We use integer arithmetic instead of Math.pow to guarantee precision.
        long powerOfTen = 1;
        long remaining = n; // Preserve n — we need its original value below.
        while (remaining > 0) {
            powerOfTen *= 10;
            remaining /= 10;
        }
        // If n = 1234 and powerOfTen = 10000, then (10000 + 1) * 1234 = 12341234.
        return (powerOfTen + 1) * n;
    }

    // -----------------------------------------------------------------------
    // Monte Carlo: how many uniform [0, 1) values until their sum exceeds 1?
    // The theoretical expected value is e ≈ 2.71828... (a beautiful result).
    // Demonstrates: do-while loop, numerical simulation, averaging trials.
    // -----------------------------------------------------------------------

    /**
     * A single trial: generate random numbers in [0, 1) until their sum
     * exceeds one, and return how many were needed.
     *
     * @return the count of random numbers needed to exceed one in this trial
     */
    public static int rollUntilSumExceedsOne() {
        double runningTotal = 0;
        int rolls = 0;
        do {
            runningTotal += rng.nextDouble();
            rolls++;
        } while (runningTotal < 1);
        return rolls;
    }

    /**
     * Estimate the expected number of uniform [0, 1) samples needed for
     * their sum to exceed one, by averaging many independent trials.
     *
     * @param trials the number of trials to perform
     * @return the average result across all trials
     */
    public static double averageRollsUntilOne(int trials) {
        // IntStream.range replaces the manual counting loop. The .parallel()
        // call is safe here because each trial uses its own local state;
        // only the SecureRandom is shared, and it is thread-safe.
        long totalRolls = IntStream.range(0, trials)
                .mapToLong(i -> rollUntilSumExceedsOne())
                .sum();
        return (double) totalRolls / trials;
    }

    // -----------------------------------------------------------------------
    // Heron's algorithm for numerical square root approximation.
    // Demonstrates: convergence loop, method overloading for default args.
    // -----------------------------------------------------------------------

    /**
     * Calculate the square root of {@code x} with Heron's method, iterating
     * an initial guess until the {@code double} value no longer changes.
     * <p>
     * Another blast from the ancient age of swords and sandals when, as the
     * old song says, "the ships were made of wood and men were made of iron."
     * <p>
     * The idea generalizes to arbitrary roots. Heron's algorithm is a special
     * case of the much later and more famous Newton's method for finding roots
     * of continuous differentiable functions.
     *
     * @param x       the number whose square root to compute
     * @param verbose whether to print each iteration's progress
     * @return the square root of {@code x}
     */
    public static double heronSquareRoot(double x, boolean verbose) {
        double guess = x / 2; // We have to start from somewhere.
        double previousGuess;
        do {
            if (verbose) { System.out.println("Current guess is " + guess); }
            previousGuess = guess;
            // New guess: average of the current guess and x / guess.
            guess = (guess + x / guess) / 2;
        } while (guess != previousGuess);

        if (verbose) { System.out.println("Returning result " + guess); }
        return guess;
    }

    /**
     * Non-verbose overload — simulates a default parameter value.
     * Java does not support default or keyword arguments; method overloading
     * is the idiomatic alternative.
     */
    public static double heronSquareRoot(double x) {
        return heronSquareRoot(x, false);
    }

    // -----------------------------------------------------------------------
    // Monte Carlo estimation of the unit circle's area (i.e., estimating π).
    // Demonstrates: random sampling, ratio estimation, convergence analysis.
    // -----------------------------------------------------------------------

    /**
     * Estimate the area of the unit circle by generating random points in the
     * enclosing 2×2 square and counting how many fall inside the circle.
     *
     * @param sampleSize number of random points to generate
     * @return the estimated area (should converge toward π)
     */
    public static double estimateUnitCircleArea(int sampleSize) {
        int insideCount = 0;
        for (int i = 0; i < sampleSize; i++) {
            // Random point in the origin-centered 2×2 square.
            double x = rng.nextDouble() * 2 - 1;
            double y = rng.nextDouble() * 2 - 1;
            if (x * x + y * y <= 1) { insideCount++; }
        }
        // The circle fills π/4 of the square's area, so multiply by 4.
        return 4.0 * insideCount / sampleSize;
    }

    /**
     * Show how the Monte Carlo estimation error shrinks as the sample size
     * grows by powers of ten.
     */
    public static void checkAreaConvergence() {
        int sampleSize = 10;
        for (int power = 1; power <= 7; power++) {
            double area = estimateUnitCircleArea(sampleSize);
            double error = Math.abs(Math.PI - area);
            System.out.printf("Using n = %,10d, estimate = %.7f, error = %.7f%n",
                    sampleSize, area, error);
            sampleSize *= 10;
        }
    }

    // -----------------------------------------------------------------------
    // Main method — exercise each example and display results.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        // --- Digit counting ---
        System.out.println("--- countDigits ---");
        for (int testValue : new int[]{0, 7, -42, 1234, 1_000_000}) {
            System.out.printf("countDigits(%d) = %d%n", testValue, countDigits(testValue));
        }

        // --- Digit duplication ---
        System.out.println("\n--- duplicateDigits ---");
        for (long testValue : new long[]{5, 42, 1234, 9999}) {
            System.out.printf("duplicateDigits(%d) = %d%n", testValue, duplicateDigits(testValue));
        }

        // --- Random sum simulation ---
        System.out.println("\n--- How many uniform [0,1) values to exceed 1? ---");
        System.out.println("Theoretical expected value: e ≈ " + Math.E);
        for (int trials : new int[]{1_000, 10_000, 100_000, 1_000_000}) {
            System.out.printf("Average over %,d trials: %.5f%n",
                    trials, averageRollsUntilOne(trials));
        }

        // --- Heron's square root ---
        System.out.println("\n--- heronSquareRoot (verbose for √2) ---");
        heronSquareRoot(2.0, true);
        System.out.printf("%nheronSquareRoot(144) = %.1f%n", heronSquareRoot(144));

        // --- Monte Carlo π estimation ---
        System.out.println("\n--- Monte Carlo estimation of π ---");
        checkAreaConvergence();
    }
}