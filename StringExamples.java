import java.math.BigInteger;
import java.util.Random;

public class StringExamples {

    // First, a cute little example from Stack Overflow to demonstrate that
    // for given fixed seed, the series produced by Random is deterministic.
    public static String randomString(int i) {
        Random rng = new Random(i);
        StringBuilder sb = new StringBuilder();
        int k;
        int off = ('a' - 1);
        do {
            k = off + rng.nextInt(27);
            if(k > off) { sb.append((char)(k)); }
        } while(k != off);
        return sb.toString();
    }
    
    // Prints "hello world" on the console.
    public static void helloWorld() {
        System.out.println(randomString(-229985452) + " " + randomString(-147909649));
    }
    
    // Real programs and games use this idea for "procedural generation" of
    // levels, so that the level ID is used to seed the RNG that the level
    // generation algorithm internally uses. This way, the level is always
    // the same for everyone every time that they play that game.
    
    // Sometimes the easiest way is to build a lookup table for possible cases.
    // This table can be an if-else ladder, an array, or as in here, a string.
    private static final String lowerCase = "abcdefghijklmnopqrstuvwxyz";
    private static final String lowerCaseRot13 = "nopqrstuvwxyzabcdefghijklm";
    private static final String upperCase = lowerCase.toUpperCase();
    private static final String upperCaseRot13 = lowerCaseRot13.toUpperCase();

