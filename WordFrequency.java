import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Read a text file and compute word frequencies. Demonstrates modern Java I/O
 * with NIO Files.lines(), stream-based text processing, Map operations, and
 * Comparator composition. Updated for Java 21+.
 *
 * The previous version of this file contained an elaborate contraction-expansion
 * system ("doesn't" → "does not", "won't" → "will not", etc.) under the
 * delusion that computational linguistics somehow needs to remove contractions.
 * It has been taken out back and dealt with. The new version simply strips
 * apostrophes so that "don't" becomes "dont" — honest about its crudeness
 * rather than pretending to be NLP. For real tokenization, use a real library.
 *
 * @author Ilkka Kokkarinen
 */
public class WordFrequency {

    // To run this program, pass the filename as a command-line argument,
    // or change this default to some text file that you have.
    private static final String DEFAULT_FILE = "warandpeace.txt";

    // Precompiled regex pattern: anything that is not a lowercase letter.
    // Compiling once and reusing is faster than calling String.split() with
    // a regex string on every line (which recompiles the pattern each time).
    private static final Pattern NON_LETTER = Pattern.compile("[^a-z]+");

    /**
     * A record to pair a word with its count. Records (Java 16+) automatically
     * generate equals, hashCode, toString, and accessor methods.
     */
    public record WordCount(String word, int count) {}

    /**
     * Read all lines from the given path and return a map of word frequencies.
     * @param path The file to read.
     * @return A map from each word to the number of times it appears.
     * @throws IOException if the file cannot be read.
     */
    public static Map<String, Integer> wordFrequencies(Path path) throws IOException {
        // Files.lines() returns a lazy Stream<String> — it reads lines on demand
        // rather than loading the entire file into memory. The try-with-resources
        // ensures the underlying file handle is closed when the stream is done.
        Map<String, Integer> frequencies = new TreeMap<>();

        try (Stream<String> lines = Files.lines(path)) {
            lines
                    .map(String::toLowerCase)      // normalize case
                    .map(line -> line.replace("'", "")) // strip apostrophes (crude but honest)
                    .flatMap(NON_LETTER::splitAsStream)  // split into words, as a stream
                    .filter(word -> !word.isEmpty())      // discard empty fragments
                    .forEach(word ->
                            // Map.merge (Java 8+): if key absent, insert value; if present,
                            // apply the remapping function. Replaces the getOrDefault/put dance.
                            frequencies.merge(word, 1, Integer::sum)
                    );
        }
        return frequencies;
    }

    /**
     * Sort words by descending frequency, breaking ties alphabetically.
     * Built entirely from Comparator factory methods — no custom class needed.
     */
    public static List<WordCount> sortedByFrequency(Map<String, Integer> frequencies) {
        // Comparator.comparingInt extracts the sort key; reversed() flips to descending;
        // thenComparing adds a secondary sort on the word itself (ascending/alphabetical).
        return frequencies.entrySet().stream()
                .sorted(
                        Map.Entry.<String, Integer>comparingByValue().reversed()
                                .thenComparing(Map.Entry.comparingByKey())
                )
                .map(e -> new WordCount(e.getKey(), e.getValue()))
                .toList(); // Java 16+ unmodifiable list, replaces .collect(Collectors.toList())
    }

    /**
     * Print words wrapped to a maximum line width. Replaces the old LinePrinter
     * dependency with a simple, self-contained approach.
     */
    public static void printWrapped(List<String> items, int maxWidth) {
        int col = 0;
        for (String item : items) {
            if (col > 0 && col + 1 + item.length() > maxWidth) {
                System.out.println();
                col = 0;
            }
            if (col > 0) {
                System.out.print(" ");
                col++;
            }
            System.out.print(item);
            col += item.length();
        }
        System.out.println();
    }

    // For demonstration purposes, some word frequencies from "War and Peace".
    public static void main(String[] args) throws IOException {
        // Accept filename from command line, or fall back to the default.
        String filename = args.length > 0 ? args[0] : DEFAULT_FILE;
        Path path = Path.of(filename);

        if (!Files.exists(path)) {
            System.err.println("File not found: " + path);
            System.exit(1);
        }

        Map<String, Integer> frequencies = wordFrequencies(path);
        System.out.println("Found " + frequencies.size() + " distinct words.\n");

        // Look up some specific words.
        System.out.println("Some occurrence counts:");
        for (String word : List.of("chicken", "prince", "russia", "train", "i",
                "supercalifragilisticexpialidocius")) {
            System.out.println("  " + word + ": " + frequencies.getOrDefault(word, 0));
        }

        // Sort all words by frequency.
        List<WordCount> sorted = sortedByFrequency(frequencies);

        // Print the 300 most frequent words, wrapped to 80 columns.
        int topN = Math.min(300, sorted.size());
        System.out.println("\nThe " + topN + " most frequent words:\n");
        List<String> topFormatted = sorted.stream()
                .limit(topN)
                .map(wc -> wc.word() + " (" + wc.count() + ")")
                .toList();
        printWrapped(topFormatted, 80);

        // Print all words that occur exactly once (hapax legomena).
        List<String> hapaxes = sorted.stream()
                .filter(wc -> wc.count() == 1)
                .map(WordCount::word)
                .toList();
        System.out.println("\n" + hapaxes.size() + " words occur only once (hapax legomena):\n");
        printWrapped(hapaxes, 80);
    }
}