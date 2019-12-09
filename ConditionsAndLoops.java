import java.util.Random;
import java.util.Scanner;

/**
 * A bunch of example methods to demonstrate conditions and loops in Java.
 * @author Ilkka Kokkarinen
 */

public class ConditionsAndLoops {

    /**
     * Return the sign (-, 0, +) of the integer argument as a {@code char}.
     * To choose from three possibilities, we need to nest an if-else inside an if-else.
     * @param a An integer whose sign we want to determine.
     * @return The sign of the parameter integer.
     */
    public static char sign(int a) {
        char result;
        if(a < 0) { result = '-'; }
        else {
            if(a > 0) { result = '+'; }
            else { result = '0'; }
        }
        return result;
    }
    
    /**
     * Computes the maximum of its three parameters.
     * @param a The first of the three numbers.
     * @param b The second of the three numbers.
     * @param c The third of the three numbers.
     * @return The largest of the three numbers.
     */
    public static int maximum(int a, int b, int c) {
        // Use a local variable to remember the largest number seen so far.
        int max = a;
        if(b > max) { max = b; }
        if(c > max) { max = c; }
        return max;
    }

    /**
     * Computes the median of its three parameters.
     * @param a The first of the three numbers.
     * @param b The second of the three numbers.
     * @param c The third of the three numbers.
     * @return The median of the three numbers. 
     */
    public static int median(int a, int b, int c) {
        int med;
        if(a > b && a > c) {
            // Here we know a is the largest of the three.
            if(b > c) { med = b; }
            else { med = c; }
        }
        else if(a < b && a < c) {
            // Here we know that a is smallest of the three.
            if(b > c) { med = c; }
            else { med = b; }                
        }
        else {
            // If a is not the largest or the smallest, it is the median.
            med = a;
        }
        return med;  
    }
    
    /**
     * Demonstrate the use of if-else ladders in Java with a method that
     * computes the number of days in the given month.
     * @param m The month number (January = 1, December = 12)
     * @param leapYear Whether the current year is a leap year.
     * @return The number of days in the given month.
     */
    public static int numberOfDays(int m, boolean leapYear) {
        int d;
        if(m < 1 || m > 12) { d = 0; } // nonexistent month
        else if(m == 2) { 
            if(leapYear) { d = 29; } else { d = 28; }
        }
        else if(m == 4 || m == 6 || m == 9 || m == 11) { d = 30; }
        else { d = 31; } // last step of a ladder is typically unconditional
        return d;
    }
    
    /**
     * Another example of if-else ladders in Java, with a method that
     * returns the Ryerson letter grade from the percentage grade, as in
     * {@code http://www.ryerson.ca/currentstudents/essr/gradescales_ugrad/}
     * @param p The percentage grade.
     * @return The letter grade as a string.
     */
    public static String convertRyersonLetterGrade(int p) {
        String result = "";
        // Start by handling F and A levels as special cases
        if(p < 50) { result = "F"; }
        else if(p >= 90) { result = "A+"; }
        else if(p >= 85) { result = "A"; }
        else if(p >= 80) { result = "A-"; }
        else { // B, C, and D levels all have the same structure
            int tens = p / 10; // now known to be 5, 6 or 7 for certain
            result = "DCB".substring(tens - 5, tens - 4); // cute
            int ones = p % 10;
            if(ones < 3) { result += "-"; }
            else if(ones > 6) { result += "+"; }
        }
        return result;
    }
    
    /**
     * Demonstrate the use of switch structure in Java with a method that
     * computes the number of days in the given month.
     * @param m The month number (January = 1, December = 12)
     * @param leapYear Whether the current year is a leap year.
     * @return The number of days in the given month.
     */
    public static int numberOfDaysWithSwitch(int m, boolean leapYear) {
        int d;
        switch(m) {
            // Multiple cases with common body can be combined together.
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
            d = 31; break;
            case 2:
            d = (leapYear? 29 : 28); break; // Ternary selection COND? EXPR1: EXPR2
            case 4: case 6: case 9: case 11:
            d = 30; break;
            default: // Sort of "none of the above"
            d = 0;
        }
        return d;
    }
    
