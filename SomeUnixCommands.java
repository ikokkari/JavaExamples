import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;

/**
 * Java implementations of a few classic Unix text-processing commands,
 * demonstrating Reader/Writer decoration and line-oriented I/O.
 * Updated for Java 21+ with NIO and stream-based alternatives.
 *
 * Each method follows the Unix philosophy: read from a Reader, write to
 * a Writer, and let the caller decide what those are connected to (a file,
 * stdin/stdout, a socket, a StringWriter for testing, etc.). The methods
 * flush but never close their output — closing System.out would be fatal,
 * and it is the caller's responsibility to manage the lifecycle of the
 * streams it owns.
 *
 * @author Ilkka Kokkarinen
 */
public class SomeUnixCommands {

    /**
     * Emulate {@code uniq}: suppress consecutive duplicate lines.
     * (The real Unix uniq requires sorted input to remove all duplicates;
     * this faithfully reproduces that behaviour.)
     */
    public static void uniq(Reader in, Writer out) throws IOException {
        var br = new BufferedReader(in);
        var pw = new PrintWriter(out);
        String prev = null;
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.equals(prev)) {
                pw.println(line);
            }
            prev = line;
        }
        pw.flush(); // Flush but do NOT close — closing System.out is fatal.
    }

    /**
     * Emulate {@code rev}: output each line with its characters reversed.
     */
    public static void rev(Reader in, Writer out) throws IOException {
        var br = new BufferedReader(in);
        var pw = new PrintWriter(out);
        String line;
        while ((line = br.readLine()) != null) {
            pw.println(new StringBuilder(line).reverse());
        }
        pw.flush();
    }

    /**
     * Emulate {@code cut -f}: output only the specified fields (0-indexed)
     * from each line, where fields are separated by {@code sep}.
     *
     * @param in     The input reader.
     * @param out    The output writer.
     * @param sep    The field separator regex (e.g. " " or "\\t" or ",").
     * @param fields The 0-based indices of the fields to extract.
     */
    public static void cut(Reader in, Writer out, String sep, int... fields)
            throws IOException {
        var br = new BufferedReader(in);
        var pw = new PrintWriter(out);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.isEmpty()) { continue; } // isEmpty() over length()==0
            String[] parts = line.split(sep);
            // StringJoiner produces "a b c" without a trailing/leading space.
            // Cleaner than the manual "if (i > 0) print separator" pattern.
            var joiner = new StringJoiner(" ");
            for (int idx : fields) {
                if (idx < parts.length) {
                    joiner.add(parts[idx]);
                }
            }
            pw.println(joiner);
        }
        pw.flush();
    }

    // -----------------------------------------------------------------------
    // DEMO
    // -----------------------------------------------------------------------

    public static void main(String[] args) throws IOException {
        // Create a sample file so this demo is self-contained.
        // Files.writeString (Java 11+) writes a String to a file in one call.
        // Text blocks (Java 15+) make the sample text readable inline.
        Path sample = Path.of("lorem.txt");
        Files.writeString(sample, """
                Lorem ipsum dolor sit amet consectetur adipiscing elit
                Lorem ipsum dolor sit amet consectetur adipiscing elit
                sed do eiusmod tempor incididunt ut labore et dolore
                magna aliqua ut enim ad minim veniam quis nostrud
                magna aliqua ut enim ad minim veniam quis nostrud
                magna aliqua ut enim ad minim veniam quis nostrud
                exercitation ullamco laboris nisi ut aliquip ex ea
                """);

        // Files.newBufferedReader (Java 7+) returns a BufferedReader with
        // UTF-8 encoding by default — no need for the old three-layer
        // InputStreamReader(FileInputStream(...), StandardCharsets.UTF_8) chain.
        // Each reader is wrapped in try-with-resources for proper cleanup.

        System.out.println("=== uniq ===");
        try (var reader = Files.newBufferedReader(sample)) {
            uniq(reader, new PrintWriter(System.out));
        }

        System.out.println("\n=== rev ===");
        try (var reader = Files.newBufferedReader(sample)) {
            rev(reader, new PrintWriter(System.out));
        }

        System.out.println("\n=== cut -f 1,3,6 ===");
        try (var reader = Files.newBufferedReader(sample)) {
            cut(reader, new PrintWriter(System.out), " ", 1, 3, 6);
        }

        // Clean up the sample file.
        Files.deleteIfExists(sample);
    }
}