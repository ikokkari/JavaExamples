import java.security.SecureRandom;
import java.util.Random;

/**
 * Additional examples of conditions and loops for the course CCPS 109.
 * @author Ilkka Kokkarinen
 */

public class MoreLoopExamples {
    
    /**
     * Given an integer {@code n}, count how many digits its representation contains.
     * @param n The integer whose digits are to be counted.
     * @return How many digits there are in the parameter {@code n}.
     */
    public static int countDigits(int n) {
        if(n < 0) { n = -n; } // flip a negative number to positive
        int count = 1;
        while(n > 9) {
            n = n / 10;
            count++;
        }
        return count;
    }
    
    /**
     * Given an integer {@code n}, compute the number given by writing the
     * digits of {@code n} twice. For example, for argument 1234, the method
     * would return 12341234.
     * @param n The integer whose digits are to be duplicated.
     * @return The result of duplicating the digits.
     */
    public static long duplicateDigits(long n) {
        if(n < 0) { return -duplicateDigits(-n); }
        int d = 1;
        long n2 = n; // We should not modify n, so we modify n2 instead.
        // Compute the power of 10 that has same number of digits as n.
        // Use integer arithmetic instead of Math.pow to guarantee precision.
        while(n2 > 0) {
            d = d * 10; n2 = n2 / 10;
        }
        return (d+1) * n; // We need the original value of n in this formula.
    }
    
    // Another classic randomness problem. Let us solve it by numerical simulation.
    // On average, how many random numbers from the uniform interval [0, 1) do you
    // need to generate for their sum to exceed one?
    
    /**
     * A single trial of generating random numbers until their sum exceeds one.
     * @return The count of random numbers needed to reach one in this trial.
     */
    public static int rollUntilAddToOne() {
        double total = 0;
        int rolls = 0;
        do { 
            total += rng.nextDouble(); 
            rolls++; 
        } while(total < 1);
        return rolls;
    }
    
    /**
     * Perform given number of {@code trials} of generating random numbers until
     * their sum exceeds one.
     * @param trials The numbers of trials to perform.
     * @return The average result of the random trials.
     */
    public static double howManyUntilOne(int trials) {
        int count = 0;
        for(int i = 0; i < trials; i++) {
            count += rollUntilAddToOne();
        }
        return (double)count / trials;
    }
    
    /**
     * Calculate the square root of {@code x} with Heron's method, iterating an
     * initial guess until the double value no longer changes. Another blast from
     * the ancient age of swords and sandals back when, like it says in the song,
     * "the ships were made of wood and men were made of iron".
     * @param x The number whose square root we want to numerically compute.
     * @param verbose Whether the method should output its progress.
     * @return The square root of {@code x}, within given tolerance of {@code tol}.
     */
    public static double heronRoot(double x, boolean verbose) {
        double guess = x / 2; // we have to start from somewhere
        double prev = 0;
        while(guess != prev) {
            if(verbose) { System.out.println("Current guess is " + guess); }
            // current guess becomes the previous guess
            prev = guess;
            // calculate a new, more accurate guess
            guess = (guess + x / guess) / 2;
        }
        if(verbose) { System.out.println("Returning result " + guess); }
        return guess;
        
        // The idea generalizes to arbitrary roots, not just the square root. 
        // Heron's algorithm is a special case of the much later and more famous
        // "Newton's method" to solve roots of arbitrary continuous functions
        // whose differentials are known. Plugging the square root into Newton's
        // method gives Heron root iteration.
    }
    
    // Monte Carlo estimation for area of some complicated shape: generate
    // random points from some easy area that is known to fully contain the
    // area that we want to measure, and count how many of these points fall
    // under that area. Use this percentage of the larger area as an estimate
    // for the smaller area.
    
    private static final String seed =
    "Serious long simulations require a serious random number generator " +
    "that has sufficient entropy to generate reliable results.";
    private static final Random rng = new SecureRandom(seed.getBytes());
    
    /**
     * Estimate the area of the unit circle with Monte Carlo estimation.
     * @param n Number of random samples to generate for the estimation.
     * @return The estimated area of the unit circle.
     */
    public static double estimateUnitCircleArea(int n) {
        int count = 0;
        for(int i = 0; i < n; i++) {
            // random point in origin-centered 2*2 square
            double x = rng.nextDouble() * 2 - 1;
            double y = rng.nextDouble() * 2 - 1;
            // if inside the circle, increment count
            if(x*x + y*y <= 1) { count++; }
        }
        return 4 * (double) count / n;
    }
    
    /**
     * Measure the error of Monte Carlo estimation for unit circle area,
     * for various values of the sample size {@code n}.
     */
    public static void checkAreaConvergence() {
        int n = 10;
        for(int i = 0; i < 7; i++) {
            double area = estimateUnitCircleArea(n);
            double error = Math.abs(Math.PI - area);
            System.out.printf("Using n = %d, error is %.7f\n", n, error);
            n = 10 * n;
        }
    }
}