    /**
     * Determine whether the given year is a leap year.
     * @param y The year to examine.
     * @return Whether that year is a leap year.
     */
    public static boolean isLeapYear(int y) {
        if(y % 4 != 0) { return false; }
        // Now we know that the year is divisible by 4.
        if(y % 100 != 0) { return true; }
        // Now we know that it is divisible by 100, so the decision
        // depends on whether it is divisible by 400.
        return y % 400 == 0;
    }
    
    /**
     * Determine whether the given year is a leap year.
     * @param y The year to examine.
     * @return Whether that year is a leap year.
     */
    public static boolean isLeapYearOtherWay(int y) {
        if(y % 400 == 0) { return true; }
        if(y % 100 == 0) { return false; }
        return y % 4 == 0;
    }
    
    /**
     * Determine whether the given year is a leap year.
     * @param y The year to examine.
     * @return Whether that year is a leap year.
     */
    public static boolean isLeapYearOneLiner(int y) {
        return y % 4 == 0 && (y % 100 != 0 || y % 400 == 0);
    }
    
    /**
     * Check that the above three leap year methods always return the same result.
     * @return {@code true} if all three methods agree on the classification for 
     * all years between the extremes of the famous song "In The Year 2525" by
     * Zager & Evans, {@code false} otherwise.
     */
    public static boolean testLeapYearMethods() {
        for(int y = 2525; y <= 9595; y++) {
            boolean b1 = isLeapYear(y);
            boolean b2 = isLeapYearOtherWay(y);
            boolean b3 = isLeapYearOneLiner(y);
            if(b1 != b2 || b2 != b3) { // de Morgan to !(b1 == b2 && b2 == b3)
                return false; // Tear it down and start again.
            }
        }
        return true; // I am pleased where man has been.
    }
    
    /**
     * The oldest surviving algorithm in recorded history, Euclid's
     * algorithm to find the greatest common divisor of two integers.
     * @param a First integer
     * @param b Second integer
     * @return The greatest common divisor of {@code a} and {@code b}.
     */
    public static int gcd(int a, int b) {
        // Works whether a > b or not. Try it out.
        while(b > 0) {
            int tmp = a % b; // % is the integer remainder operator
            a = b;
            b = tmp;
        }
        return a;
    }
    
    /*
     * Compute the length of the famous Collatz 3n+1 series. See e.g. the page
     * {@code https://en.wikipedia.org/wiki/Collatz_conjecture}
     * @param start The number to start the series from.
     * @param verbose Whether the method should print the numbers along the way.
     * @return The number of steps needed to reach the goal 1.
     */
    public static int collatz(int start, boolean verbose) {
        int n = start;
        int count = 0;
        while(n > 1) {
            if(verbose) {
                System.out.print((count > 0 ? ", " : "") + n);
            }
            count++;
            if(n % 2 == 0) { n = n / 2; }
            else { n = 3*n+1; }
        }
        if(verbose) {
            System.out.println((count > 0 ? ", " : "") + n);
        }
        return count;
    }
    
