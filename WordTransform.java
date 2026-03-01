import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Word transformation engine: apply fun linguistic transforms (reversal,
 * Pig Latin, Ubbi Dubbi) to every word in a sentence while preserving
 * punctuation, spacing, and capitalization.
 * <p>
 * This example demonstrates the <b>Strategy pattern</b>: the transformation
 * algorithm is passed as a parameter, so the sentence-walking logic is
 * written once and reused for any transform. In modern Java, the strategy
 * is simply a {@link UnaryOperator}{@code <String>} — no need for a custom
 * interface or separate classes.
 * <p>
 * Updated for Java 21+ with lambdas, regex-based word extraction, and
 * helper methods that eliminate repeated case-handling boilerplate.
 *
 * @author Ilkka Kokkarinen
 */
public class WordTransform {

    // -----------------------------------------------------------------------
    // Helper: re-apply the capitalization pattern of `model` onto `target`.
    // This eliminates the repeated cap-handling code in every transform.
    // -----------------------------------------------------------------------

    /**
     * Transfer the capitalization of the first character of {@code model}
     * onto the first character of {@code target}, lowercasing the rest.
     * For example, {@code matchCase("Hello", "orldw")} → {@code "Orldw"}.
     */
    private static String matchCase(String model, String raw) {
        if (raw.isEmpty()) { return raw; }
        char first = Character.isUpperCase(model.charAt(0))
                ? Character.toUpperCase(raw.charAt(0))
                : Character.toLowerCase(raw.charAt(0));
        return first + raw.substring(1).toLowerCase();
    }

    // -----------------------------------------------------------------------
    // Helper: is this character a vowel? (Y included for these transforms.)
    // -----------------------------------------------------------------------

    private static boolean isVowel(char ch) {
        return "AEIOUYaeiouy".indexOf(ch) >= 0;
    }

    // -----------------------------------------------------------------------
    // Transform strategies — each is a simple static method that can be
    // passed as a method reference (UnaryOperator<String>).
    // -----------------------------------------------------------------------

    /**
     * Reverse the letters of a word, preserving the original capitalization
     * pattern (first letter capitalized if the original was).
     */
    public static String reverse(String word) {
        String reversed = new StringBuilder(word.toLowerCase()).reverse().toString();
        return matchCase(word, reversed);
    }

    /**
     * Convert a word to Pig Latin:
     * <ul>
     *   <li>If the word starts with a vowel, append "way": "apple" → "appleway"</li>
     *   <li>Otherwise, move the leading consonant cluster to the end and append
     *       "ay": "string" → "ingstray", "Hello" → "Ellohay"</li>
     * </ul>
     * Capitalization of the original first letter is preserved.
     */
    public static String pigLatin(String word) {
        // Find the index of the first vowel.
        int firstVowel = 0;
        while (firstVowel < word.length() && !isVowel(word.charAt(firstVowel))) {
            firstVowel++;
        }

        if (firstVowel == 0) {
            // Starts with a vowel: just append "way".
            return word + "way";
        }
        // Move consonant cluster to the end, append "ay".
        String rotated = word.substring(firstVowel) + word.substring(0, firstVowel).toLowerCase() + "ay";
        return matchCase(word, rotated);
    }

    /**
     * Convert a word to Ubbi Dubbi: insert "ub" before every vowel.
     * "Hello" → "Hubellubo", "world" → "world" (no vowels to modify).
     */
    public static String ubbiDubbi(String word) {
        var result = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (!isVowel(ch)) {
                result.append(ch);
            } else if (Character.isUpperCase(ch)) {
                result.append("Ub").append(Character.toLowerCase(ch));
            } else {
                result.append("ub").append(ch);
            }
        }
        return result.toString();
    }

    // -----------------------------------------------------------------------
    // The sentence-level engine: find each word, apply the transform,
    // and preserve all non-letter characters in place.
    //
    // Uses a regex Matcher to cleanly separate words from non-words,
    // replacing the manual state-machine approach.
    // -----------------------------------------------------------------------

    /** Pattern matching one or more consecutive letters. */
    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z]+");

    /**
     * Apply a word transformation to every word in the given text, preserving
     * all punctuation, spacing, and other non-letter characters.
     *
     * @param text        the input text
     * @param transformer a function that transforms a single word
     * @return the text with every word transformed
     */
    public static String transformSentence(String text, UnaryOperator<String> transformer) {
        // Matcher.replaceAll with a lambda (Java 9+) handles the word
        // extraction and reassembly cleanly — no manual index tracking.
        Matcher matcher = WORD_PATTERN.matcher(text);
        return matcher.replaceAll(match -> transformer.apply(match.group()));
    }

    // -----------------------------------------------------------------------
    // Main — demonstrate each transform with method references.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        String text = "What does this become? We shall see!";

        System.out.println("Original:   " + text);
        System.out.println();

        // Method references serve as strategy objects — no classes needed.
        System.out.println("Reversed:   " + transformSentence(text, WordTransform::reverse));
        System.out.println("Pig Latin:  " + transformSentence(text, WordTransform::pigLatin));
        System.out.println("Ubbi Dubbi: " + transformSentence(text, WordTransform::ubbiDubbi));

        // Lambdas work too — here's an inline "shout" transform:
        System.out.println("Shouting:   " + transformSentence(text, String::toUpperCase));

        // And transforms can be composed:
        System.out.println("Reversed Pig Latin: "
                + transformSentence(text,
                word -> reverse(pigLatin(word))));

        // Additional test cases to verify punctuation and case handling.
        System.out.println();
        String[] testSentences = {
                "I am the walrus, said John.",
                "To be, or not to be -- that is the question!",
                "Don't count your chickens before they hatch.",
        };
        for (String sentence : testSentences) {
            System.out.println("Original:  " + sentence);
            System.out.println("Pig Latin: " + transformSentence(sentence, WordTransform::pigLatin));
            System.out.println();
        }
    }
}