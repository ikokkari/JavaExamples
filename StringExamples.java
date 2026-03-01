import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A collection of string manipulation examples demonstrating character-level
 * processing, lookup tables, run-length encoding, and digit-by-digit arithmetic.
 * <p>
 * Updated for Java 21+ with modern idioms, better naming, and cleaner algorithms.
 *
 * @author Ilkka Kokkarinen
 */
public class StringExamples {

    // -----------------------------------------------------------------------
    // Deterministic "random" string generation.
    //
    // A cute example from Stack Overflow: for a given seed, the Random
    // sequence is completely deterministic, so you can find seeds that
    // produce specific words. This prints "hello world".
    //
    // Real programs and games use this idea for "procedural generation":
    // a level ID seeds the RNG used by the level generator, so the level
    // is always the same for everyone who plays it.
    // -----------------------------------------------------------------------

    /**
     * Generate a deterministic string from the given RNG seed. The generator
     * picks random letters until it hits a "stop" signal (value 0 mod 27).
     *
     * @param seed the seed for the random number generator
     * @return the generated string
     */
    public static String randomString(int seed) {
        var rng = new Random(seed);
        var result = new StringBuilder();
        int codePoint;
        int base = 'a' - 1;
        do {
            codePoint = base + rng.nextInt(27);
            if (codePoint > base) { result.append((char) codePoint); }
        } while (codePoint != base);
        return result.toString();
    }

    /** Prints "hello world" — found by brute-force searching for the right seeds. */
    public static void helloWorld() {
        System.out.println(randomString(-229985452) + " " + randomString(-147909649));
    }

    // -----------------------------------------------------------------------
    // ROT-13 — a classic self-inverting letter substitution cipher.
    //
    // Applying ROT-13 twice returns the original text. This is achieved by
    // shifting each letter 13 positions in a 26-letter alphabet, so that
    // shifting twice gives 26 = full cycle.
    //
    // "Hello, world!" → "Uryyb, jbeyq!"
    // -----------------------------------------------------------------------

    // Lookup tables: each character's replacement is at the same index.
    private static final String LOWER      = "abcdefghijklmnopqrstuvwxyz";
    private static final String LOWER_ROT  = "nopqrstuvwxyzabcdefghijklm";
    private static final String UPPER      = LOWER.toUpperCase();
    private static final String UPPER_ROT  = LOWER_ROT.toUpperCase();

