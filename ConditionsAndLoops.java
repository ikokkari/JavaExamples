import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A collection of example methods demonstrating conditions and loops in Java.
 * Updated for Java 21+ with modern idioms, switch expressions, and better naming.
 *
 * @author Ilkka Kokkarinen
 */

public class ConditionsAndLoops {

    // A seeded random number generator for repeatable results.
    private static final Random rng = new Random(12345);

    // -----------------------------------------------------------------------
    // Sign of an integer.
    // Demonstrates: if-else chain returning one of three possibilities.
    // -----------------------------------------------------------------------

    /**
     * Return the sign of the integer argument as a {@code char}:
     * {@code '-'}, {@code '0'}, or {@code '+'}.
     *
     * @param value the integer whose sign to determine
     * @return the sign character
     */
    public static char sign(int value) {
        if (value < 0) { return '-'; }
        if (value > 0) { return '+'; }
        return '0';
    }

    // -----------------------------------------------------------------------
    // Maximum of three values.
    // Demonstrates: sequential comparisons with a running maximum.
    // -----------------------------------------------------------------------

    /**
     * Return the largest of three integers.
     *
     * @param a the first number
     * @param b the second number
     * @param c the third number
     * @return the largest of the three
     */
    public static int maximum(int a, int b, int c) {
        int max = a;
        if (b > max) { max = b; }
        if (c > max) { max = c; }
        return max;
    }

    // -----------------------------------------------------------------------
    // Median of three values.
    // Demonstrates: nested conditions, short-circuit reasoning.
    // -----------------------------------------------------------------------

    /**
     * Return the median (middle value) of three integers.
     *
     * @param a the first number
     * @param b the second number
     * @param c the third number
     * @return the median of the three
     */
    public static int median(int a, int b, int c) {
        if (a > b && a > c) {
            // a is the largest → median is the larger of b and c.
            return Math.max(b, c);
        }
        if (a < b && a < c) {
            // a is the smallest → median is the smaller of b and c.
            return Math.min(b, c);
        }
        // a is neither largest nor smallest → a is the median.
        return a;
    }

    // -----------------------------------------------------------------------
    // Days in a month — three versions showing language evolution.
    // Demonstrates: if-else ladder, old switch statement, modern switch expression.
    // -----------------------------------------------------------------------

    /**
     * Return the number of days in the given month using an if-else ladder.
     *
     * @param month    the month (1 = January, 12 = December)
     * @param leapYear whether the current year is a leap year
     * @return the number of days, or 0 for an invalid month
     */
    public static int daysInMonthIfElse(int month, boolean leapYear) {
        if (month < 1 || month > 12) { return 0; }
        if (month == 2) { return leapYear ? 29 : 28; }
        if (month == 4 || month == 6 || month == 9 || month == 11) { return 30; }
        return 31; // The last step of a ladder is typically unconditional.
    }