    /**
     * The classic self-inverting ROT-13 obfuscation scheme. For example,
     * {@code "Hello, world!"} becomes {@code "Uryyb, jbeyq!"}.
     * @param s The String to obfuscate.
     * @return The ROT-13 obfuscated string.
     */
    public static String rot13(String s) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < s.length(); i++) { 
            char c = s.charAt(i);
            int idx = lowerCase.indexOf(c); // is c a lowercase character?
            if(idx > -1) { result.append(lowerCaseRot13.charAt(idx)); }
            else {
                idx = upperCase.indexOf(c); // is c an uppercase character?
                if(idx > -1) { result.append(upperCaseRot13.charAt(idx)); }
                else { result.append(c); } // append c as it is
            }
        }
        return result.toString();
    }

    // Utility method needed in a couple of later methods.
    private static boolean isVowel(char c) {
        return "AEIOUaeiou".indexOf(c) > -1;
    }

    /**
     * Remove all vowels from the given string.
     * @param s The string to disemvowel.
     * @return The disemvoweled string.
     */ 
    public static String disemvowel(String s) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < s.length(); i++) {
            if(!isVowel(s.charAt(i))) { result.append(s.charAt(i)); }
        }
        return result.toString();
    }

    /**
     * Given two integers encoded as {@code String}, compute their sum as a
     * {@code String.} For simplicity, both integers must be nonnegative.
     * @param a The first integer.
     * @param b The second integer.
     * @return The sum of the integers, as a {@code String}.
     */
    public static String add(String a, String b) {
        StringBuilder c = new StringBuilder();
        int carry = 0;
        int i1 = a.length() - 1, i2 = b.length() - 1;
        while(i1 >= 0 || i2 >= 0) {
            int d1 = i1 >= 0 ? Character.getNumericValue(a.charAt(i1)) : 0;
            int d2 = i2 >= 0 ? Character.getNumericValue(b.charAt(i2)) : 0;
            int sum = d1 + d2 + carry;
            c.append((char)('0' + (sum % 10)));
            carry = sum / 10;
            i1--; i2--;
        }
        if(carry == 1) { c.append("1"); } // the last carry
        return c.reverse().toString();
    }

    // Let's test the previous method by computing the n:th Fibonacci number
    // using both the method and the known method of the BigInteger class.
    // (This is called "gold testing", testing your method against a method
    // that does the same thing and is known to be correct.)

    /**
     * Compute the {@code n}:th Fibonacci number as a {@code String}.
     * @param n The index of the Fibonacci number to compute.
     * @return The {@code n}:th Fibonacci number.
     */
    public static String fib(int n) {
        String f1 = "1", f2 = "1";
        BigInteger b1 = new BigInteger("1");
        BigInteger b2 = new BigInteger("1");
        for(int i = 2; i <= n; i++) {
            String f3 = add(f1, f2); // next Fibonacci with string add
            f1 = f2; f2 = f3; // shift "sliding window" one step left

            BigInteger b3 = b1.add(b2); // and with 
            b1 = b2; b2 = b3;

            assert f2.equals(b2.toString());
        }
        return f2;
    }

    /**
     * The "count and say" problem. Read the digits in the string "aloud",
     * producing a new string in the process. For example, the string
     * "1111433" read as "four ones, one four, two threes" would produce
     * the new string "411423".
     * @param s The {@code String} of digits to count and say.
     * @return The {@code String} of reading the parameter string aloud.
     */
    public static String countAndSay(String s) {
        if(s.length() == 0) { return ""; }
        StringBuilder res = new StringBuilder();
        int count = 1;
        char prev = s.charAt(0);
        for(int i = 1; i <= s.length(); i++) {
            char curr = i < s.length() ? s.charAt(i) : '$';
            if(curr == prev) { // the run continues
                count++;
            }
            else { // start a new run
                res.append(count);
                res.append(prev);
                prev = curr;
                count = 1;
            }
        }
        return res.toString();
    }

    /**
     * Iterate the "count and say" method the given number of times.
     * @param s The {@code String} to start the iteration from.
     * @param n The number of times to iterate.
     */
    public static void iterateCountAndSay(String s, int n) {
        System.out.println(s);
        for(int i = 0; i < n; i++) {
            s = countAndSay(s);
            System.out.println(s);
        }
    }

    // Puzzle for thought: does there exist some string s other than "" and "22"
    // that is "self-describing" in the sense that s.equals(countAndSay(s)) ?

    /**
     * Count how many vowels there are in the given phrase, using a special rule for "y"
     * so that "y" is a vowel if it does not have a vowel immediately on either side.
     * @param phrase The phrase from which we count the vowels.
     * @return The count of vowels in the given phrase.
     */
    public static int vowelCounter(String phrase) {
        int count = 0;
        for(int i = 0; i < phrase.length(); i++) {
            char c = phrase.charAt(i);
            if(isVowel(c)) { count++; } // unconditionally
            else if(c == 'y' || c == 'Y') { // handle Y as a special case
                boolean leftIsNotVowel = (i == 0 || !isVowel(phrase.charAt(i-1)));
                boolean rightIsNotVowel = (i == phrase.length()-1 || !isVowel(phrase.charAt(i+1)));
                if(leftIsNotVowel && rightIsNotVowel) { count++; }
            }
        }
        return count;
    }

    /**
     * Reverse the order of words in a string, while keeping each word as it is. For
     * example, "Hello there, world!" would become "world! there, Hello".
     * @param phrase The string the reverse the order of words in.
     * @return The string containing the original words in reverse order.
     */
    public static String wordReverser(String phrase) {
        int start = 0;
        boolean inWord = false;
        StringBuilder result = new StringBuilder();
        for(int i = 0; i <= phrase.length(); i++) {
            if(i == phrase.length() || Character.isWhitespace(phrase.charAt(i))) {
                if(inWord) {
                    inWord = false;
                    result.insert(0, new StringBuilder().append(phrase, start, i).append(result.length() == 0 ? "" : " ").toString());
                }
            }
            else {
                if(!inWord) { start = i; }
                inWord = true;
            }
        }
        return result.toString();
    }

    public static void main(String[] args) {
        String message = "Hello, world! How are you doing there, buddy?";
        String rotated = rot13(message);
        String back = rot13(rotated);
        System.out.println("Original message: " + message);
        System.out.println("Rotated message : " + rotated);
        System.out.println("Rotated again   : " + back);

        System.out.println("Disemvoweled original: " + disemvowel(message));
        System.out.println("The original message contains " + vowelCounter(message) + " vowels.");
        System.out.print("Reversing the words of original message gives: ");
        System.out.println(wordReverser(message));

        System.out.print("Computed using strings, the 30:th Fibonacci number is: ");
        System.out.println(fib(30));

        String original = "123";
        while(original.length() < 100) {
            String counted = countAndSay(original);
            System.out.println("Count and say of " + original + " is " + counted);
            original = counted;
        }
    }
}