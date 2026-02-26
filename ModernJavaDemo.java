import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.PriorityQueue;

import static java.lang.Math.sqrt;
import static java.lang.System.out;

/**
 * Demonstrate various Java features from Java 5 through 21, with honest
 * commentary about which features have aged well and which have not.
 * Originally a "Java 5 features" demo — but Java 5 came out in 2005,
 * and we now have students who are younger than Java 5, Java 8, AND
 * Java 11. The features shown here are no longer "new"; they are simply
 * "how you write Java."
 *
 * @author Ilkka Kokkarinen
 */
public class ModernJavaDemo {

    // -----------------------------------------------------------------------
    // ANNOTATIONS (Java 5)
    // -----------------------------------------------------------------------
    // To define your own annotation type, use @interface. Annotations can
    // themselves be annotated with meta-annotations that control their
    // retention policy (how long they survive) and legal targets.

    @Retention(RetentionPolicy.RUNTIME) // keep until runtime (for reflection)
    @Target(ElementType.TYPE)           // may be applied to classes and other types
    @interface Author {
        String name();
        int year() default 2026; // how the time goes by...
    }

    // Let us use our new annotation type on a little class.
    @Author(name = "Ilkka Kokkarinen")
    private static class AnnotationExample { /* whatevs */ }

    // Use reflection to examine the annotation at runtime. If the Author
    // annotation were discarded after compilation (RetentionPolicy.CLASS),
    // it would not show up here.
    public static void annotationDemo() {
        out.println("Starting annotationDemo.");
        var example = new AnnotationExample();
        Class<? extends AnnotationExample> c = example.getClass();
        for (Annotation ant : c.getAnnotations()) {
            out.println("Found annotation: " + ant);
        }
        out.println("Finished annotationDemo.\n");
    }

    // -----------------------------------------------------------------------
    // BOXING, UNBOXING, AND THE INTEGER CACHE (Java 5)
    // -----------------------------------------------------------------------
    // Autoboxing silently converts between primitives and their wrapper classes.
    // This is convenient but hides a sharp edge: the Integer cache.

    public static void boxingDemo() {
        out.println("Starting boxingDemo.");

        Integer a = 42;     // Autoboxing: int → Integer. Value ∈ [-128, 127], so cached.
        Integer b = 42;     // Same cached object is reused.
        int c = 42;         // Plain primitive.
        assert a == c;      // Wrapper vs. primitive: auto-unbox, then compare values. OK.
        assert a == b;      // Wrapper vs. wrapper: == compares references. Same cache → true.
        assert a.equals(b); // .equals() compares content. Always safe.

        Integer d = 84;     // Still within [-128, 127] — cached.
        // new Integer(84) was deprecated in Java 9 and removed in Java 16.
        // Use Integer.valueOf(84) instead, which uses the cache.
        Integer e = Integer.valueOf(84);
        // valueOf may or may not return the same object as d — the cache range
        // is guaranteed for [-128, 127] but implementations may extend it.
        assert d.equals(e); // Content comparison — always correct.
        assert a + b == e;  // Arithmetic auto-unboxes both sides before operating.
        assert a < e;       // Order comparisons also auto-unbox.

        Integer f = 9999;   // Well outside the cache range.
        Integer g = 9999;   // A different object, despite the same value.
        assert f != g;      // == compares references: guaranteed different.
        assert f.equals(g); // .equals() compares values: equal.

        // Moral: NEVER use == to compare wrapper objects. Use .equals() or unbox first.
        // This is the #1 autoboxing trap in Java.

        out.println("Sleep well. Integers still work the way they are supposed to.");
        out.println("Finished boxingDemo.\n");
    }

    // -----------------------------------------------------------------------
    // VARARGS (Java 5)
    // -----------------------------------------------------------------------
    // The ... syntax lets a method accept zero or more arguments of the given type.
    // Internally, Java creates an array — but the caller doesn't have to.

    public static String concatenate(Object... rest) {
        // StringJoiner or String.join would be more modern here, but the point
        // of this demo is to show varargs, not string joining.
        var result = new StringBuilder();
        for (Object o : rest) { result.append(o).append(" "); }
        return result.toString();
    }

