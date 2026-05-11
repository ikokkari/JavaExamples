import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Dissociated Press: a character-level Markov text generator.
 * <p>
 * The algorithm reads a corpus and, for every substring ("pattern") up to
 * {@code maxPatternLength} characters long, records which characters have
 * been observed to follow it. To generate text, it looks up the current
 * pattern in that map and picks a random follower, then slides the pattern
 * window forward. Longer patterns produce output that tracks the source
 * more closely; shorter ones produce wilder, more "dissociated" text.
 * <p>
 * The technique dates back to the 1970s Emacs "dissociated-press" command
 * and is essentially the character-level ancestor of modern n-gram and
 * neural language models.
 *
 * @author Ilkka Kokkarinen
 */
public class DissociatedPress {

    // To run the demo, point this at any reasonably large text file you have.
    private static final String DEFAULT_CORPUS = "warandpeace.txt";

    private static final SplittableRandom RNG = new SplittableRandom();

    // For each observed pattern, we store a list of characters that have
    // followed it. Using List<Character> instead of string concatenation
    // avoids quadratic growth from repeated += on strings, and lets us
    // index into it in O(1) for the random selection.
    private final Map<String, List<Character>> followMap = new HashMap<>();
    private final int maxPatternLength;
    private final int maxFollowSize;
    private String pattern;

    /**
     * Create a generator with default settings: single-space seed,
     * patterns up to 7 characters, follow lists capped at 200 entries.
     */
    public DissociatedPress() {
        this(" ", 7, 200);
    }

    /**
     * Create a generator with the given configuration.
     *
     * @param seed             the initial pattern (typically a space)
     * @param maxPatternLength the longest pattern to track
     * @param maxFollowSize    cap on followers stored per pattern
     */
    public DissociatedPress(String seed, int maxPatternLength, int maxFollowSize) {
        this.pattern = seed;
        this.maxPatternLength = maxPatternLength;
        this.maxFollowSize = maxFollowSize;
    }

    /**
     * Feed one character of training text into the model.
     * <p>
     * For every suffix of the current pattern, the character is appended
     * to that suffix's follow list (unless the list is already at capacity).
     * Then the pattern window slides forward.
     *
     * @param ch the next character from the training corpus
     */
    public void train(char ch) {
        for (int i = 0; i < pattern.length(); i++) {
            String suffix = pattern.substring(i);
            followMap.computeIfAbsent(suffix, _ -> new ArrayList<>());
            var followers = followMap.get(suffix);
            if (followers.size() < maxFollowSize) {
                followers.add(ch);
            }
        }
        pattern += ch;
        if (pattern.length() > maxPatternLength) {
            pattern = pattern.substring(1);
        }
    }

    /**
     * Train the model on an entire string (convenience wrapper).
     *
     * @param text the training text
     */
    public void trainOnText(String text) {
        for (int i = 0; i < text.length(); i++) {
            train(text.charAt(i));
        }
    }

    /**
     * Emit one character of generated text.
     * <p>
     * Starting from the current pattern (trimmed to {@code maxEmitLength}),
     * we try progressively shorter suffixes until we find one that has a
     * non-empty follow list. A random character is drawn from that list,
     * the pattern window slides forward, and the character is returned.
     *
     * @param maxEmitLength maximum pattern length to use during generation
     *                      (shorter = wilder output)
     * @return the generated character, or {@code '$'} if no pattern matched
     */
    public char emit(int maxEmitLength) {
        // Trim pattern to the requested emission length.
        if (pattern.length() > maxEmitLength) {
            pattern = pattern.substring(pattern.length() - maxEmitLength);
        }
        // Try progressively shorter suffixes of the pattern.
        while (!pattern.isEmpty()) {
            var followers = followMap.get(pattern);
            if (followers != null && !followers.isEmpty()) {
                char next = followers.get(RNG.nextInt(followers.size()));
                pattern += next;
                if (pattern.length() > maxPatternLength) {
                    pattern = pattern.substring(1);
                }
                return next;
            }
            pattern = pattern.substring(1);
        }
        return '$'; // no match — should not happen with a reasonable corpus
    }