    /**
     * Apply the ROT-13 substitution cipher. Non-letter characters pass through
     * unchanged. Applying this method twice returns the original string.
     *
     * @param text the string to obfuscate
     * @return the ROT-13 transformed string
     */
    public static String rot13(String text) {
        var result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            int lowerIndex = LOWER.indexOf(ch);
            if (lowerIndex >= 0) {
                result.append(LOWER_ROT.charAt(lowerIndex));
            } else {
                int upperIndex = UPPER.indexOf(ch);
                result.append(upperIndex >= 0 ? UPPER_ROT.charAt(upperIndex) : ch);
            }
        }
        return result.toString();
    }

    // -----------------------------------------------------------------------
    // Disemvoweling — remove all vowels from a string.
    // -----------------------------------------------------------------------

    private static boolean isVowel(char ch) {
        return "AEIOUaeiou".indexOf(ch) >= 0;
    }

    /**
     * Remove all vowels from the given string.
     *
     * @param text the string to disemvowel
     * @return the string with all vowels removed
     */
    public static String disemvowel(String text) {
        var result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (!isVowel(ch)) { result.append(ch); }
        }
        return result.toString();
    }

    // -----------------------------------------------------------------------
    // Vowel counting with a special rule for "Y".
    //
    // The letter Y is counted as a vowel only if it does NOT have a vowel
    // immediately adjacent on either side. This captures the linguistic
    // intuition that Y acts as a vowel in "gym" but not in "oyster".
    // -----------------------------------------------------------------------

    /**
     * Count vowels in the given phrase, treating Y as a vowel when it has
     * no vowel neighbor on either side.
     *
     * @param phrase the phrase to analyze
     * @return the vowel count
     */
    public static int countVowels(String phrase) {
        int count = 0;
        for (int i = 0; i < phrase.length(); i++) {
            char ch = phrase.charAt(i);
            if (isVowel(ch)) {
                count++;
            } else if (ch == 'y' || ch == 'Y') {
                boolean leftNotVowel  = (i == 0
                        || !isVowel(phrase.charAt(i - 1)));
                boolean rightNotVowel = (i == phrase.length() - 1
                        || !isVowel(phrase.charAt(i + 1)));
                if (leftNotVowel && rightNotVowel) { count++; }
            }
        }
        return count;
    }

    // -----------------------------------------------------------------------
    // Digit-by-digit string addition — adding two nonnegative integers
    // represented as strings, producing a string result.
    //
    // This is essentially the same algorithm you learned in grade school
    // for adding numbers with pencil and paper, column by column from right
    // to left, carrying a 1 when a column sums to 10 or more.
    // -----------------------------------------------------------------------

    /**
     * Add two nonnegative integers encoded as digit strings.
     *
     * @param first  the first integer as a string
     * @param second the second integer as a string
     * @return the sum as a string
     */
    public static String addStrings(String first, String second) {
        var result = new StringBuilder();
        int carry = 0;
        int i = first.length() - 1;
        int j = second.length() - 1;

        while (i >= 0 || j >= 0 || carry > 0) {
            int digitA = (i >= 0) ? first.charAt(i--) - '0' : 0;
            int digitB = (j >= 0) ? second.charAt(j--) - '0' : 0;
            int sum = digitA + digitB + carry;
            result.append((char) ('0' + sum % 10));
            carry = sum / 10;
        }
        return result.reverse().toString();
    }

    // -----------------------------------------------------------------------
    // Fibonacci via string addition — and gold testing against BigInteger.
    //
    // "Gold testing" means testing your method against a known-correct
    // implementation that computes the same thing. If the two disagree,
    // something is wrong.
    // -----------------------------------------------------------------------

    /**
     * Compute the {@code n}-th Fibonacci number as a string, using our
     * {@code addStrings} method. Simultaneously computes via {@link BigInteger}
     * as a gold test, throwing an AssertionError on mismatch.
     *
     * @param n the index of the Fibonacci number (0-indexed: fib(0)=1, fib(1)=1)
     * @return the n-th Fibonacci number as a string
     */
    public static String fibonacci(int n) {
        String strPrev = "1", strCurr = "1";
        var bigPrev = BigInteger.ONE;
        var bigCurr = BigInteger.ONE;

        for (int i = 2; i <= n; i++) {
            String strNext = addStrings(strPrev, strCurr);
            strPrev = strCurr;
            strCurr = strNext;

            BigInteger bigNext = bigPrev.add(bigCurr);
            bigPrev = bigCurr;
            bigCurr = bigNext;

            assert strCurr.equals(bigCurr.toString())
                    : "Mismatch at fib(%d): string=%s, BigInteger=%s"
                    .formatted(i, strCurr, bigCurr);
        }
        return strCurr;
    }

    // -----------------------------------------------------------------------
    // "Count and say" — run-length encoding of a digit string.
    //
    // Read the digits aloud: "1111433" → "four ones, one four, two threes"
    // → "411423". Iterating this produces an interesting sequence.
    //
    // Puzzle: does there exist a string other than "" and "22" that is
    // "self-describing", meaning s.equals(countAndSay(s))?
    // -----------------------------------------------------------------------

    /**
     * Produce the "count and say" encoding of a digit string.
     *
     * @param digits the string of digits to encode
     * @return the run-length encoded result
     */
    public static String countAndSay(String digits) {
        if (digits.isEmpty()) { return ""; }
        var result = new StringBuilder();
        int runLength = 1;
        char runChar = digits.charAt(0);

        for (int i = 1; i <= digits.length(); i++) {
            char current = (i < digits.length()) ? digits.charAt(i) : '\0';
            if (current == runChar) {
                runLength++;
            } else {
                result.append(runLength).append(runChar);
                runChar = current;
                runLength = 1;
            }
        }
        return result.toString();
    }

    /**
     * Iterate "count and say" the given number of times, printing each step.
     *
     * @param start      the initial string
     * @param iterations how many times to apply the transformation
     */
    public static void iterateCountAndSay(String start, int iterations) {
        String current = start;
        System.out.println(current);
        for (int i = 0; i < iterations; i++) {
            current = countAndSay(current);
            System.out.println(current);
        }
    }

    // -----------------------------------------------------------------------
    // Reverse the order of words in a string.
    //
    // "Hello there, world!" → "world! there, Hello"
    //
    // The modern approach: split on whitespace, reverse the array, rejoin.
    // This is clearer and less error-prone than manual index tracking.
    // -----------------------------------------------------------------------

    /**
     * Reverse the order of words in a string while keeping each word intact.
     * Multiple spaces between words are normalized to single spaces.
     *
     * @param phrase the string whose words to reverse
     * @return the string with words in reverse order
     */
    public static String reverseWords(String phrase) {
        // Split on one or more whitespace characters, filter out any empty
        // strings (which can arise from leading/trailing whitespace).
        String[] words = phrase.strip().split("\\s+");

        // Reverse the array in place.
        for (int left = 0, right = words.length - 1; left < right; left++, right--) {
            String temp = words[left];
            words[left] = words[right];
            words[right] = temp;
        }
        return String.join(" ", words);
    }

    // -----------------------------------------------------------------------
    // Bonus: a stream-based version for comparison.
    // -----------------------------------------------------------------------

    /**
     * Reverse word order using streams — a concise functional alternative.
     * Collects words into a list, reverses it, and joins with spaces.
     */
    public static String reverseWordsStream(String phrase) {
        var words = Arrays.asList(phrase.strip().split("\\s+"));
        java.util.Collections.reverse(words);
        return String.join(" ", words);
    }

    // -----------------------------------------------------------------------
    // Main — exercise each example and display results.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        // --- Deterministic "random" strings ---
        System.out.println("--- Deterministic random strings ---");
        helloWorld();

        // --- ROT-13 ---
        System.out.println("\n--- ROT-13 ---");
        String message = "Hello, world! How are you doing there, buddy?";
        String rotated = rot13(message);
        String restored = rot13(rotated);
        System.out.println("Original:     " + message);
        System.out.println("ROT-13:       " + rotated);
        System.out.println("ROT-13 again: " + restored);
        System.out.println("Self-inverse:  " + message.equals(restored));

        // --- Disemvoweling ---
        System.out.println("\n--- Disemvoweling ---");
        System.out.println("Original:     " + message);
        System.out.println("Disemvoweled: " + disemvowel(message));

        // --- Vowel counting ---
        System.out.println("\n--- Vowel counting (with Y rule) ---");
        String[] testPhrases = {message, "Gym rhythm", "Oyster bay"};
        for (String phrase : testPhrases) {
            System.out.printf("\"%s\" → %d vowels%n", phrase, countVowels(phrase));
        }

        // --- Word reversal ---
        System.out.println("\n--- Word reversal ---");
        System.out.println("Original:  " + message);
        System.out.println("Reversed:  " + reverseWords(message));
        System.out.println("Stream:    " + reverseWordsStream(message));

        // --- String addition and Fibonacci ---
        System.out.println("\n--- String addition (Fibonacci gold test) ---");
        for (int n : new int[]{10, 30, 50, 100}) {
            String fib = fibonacci(n);
            System.out.printf("fib(%d) = %s (%d digits)%n", n, fib, fib.length());
        }

        // --- Count and say ---
        System.out.println("\n--- Count and say iteration ---");
        iterateCountAndSay("1", 10);

        // The puzzle: self-describing strings.
        System.out.println("\nSelf-describing check: countAndSay(\"22\") = \""
                + countAndSay("22") + "\"");
    }
}