import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;

/**
 * An immensely useful class that for some reason is missing from the Java
 * standard library. (Google "why Java has no Pair class" for a lively debate.)
 *
 * The original version of this file contained a hand-written Pair with
 * manually coded getFirst(), getSecond(), equals(), hashCode(), and toString().
 * That was 70 lines of careful boilerplate. As of Java 16, a record does
 * all of that in a single line:
 *
 *     record Pair<T, U>(T first, U second) {}
 *
 * That's it. The compiler generates:
 *   - a constructor Pair(T first, U second)
 *   - accessor methods first() and second() (not getFirst/getSecond)
 *   - equals() that compares both components
 *   - hashCode() that combines both components
 *   - toString() that shows both components
 *
 * All final, all correct, all free. The rest of this file exists to explain
 * what's happening under the hood and to show what you would still need to
 * write by hand if you wanted to customize the behaviour.
 *
 * @author Ilkka Kokkarinen
 */
public final class Pair<T, U> {

    // A generic class can have static members, but they cannot use the type
    // parameters in any way (because of type erasure — at runtime, there is
    // only one Pair class, not one per type argument combination).
    private static int count = 0;
    public static int getCount() { return count; }

    // The components. Marked final — a Pair, like a record, is immutable.
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        count++;
        this.first = first;
        this.second = second;
    }

    public T first()  { return first; }   // Record-style accessors (no "get" prefix).
    public U second() { return second; }

    // --- toString, equals, hashCode ---
    // A record would generate all three of these automatically.
    // We write them here so students can see what the compiler does for them.

    @Override
    public String toString() {
        return "Pair[first=" + first + ", second=" + second + "]";
    }

    @Override
    public boolean equals(Object other) {
        // Java 16+ pattern matching for instanceof: tests the type AND binds
        // a variable in one step. No separate cast needed.
        if (other instanceof Pair<?, ?> that) {
            // Objects.equals handles nulls safely — returns true if both are
            // null, false if only one is, and delegates to .equals() otherwise.
            return Objects.equals(this.first, that.first)
                    && Objects.equals(this.second, that.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        // Objects.hash (Java 7+) computes a well-distributed combined hash code
        // from any number of fields. It replaces the manual bit-shifting and
        // XOR gymnastics that we used to write by hand. Under the hood, it
        // uses Arrays.hashCode with a prime multiplier (31), which is the same
        // algorithm that the compiler generates for records.
        return Objects.hash(first, second);
    }

    // -----------------------------------------------------------------------
    // THE RECORD ALTERNATIVE
    // -----------------------------------------------------------------------
    // If you don't need the static counter or any other customization, you
    // can replace this entire 80-line class with:
    //
    //     record SimplePair<T, U>(T first, U second) {}
    //
    // Let's define one and prove it works identically:

    record SimplePair<T, U>(T first, U second) {}

    // -----------------------------------------------------------------------
    // CONVENIENCE FACTORY METHOD (Java 5+ style, popularized by Java 9 Map.of)
    // -----------------------------------------------------------------------
    // A static factory method that infers the type arguments, so callers can
    // write Pair.of("hello", 42) instead of new Pair<String, Integer>("hello", 42).

    public static <T, U> Pair<T, U> of(T first, U second) {
        return new Pair<>(first, second);
    }

    // -----------------------------------------------------------------------
    // DEMO: hash code quality test using War and Peace
    // -----------------------------------------------------------------------

    public static void main(String[] args) throws IOException {
        Path path = Path.of(args.length > 0 ? args[0] : "warandpeace.txt");
        if (!Files.exists(path)) {
            // Create a sample file so the demo is self-contained.
            Files.writeString(path, """
                    Prince Vasili came out first he could not restrain a triumphant smile
                    The little princess came after him she could not see for tears
                    The whole household was plunged in confusion and dismay
                    War and peace peace and war the eternal dance of nations
                    """.repeat(100)); // repeat to get a decent word count
        }

        // Count how many distinct hash codes we get from (word, index) pairs.
        // The closer this is to the total word count, the better the hash function.
        var seenHand = new HashSet<Integer>();  // hand-written Pair
        var seenRecord = new HashSet<Integer>(); // record SimplePair
        int wordNo = 0;

        for (String line : Files.readAllLines(path)) {
            for (String word : line.split("\\s+")) {
                if (word.isEmpty()) continue;
                seenHand.add(Pair.of(word, wordNo).hashCode());
                seenRecord.add(new SimplePair<>(word, wordNo).hashCode());
                wordNo++;
            }
        }

        System.out.println("Total word occurrences: " + wordNo);
        System.out.println("Distinct hash codes (hand-written): " + seenHand.size());
        System.out.println("Distinct hash codes (record):       " + seenRecord.size());
        System.out.println("Pairs created (hand-written only):  " + Pair.getCount());

        // Demonstrate that the hand-written Pair and the record behave identically.
        var p1 = Pair.of("hello", 42);
        var p2 = Pair.of("hello", 42);
        var p3 = Pair.of("world", 42);
        var r1 = new SimplePair<>("hello", 42);

        System.out.println("\np1 = " + p1);
        System.out.println("r1 = " + r1);
        System.out.println("p1.equals(p2): " + p1.equals(p2)); // true
        System.out.println("p1.equals(p3): " + p1.equals(p3)); // false
        System.out.println("p1.hashCode() == p2.hashCode(): " + (p1.hashCode() == p2.hashCode()));

        // The record's toString looks almost identical:
        // Pair[first=hello, second=42] vs SimplePair[first=hello, second=42]
    }
}