    /**
     * Generate a string of the given length using the specified pattern depth.
     *
     * @param maxEmitLength the pattern depth for generation
     * @param length        how many characters to produce
     * @return the generated text
     */
    public String generate(int maxEmitLength, int length) {
        var sb = new StringBuilder(length);
        char prev = ' ';
        for (int i = 0; i < length; i++) {
            char next = emit(maxEmitLength);
            // Collapse runs of whitespace into single spaces.
            if (Character.isWhitespace(next) && Character.isWhitespace(prev)) {
                i--;
                continue;
            }
            sb.append(next);
            prev = next;
        }
        return sb.toString();
    }

    /**
     * Resets the internal pattern to a single space, so generation can
     * start fresh without rebuilding the model.
     */
    public void resetPattern() {
        this.pattern = " ";
    }

    // -----------------------------------------------------------------------
    // Diagnostics
    // -----------------------------------------------------------------------

    /** Summary statistics for one pattern length. */
    private record LevelStats(int patternLength, int distinctPatterns,
                               int saturatedPatterns, double avgFollowers) {}

    /**
     * Print a summary of the model: which characters appear in the corpus,
     * and for each pattern length, how many distinct patterns exist, how
     * many have hit the follower cap, and the average follow-list size.
     */
    public void printModelSummary() {
        // Collect the single-character patterns as the "alphabet".
        String alphabet = followMap.keySet().stream()
                .filter(p -> p.length() == 1)
                .sorted()
                .collect(Collectors.joining());
        System.out.println("Characters found in corpus: " + alphabet);

        // Gather per-length statistics.
        var stats = IntStream.rangeClosed(1, maxPatternLength)
                .mapToObj(len -> {
                    var patternsOfLen = followMap.entrySet().stream()
                            .filter(e -> e.getKey().length() == len)
                            .toList();
                    int count = patternsOfLen.size();
                    int saturated = (int) patternsOfLen.stream()
                            .filter(e -> e.getValue().size() == maxFollowSize)
                            .count();
                    double avgFollow = patternsOfLen.stream()
                            .mapToInt(e -> e.getValue().size())
                            .average()
                            .orElse(0.0);
                    return new LevelStats(len, count, saturated, avgFollow);
                })
                .toList();

        System.out.println("\nLength  Distinct  Saturated  Avg followers");
        for (var s : stats) {
            System.out.printf("%4d    %7d    %7d    %10.3f%n",
                    s.patternLength(), s.distinctPatterns(),
                    s.saturatedPatterns(), s.avgFollowers());
        }
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Demo
    // -----------------------------------------------------------------------

    /**
     * Read a text file, build the model, then demonstrate output at
     * each pattern depth from 1 (near-random) to 7 (closely tracking
     * the source).
     */
    public static void main(String[] args) throws IOException {
        String filename = (args.length > 0) ? args[0] : DEFAULT_CORPUS;
        String corpus = Files.readString(Path.of(filename));

        var dp = new DissociatedPress();
        dp.trainOnText(corpus);
        dp.printModelSummary();

        for (int depth = 1; depth <= 7; depth++) {
            if (depth > 1) { System.out.println("\n---\n"); }
            System.out.println("Pattern depth " + depth + ":");
            dp.resetPattern();
            String output = dp.generate(depth, 600);
            // Word-wrap at ~65 columns for readability.
            System.out.println(wordWrap(output, 65));
        }
    }

    /**
     * Wrap text to the specified line width, breaking at whitespace.
     */
    private static String wordWrap(String text, int width) {
        var sb = new StringBuilder(text.length() + text.length() / width);
        int col = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (col >= width && Character.isWhitespace(ch)) {
                sb.append('\n');
                col = 0;
            } else {
                sb.append(ch);
                col++;
            }
        }
        return sb.toString();
    }
}