    // -----------------------------------------------------------------------
    // THE ORWELLIAN DEMO (Java 5 reflection, increasingly hostile since Java 9)
    // -----------------------------------------------------------------------
    // Adapted from a classic Stack Overflow thread. This mutates the JVM's
    // internal Integer cache via reflection, making 2+2 print as 5.
    //
    // Since Java 9's module system, accessing private internals of java.lang
    // requires --add-opens on the command line. Since Java 16, strong
    // encapsulation is the default. Run with:
    //   java --add-opens java.base/java.lang=ALL-UNNAMED ModernJavaDemo
    // to see this demo work. Without it, you get an InaccessibleObjectException.
    //
    // This is a feature, not a bug. The module system exists precisely to
    // prevent code from reaching into the JVM's underwear drawer like this.

    public static void orwellianDemo() throws Exception {
        out.println("Starting orwellianDemo.");
        Class<?> cacheType = Integer.class.getDeclaredClasses()[0];
        Field cacheField = cacheType.getDeclaredField("cache");
        cacheField.setAccessible(true); // bypasses access control (if modules allow it)
        Integer[] cachedIntegers = (Integer[]) cacheField.get(cacheType);
        cachedIntegers[132] = cachedIntegers[133]; // position 132 = value 4, 133 = value 5
        // Two plus two is whatever The Party says it is, Winston.
        out.printf("Two plus two equals %d.%n", 2 + 2); // prints 5
        // Even the basic laws of arithmetic must yield to doublethink.
        // (Don't forget to restore sanity if running more code after this.)
        cachedIntegers[132] = 4; // restore, because we're not actual totalitarians
        out.println("Finished orwellianDemo.\n");
    }

    // -----------------------------------------------------------------------
    // PRIORITY QUEUE (Java 5)
    // -----------------------------------------------------------------------
    // Queue-as-a-Collection is an absurdity in terms. Saying that a Queue
    // is-a Collection is like saying that a Car is-an Engine.
    // That said, PriorityQueue<T> often comes handy in clever algorithms.
    // Just don't pretend that it is also a Collection. (Or a floor wax.
    // Or a dessert topping.)

    public static void queueDemo() {
        out.println("Starting queueDemo.");

        // Natural ordering (min-heap by default).
        var minHeap = new PriorityQueue<Integer>();
        minHeap.add(7);
        minHeap.offer(2);
        minHeap.offer(3);
        out.println("head: " + minHeap.peek());   // 2
        out.println("pop:  " + minHeap.remove());  // 2
        out.println("head: " + minHeap.peek());   // 3
        out.println("pop:  " + minHeap.remove());  // 3
        out.println("pop:  " + minHeap.remove());  // 7

        // Max-heap via Comparator.reverseOrder() — no need to write -x hacks.
        var maxHeap = new PriorityQueue<>(Comparator.<Integer>reverseOrder());
        maxHeap.addAll(List.of(7, 2, 3));
        out.println("Max-heap pops: "
                + maxHeap.remove() + " "   // 7
                + maxHeap.remove() + " "   // 3
                + maxHeap.remove());       // 2

        // Custom comparator: sort strings by length, then alphabetically.
        // Comparator.comparing (Java 8+) builds comparators from key extractors.
        var byLength = new PriorityQueue<>(
                Comparator.comparingInt(String::length)
                        .thenComparing(Comparator.naturalOrder())
        );
        byLength.addAll(List.of("banana", "fig", "apple", "kiwi", "date"));
        out.print("By length: ");
        while (!byLength.isEmpty()) {
            out.print(byLength.remove() + " ");
        }
        out.println();

        out.println("Finished queueDemo.\n");
    }

    // -----------------------------------------------------------------------
    // OPTIONAL (Java 8)
    // -----------------------------------------------------------------------
    // Optional<T> represents a value that might not be there. It replaces the
    // "return null and hope the caller checks" pattern that has caused more
    // NullPointerExceptions than any other design decision in Java's history.