    /**
     * Use the utility methods of Character to find out what characters are allowed
     * to appear as a part of Java identifiers these days.
     * @return The count of all characters that can appear in Java identifier.
     */
    public static int countJavaIdentifierChars() {
        int count = 0;
        for(int c = 0; c < Character.MAX_CODE_POINT; c++) {
            if(Character.isJavaIdentifierPart(c) && !Character.isIdentifierIgnorable(c)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * The Internet-famous Fizzbuzz job interview quick rejection puzzle. See e.g.
     * {@code https://blog.codinghorror.com/why-cant-programmers-program/ }
     * @param start Value to start the series from.
     * @param end Value to end the series from.
     * @return A string containing the fizzbuzz-converted numbers, separated by commas.
     */
    public static String fizzBuzz(int start, int end) {
        StringBuilder result = new StringBuilder();
        for(int i = start; i <= end; i++) {
            if(i % 5 == 0 && i % 3 == 0) { result.append("fizzbuzz"); }
            else if(i % 5 == 0) { result.append("buzz"); }
            else if(i % 3 == 0) { result.append("fizz"); }
            else result.append(i);
            if(i < end) { result.append(", "); }
        }
        return result.toString();
    } 
    
    // A random number generator that we need in the next methods.
    // When no seed is given, system clock is used as the seed value.
    private static Random rng = new Random();
    
    /**
     * The classic number guessing game programming exercise. Keeps asking the
     * human player to guess the secret number until they guess it right.
     * @param min The smallest value for the secret number.
     * @param max The largest value for the secret number.
     * @return The number of guesses that the player needed to guess the number.
     */
    public static int numberGuess(int min, int max) {
        System.out.println("Guess a number between " + min + " and " + max + ".");
        int secret = rng.nextInt(max - min + 1) + min; // from {min, ..., max}
        int guess, total = 0;
        Scanner s = new Scanner(System.in);
        do { // We can use do-while, since the game must take at least one round.
            System.out.print("Enter your guess: ");
            guess = s.nextInt();
            total++;
            if(guess < secret) { System.out.println("Too low. Try again."); }
            if(guess > secret) { System.out.println("Too high. Try again."); }
        } while(guess != secret); // test meaningless until input read once
        System.out.println("You got it in " + total + " tries.");
        return total;
    }
    
    /**
     * The number guessing game programming exercise done in reverse,
     * with the method guessing the secret number thought by the user.
     * @param min The smallest value for the secret number.
     * @param max The largest value for the secret number.
     * @return The number of guesses that the method needed to guess the number.
     */
    public static int numberGuessReverseRoles(int min, int max) {
        System.out.println("I am trying to guess your secret number.");
        int upper = max, lower = min, total = 0, guess = -1;
        Scanner s = new Scanner(System.in);
        outer:
        while(lower < upper) {
            guess = (lower + upper) / 2;
            total++;
            System.out.print("I guess " + guess);
            System.out.println(". Is it (h)igh, (l)ow or (r)ight?");
            while(true) {
                String ans = s.next().toLowerCase(); // read next line of input
                if(ans.startsWith("h")) { upper = guess - 1; break; }
                if(ans.startsWith("l")) { lower = guess + 1; break; }
                if(ans.startsWith("r")) { break outer; }
            }
        }
        if(lower > upper) {
            System.out.println("I am literally shaking, I don't even, Wow just wow!");
            return 0;
        }
        else if(lower == upper) {
            System.out.println("The secret number must be " + lower + " then.");
            return total;
        }
        else {
            System.out.println("I guessed the number in " + total + " tries.");
            return total;
        }    
    }
    
    /**
     * Demonstrate that "random numbers" are not really random, but
     * once the seed has been set, the entire future series is as
     * deterministic as a train destined to follow its tracks.
     * @param seed The seed to use to generate random numbers.
     * @param n How many random numbers to generate.
     */
    public static void outputRandomSeries(int seed, int n) {
        rng.setSeed(seed);
        for(int i = 0; i < n; i++) {
            System.out.print(rng.nextInt() + " ");
        }
        System.out.println("");
    }
    
    // The class Random is good enough for toy games and such, but for
    // serious simulations, import java.security.* and use
    //
    // SecureRandom rng = new SecureRandom();
    // rng.setSeed("This should be some secret passphrase".toBytes());
    
    /**
     * Keep rolling two dice until both show you one ("snake eyes"), and
     * count how many rolls were needed.
     * @return The number of rolls needed to get snake eyes.
     */
    public static int rollUntilSnakeEyes() {
        int count = 0;
        int d1, d2;
        // Of course, you can't check the snake eyes condition until you
        // have rolled the dice once, which is why we need to use a do-while
        // loop, instead of a while loop.
        do {
            d1 = rng.nextInt(6) + 1;
            d2 = rng.nextInt(6) + 1;
            count++;
        } while(d1 > 1 || d2 > 1);
        return count;
    }
    
    /**
     * Roll a {@code d}-sided die given number of times and return the sum.
     * @param d Number of sides in the die.
     * @param n Number of rolls to make.
     * @return The total for the rolls.
     */
    public static int rollDie(int n, int d) {
        int sum = 0;
        for(int i = 0; i < n; i++) { // canonical way to do something n times
            int roll = rng.nextInt(d) + 1;
            sum = sum + roll;
        }
        return sum;
    }   
   
    /**
     * Floating point numbers don't make good loop counters, because of the
     * imprecision of the floating point arithmetic. Many familiar numbers such
     * as 0.1 simply do not have an exact representation in floating point.
     * This method illustrates this by trying to loop from 0 to 1 with an
     * increment of 0.1 each time. Never use a floating point number as a
     * loop counter!
     */
    public static void floatingPointLoop() {
        double x = 0.0;
        while(x <= 1.0) {
            System.out.println(x); // all the decimal places available
            x = x + 0.1;
        }
    }

    // In the eighties it was a law that every programming book had to have the
    // following two exercises about nested loops: First, output a rectangle
    // consisting of rows of given character. After that, output a triangle.
    
    /**
     * Output a rectangle with given number of rows and columns.
     * @param rows The number of rows in the rectangle.
     * @param cols The number of columns in the rectangle.
     * @param ch The character used to fill the rectangle.
     */
    public static void outputRectangle(int rows, int cols, char ch) {
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                System.out.print(ch);
            }
            System.out.println(""); // just line break
        }
    }
    
    /**
     * Output a triangle with given number of rows.
     * @param rows The number of rows in the triangle.
     * @param ch The character used to fill the triangle.
     */
    public static void outputTriangle(int rows, char ch) {
        for(int i = 0; i < rows; i++) {
            // outer loop counter now sets limit to inner loop rounds
            for(int j = 0; j < i + 1; j++) { 
                System.out.print(ch);
            }
            System.out.println("");
        }
    }
    
    // After these two, try to output a diamond shape, e.g. for rows == 7
    //    $
    //   $$$
    //  $$$$$
    // $$$$$$$
    //  $$$$$
    //   $$$
    //    $
    
    /**
     * Checks if the parameter is a prime number, that is, greater than one and
     * divisible only by one and by itself.
     * @param n The integer whose primality we want to determine.
     * @return Whether that integer is a prime number.
     */ 
    public static boolean isPrime(int n) {
        // Start by checking the easy cases.
        if(n < 2) { return false; }
        if(n == 2) { return true; }
        if(n % 2 == 0) { return false; }
        // Otherwise, loop through potential divisors.
        for(int d = 3; d * d <= n; d = d + 2) {
            if(n % d == 0) {
                // We found a divisor, so n is not a prime number, and that's it.
                return false;
            }            
        }
        // If we get this far, no divisors could be found, so n is a prime.
        return true;
    }
    
    /**
     * Construct the list of prime factors of positive integer. The list should
     * be separated by commas so that there is a comma before each number except
     * for the first one.
     * @param n The positive integer whose prime factors we want.
     * @return The prime factors as a comma-separated {@code String}.
     */ 
    public static String listPrimeFactors(int n) {
        StringBuilder result = new StringBuilder();
        result.append("[");
        boolean first = true;
        int d = 2;
        while(n > 1) {
            if(n % d == 0) { // found a divisor
                n = n / d; // divide it away from the number n
                if(!first) { result.append(", "); } // comma, unless first
                result.append(d); // the divisor itself
                first = false;
            }
            else {
                // move on to next potential divisor
                if(d == 2) { d = 3; }
                else { d = d + 2; } // only look at odd potential divisors
            }
        }
        result.append("]");
        return result.toString();
    }
}