    /**
     * Same logic using the classic switch statement (pre-Java 14).
     * Note the required {@code break} after each case — forgetting one
     * causes the dreaded "fall-through" bug. This is one of the most
     * infamous design mistakes inherited from C.
     *
     * @param month    the month (1 = January, 12 = December)
     * @param leapYear whether the current year is a leap year
     * @return the number of days, or 0 for an invalid month
     */
    public static int daysInMonthOldSwitch(int month, boolean leapYear) {
        int days;
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                days = 31; break;
            case 2:
                days = leapYear ? 29 : 28; break;
            case 4: case 6: case 9: case 11:
                days = 30; break;
            default:
                days = 0;
        }
        return days;
    }

    /**
     * Same logic using a modern switch expression (Java 14+). The arrow
     * syntax eliminates the need for break statements, and the compiler
     * ensures exhaustive coverage. This is the preferred style in modern Java.
     *
     * @param month    the month (1 = January, 12 = December)
     * @param leapYear whether the current year is a leap year
     * @return the number of days
     * @throws IllegalArgumentException if month is not in 1..12
     */
    public static int daysInMonth(int month, boolean leapYear) {
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 2 -> leapYear ? 29 : 28;
            case 4, 6, 9, 11 -> 30;
            default -> throw new IllegalArgumentException(
                    "Invalid month: " + month);
        };
    }

    // -----------------------------------------------------------------------
    // Leap year determination — three equivalent implementations.
    // Demonstrates: early return, boolean expressions, verification testing.
    // -----------------------------------------------------------------------

    /**
     * Determine whether the given year is a leap year, using early returns
     * that progressively narrow the possibilities.
     *
     * @param year the year to examine
     * @return whether that year is a leap year
     */
    public static boolean isLeapYear(int year) {
        if (year % 4 != 0) { return false; }
        // Now we know the year is divisible by 4.
        if (year % 100 != 0) { return true; }
        // Now we know it is divisible by 100; the decision depends
        // on whether it is also divisible by 400.
        return year % 400 == 0;
    }

    /**
     * Same logic, but checking divisibility from the largest factor down.
     */
    public static boolean isLeapYearTopDown(int year) {
        if (year % 400 == 0) { return true; }
        if (year % 100 == 0) { return false; }
        return year % 4 == 0;
    }

    /**
     * Same logic as a single boolean expression. Compact, but harder to read.
     */
    public static boolean isLeapYearOneLiner(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    /**
     * Verify that all three leap year implementations agree for every year
     * in the range 2525..9595 — the extremes of the famous song "In The
     * Year 2525" by Zager and Evans.
     *
     * @return {@code true} if all three methods agree on every year
     */
    public static boolean testLeapYearMethods() {
        return IntStream.rangeClosed(2525, 9595).allMatch(year -> {
            boolean r1 = isLeapYear(year);
            boolean r2 = isLeapYearTopDown(year);
            boolean r3 = isLeapYearOneLiner(year);
            return r1 == r2 && r2 == r3;
        });
    }

    // -----------------------------------------------------------------------
    // Euclid's GCD algorithm — the oldest surviving algorithm in history.
    // Demonstrates: while loop, integer remainder, temporary variables.
    // -----------------------------------------------------------------------

    /**
     * Compute the greatest common divisor of two positive integers using
     * Euclid's algorithm. Works whether {@code a > b} or not.
     *
     * @param a       the first integer
     * @param b       the second integer
     * @param verbose whether to print each step
     * @return the greatest common divisor of {@code a} and {@code b}
     */
    public static int gcd(int a, int b, boolean verbose) {
        while (b > 0) {
            if (verbose) {
                System.out.printf("  a = %d, b = %d%n", a, b);
            }
            int remainder = a % b;
            a = b;
            b = remainder;
        }
        if (verbose) {
            System.out.printf("  a = %d, b = %d → returning %d%n", a, b, a);
        }
        return a;
    }

    /** Non-verbose overload. */
    public static int gcd(int a, int b) {
        return gcd(a, b, false);
    }

    // -----------------------------------------------------------------------
    // Collatz conjecture (3n+1 problem).
    // Demonstrates: ternary operator, while loop, conditional output.
    // See https://en.wikipedia.org/wiki/Collatz_conjecture
    // -----------------------------------------------------------------------

    /**
     * Compute the length of the Collatz sequence starting from the given
     * number. At each step, even numbers are halved and odd numbers become
     * {@code 3n + 1}. The conjecture (still unproven!) states that this
     * always eventually reaches 1.
     *
     * @param start   the starting number
     * @param verbose whether to print the sequence
     * @return the number of steps to reach 1
     */
    public static int collatz(int start, boolean verbose) {
        int current = start;
        int steps = 0;
        while (current > 1) {
            if (verbose) {
                System.out.print((steps > 0 ? ", " : "") + current);
            }
            steps++;
            current = (current % 2 == 0) ? current / 2 : 3 * current + 1;
        }
        if (verbose) {
            System.out.println((steps > 0 ? ", " : "") + current);
        }
        return steps;
    }

    // -----------------------------------------------------------------------
    // Count Unicode letters — just how many are there?
    // Demonstrates: Character utility methods, large-range iteration.
    // -----------------------------------------------------------------------

    /**
     * Count how many Unicode code points are classified as letters. Java
     * supports the full Unicode range, so identifiers can contain characters
     * from scripts all around the world.
     *
     * @return the total number of letter code points
     */
    public static int countUnicodeLetters() {
        // IntStream makes this a clean one-liner, though slower than a raw loop.
        return (int) IntStream.range(0, Character.MAX_CODE_POINT)
                .filter(Character::isLetter)
                .count();
    }

    // -----------------------------------------------------------------------
    // FizzBuzz — the internet-famous job interview quick rejection puzzle.
    // See https://blog.codinghorror.com/why-cant-programmers-program/
    // Demonstrates: StringBuilder, modulo operator, conditional logic.
    // -----------------------------------------------------------------------

    /**
     * Generate the FizzBuzz sequence for the range {@code [start, end]}.
     * Numbers divisible by 3 become "fizz", by 5 become "buzz", and by
     * both become "fizzbuzz".
     *
     * @param start the first number (inclusive)
     * @param end   the last number (inclusive)
     * @return the comma-separated FizzBuzz string
     */
    public static String fizzBuzz(int start, int end) {
        return IntStream.rangeClosed(start, end)
                .mapToObj(i -> {
                    if (i % 15 == 0) { return "fizzbuzz"; }
                    if (i % 3 == 0) { return "fizz"; }
                    if (i % 5 == 0) { return "buzz"; }
                    return String.valueOf(i);
                })
                .collect(Collectors.joining(", "));
    }

    // -----------------------------------------------------------------------
    // Dice rolling utilities.
    // Demonstrates: do-while loop, Random, simulation.
    // -----------------------------------------------------------------------

    /**
     * Roll two dice until both show one ("snake eyes"). Return the number
     * of rolls required. This naturally calls for a do-while loop, since
     * we cannot check the condition until we have rolled at least once.
     *
     * @return the number of rolls needed to get snake eyes
     */
    public static int rollUntilSnakeEyes() {
        int rolls = 0;
        int die1, die2;
        do {
            die1 = rng.nextInt(6) + 1;
            die2 = rng.nextInt(6) + 1;
            rolls++;
        } while (die1 > 1 || die2 > 1);
        return rolls;
    }

    /**
     * Roll an {@code n}-sided die a given number of times and return the sum.
     *
     * @param rolls the number of rolls
     * @param sides the number of sides on the die
     * @return the total of all rolls
     */
    public static int rollDice(int rolls, int sides) {
        int total = 0;
        for (int i = 0; i < rolls; i++) {
            total += rng.nextInt(sides) + 1;
        }
        return total;
    }

    // -----------------------------------------------------------------------
    // Demonstrate pseudorandom number determinism.
    // Demonstrates: seeding a Random, reproducibility.
    // -----------------------------------------------------------------------

    /**
     * Show that "random" numbers are fully deterministic once the seed
     * is fixed — as predictable as a train following its tracks.
     *
     * @param seed the seed value
     * @param count how many random numbers to generate
     */
    public static void outputRandomSeries(int seed, int count) {
        rng.setSeed(seed);
        for (int i = 0; i < count; i++) {
            System.out.print(rng.nextInt() + " ");
        }
        System.out.println();
    }

    // Note: class Random is adequate for toy games and simple demos. For
    // serious simulations, use java.security.SecureRandom instead.

    // -----------------------------------------------------------------------
    // Nested loop classics: rectangle and triangle of characters.
    // In the 1980s, it was essentially a law that every programming
    // textbook had to include these two exercises about nested loops.
    // -----------------------------------------------------------------------

    /**
     * Print a rectangle of the given character.
     *
     * @param rows the number of rows
     * @param cols the number of columns
     * @param ch   the fill character
     */
    public static void printRectangle(int rows, int cols, char ch) {
        for (int row = 0; row < rows; row++) {
            System.out.println(String.valueOf(ch).repeat(cols));
        }
    }

    /**
     * Print a right triangle of the given character, with row {@code i}
     * containing {@code i + 1} characters.
     *
     * @param rows the number of rows
     * @param ch   the fill character
     */
    public static void printTriangle(int rows, char ch) {
        for (int row = 0; row < rows; row++) {
            System.out.println(String.valueOf(ch).repeat(row + 1));
        }
    }

    // After mastering these two, try outputting a diamond shape, e.g. for rows = 7:
    //    $
    //   $$$
    //  $$$$$
    // $$$$$$$
    //  $$$$$
    //   $$$
    //    $

    // -----------------------------------------------------------------------
    // Primality testing and prime factorization.
    // Demonstrates: trial division, early exit, StringBuilder.
    // -----------------------------------------------------------------------

    /**
     * Test whether {@code n} is a prime number using trial division up to
     * √n. Only odd candidate divisors are tested after checking for 2.
     *
     * @param n the integer to test
     * @return {@code true} if {@code n} is prime
     */
    public static boolean isPrime(int n) {
        if (n < 2) { return false; }
        if (n == 2) { return true; }
        if (n % 2 == 0) { return false; }
        // Test odd divisors from 3 up to √n.
        for (int divisor = 3; divisor * divisor <= n; divisor += 2) {
            if (n % divisor == 0) { return false; }
        }
        return true;
    }

    /**
     * List the prime factors of a positive integer as a bracketed,
     * comma-separated string. Factors appear with multiplicity:
     * for example, {@code primeFactors(12)} returns {@code "[2, 2, 3]"}.
     *
     * @param n the positive integer to factorize
     * @return the prime factorization as a string
     */
    public static String primeFactors(int n) {
        var result = new StringBuilder("[");
        boolean first = true;
        int divisor = 2;

        while (n > 1) {
            if (n % divisor == 0) {
                n /= divisor;
                if (!first) { result.append(", "); }
                result.append(divisor);
                first = false;
            } else {
                // Advance to the next candidate: 2 → 3, then only odd numbers.
                divisor = (divisor == 2) ? 3 : divisor + 2;
            }
        }
        result.append("]");
        return result.toString();
    }

    // -----------------------------------------------------------------------
    // Main method — exercise each example and display results.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        // --- Sign ---
        System.out.println("--- sign ---");
        for (int value : new int[]{-42, 0, 99}) {
            System.out.printf("sign(%d) = '%c'%n", value, sign(value));
        }

        // --- Maximum and median ---
        System.out.println("\n--- maximum and median ---");
        System.out.printf("maximum(3, 7, 5) = %d%n", maximum(3, 7, 5));
        System.out.printf("median(3, 7, 5) = %d%n", median(3, 7, 5));

        // --- Days in month (all three versions) ---
        System.out.println("\n--- daysInMonth (three versions) ---");
        for (int month : new int[]{1, 2, 4, 7}) {
            System.out.printf("Month %2d: if-else=%d, old switch=%d, new switch=%d%n",
                    month,
                    daysInMonthIfElse(month, false),
                    daysInMonthOldSwitch(month, false),
                    daysInMonth(month, false));
        }
        System.out.printf("Feb in leap year: %d%n", daysInMonth(2, true));

        // --- Leap year ---
        System.out.println("\n--- isLeapYear ---");
        for (int year : new int[]{1997, 2000, 2012, 2020, 2100}) {
            System.out.printf("Is %d a leap year? %s%n", year, isLeapYear(year));
        }
        System.out.println("All three methods agree on 2525..9595: "
                + testLeapYearMethods());

        // --- GCD and LCM ---
        System.out.println("\n--- gcd ---");
        int a = 2 * 3 * 5 * 11 * 13;
        int b = 2 * 2 * 3 * 7 * 13 * 23;
        System.out.printf("Computing gcd(%d, %d):%n", a, b);
        int divisor = gcd(a, b, true);
        // Group operations to avoid overflow: compute a/gcd first, then multiply by b.
        long lcm = (long) a / divisor * b;
        System.out.printf("lcm(%d, %d) = %d%n", a, b, lcm);

        // --- Collatz sequences ---
        System.out.println("\n--- Collatz sequences ---");
        for (int start : new int[]{10, 100, 1_000, 12_345, 987_654}) {
            System.out.printf("Collatz(%d): ", start);
            int steps = collatz(start, true);
            System.out.printf("  → reached 1 in %d steps%n", steps);
        }

        // --- Unicode letter count ---
        System.out.println("\n--- countUnicodeLetters ---");
        System.out.println("Counting... (this may take a moment)");
        int letterCount = countUnicodeLetters();
        System.out.printf("Found %,d Unicode code points classified as letters.%n",
                letterCount);

        // --- FizzBuzz ---
        System.out.println("\n--- fizzBuzz(1, 100) ---");
        System.out.println(fizzBuzz(1, 100));

        // --- Snake eyes simulation ---
        System.out.println("\n--- rollUntilSnakeEyes ---");
        int totalRolls = 0;
        int trials = 100_000;
        for (int i = 0; i < trials; i++) {
            totalRolls += rollUntilSnakeEyes();
        }
        System.out.printf("Average rolls to snake eyes over %,d trials: %.2f%n",
                trials, totalRolls / (double) trials);
        System.out.printf("Theoretical expected value: %.2f (= 36)%n", 36.0);

        // --- Primality ---
        System.out.println("\n--- isPrime ---");
        long primeCount = IntStream.rangeClosed(0, 1_000_000)
                .filter(ConditionsAndLoops::isPrime)
                .count();
        System.out.printf("There are exactly %,d primes up to one million.%n", primeCount);

        // --- Prime factorization ---
        System.out.println("\n--- primeFactors ---");
        for (int value : new int[]{a, b, 360, 997}) {
            System.out.printf("primeFactors(%d) = %s%n", value, primeFactors(value));
        }

        // --- Rectangle and triangle ---
        System.out.println("\nA 5×8 rectangle of dollar signs:");
        printRectangle(5, 8, '$');

        System.out.println("\nA triangle of 10 rows:");
        printTriangle(10, '$');

        // --- Random determinism demo ---
        System.out.println("\nSame seed → same sequence (twice):");
        outputRandomSeries(42, 8);
        outputRandomSeries(42, 8);
    }
}