    public static void optionalDemo() {
        out.println("Starting optionalDemo.");

        // Creating Optionals:
        Optional<String> present = Optional.of("Hello");
        Optional<String> empty = Optional.empty();
        Optional<String> nullable = Optional.ofNullable(null); // same as empty()

        // Querying:
        out.println("present.isPresent(): " + present.isPresent()); // true
        out.println("empty.isEmpty(): " + empty.isEmpty());         // true (Java 11+)

        // Extracting values safely:
        out.println("present.orElse(\"default\"): " + present.orElse("default"));
        out.println("empty.orElse(\"default\"): " + empty.orElse("default"));

        // Transforming with map (returns Optional):
        Optional<Integer> length = present.map(String::length);
        out.println("present.map(length): " + length.orElse(-1)); // 5

        // ifPresent replaces the if-not-null check:
        present.ifPresent(s -> out.println("The value is: " + s));
        empty.ifPresent(s -> out.println("This never prints."));

        // ifPresentOrElse (Java 9+): handles both branches.
        empty.ifPresentOrElse(
                s -> out.println("Found: " + s),
                () -> out.println("Nothing here — and that's OK.")
        );

        // or() (Java 9+): provide a fallback Optional.
        Optional<String> result = empty.or(() -> Optional.of("fallback"));
        out.println("empty.or(fallback): " + result.orElseThrow());

        out.println("Finished optionalDemo.\n");
    }

    // -----------------------------------------------------------------------
    // RECORDS (Java 16) — because we keep showing them everywhere else
    // -----------------------------------------------------------------------
    // A record is a concise way to declare a class that is just data: the
    // compiler generates the constructor, accessors, equals, hashCode, and
    // toString. Think of it as Java's answer to Python's dataclass or
    // namedtuple.

    record Point(double x, double y) {
        // Records can have methods and static members. They cannot have
        // mutable instance fields — all components are final.
        double distanceTo(Point other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            return sqrt(dx * dx + dy * dy);
        }
    }

    public static void recordDemo() {
        out.println("Starting recordDemo.");
        var a = new Point(3, 4);
        var b = new Point(0, 0);
        out.println("Point a: " + a);            // Point[x=3.0, y=4.0]
        out.println("a.x(): " + a.x());          // accessor, not getX()
        out.println("Distance: " + a.distanceTo(b));

        // Records get equals() and hashCode() for free.
        var c = new Point(3, 4);
        out.println("a.equals(c): " + a.equals(c)); // true — value equality
        out.println("a == c: " + (a == c));          // false — different objects

        // Records work beautifully with pattern matching (Java 21):
        record Circle(Point center, double radius) {}
        record Rectangle(Point topLeft, Point bottomRight) {}

        Object shape = new Circle(new Point(1, 2), 5.0);
        String description = switch (shape) {
            case Circle(var center, var r)
                    -> "Circle at " + center + " with radius " + r;
            case Rectangle(var tl, var br)
                    -> "Rectangle from " + tl + " to " + br;
            default -> "Unknown shape";
        };
        out.println(description);
        out.println("Finished recordDemo.\n");
    }

    // -----------------------------------------------------------------------
    // COMPACT NUMBER FORMAT (Java 12)
    // -----------------------------------------------------------------------
    // A fun little utility class that formats numbers the way humans actually
    // talk about them.

    public static void compactNumberDemo() {
        out.println("Starting compactNumberDemo.");
        var fmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        fmt.setMaximumFractionDigits(1);
        for (long n : new long[]{42, 1_200, 53_000, 7_800_000, 2_100_000_000L}) {
            out.println(n + " → " + fmt.format(n));
        }
        out.println("Finished compactNumberDemo.\n");
    }

    // -----------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        annotationDemo();
        boxingDemo();

        // Varargs demo — inline since concatenate is just a helper.
        out.println("Concatenate: " + concatenate("hello", 42, 3.14, true));

        queueDemo();
        optionalDemo();
        recordDemo();
        compactNumberDemo();

        // The Orwellian demo requires --add-opens to bypass module encapsulation.
        // Run with: java --add-opens java.base/java.lang=ALL-UNNAMED ModernJavaDemo
        try {
            orwellianDemo();
        } catch (Exception e) {
            out.println("orwellianDemo failed (as expected without --add-opens): "
                    + e.getClass().getSimpleName());
            out.println("Run with: java --add-opens java.base/java.lang=ALL-UNNAMED "
                    + "ModernJavaDemo\n");
        }
    }
}