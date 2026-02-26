import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.random.RandomGenerator;

/**
 * Demonstrate various aspects of primitive types and Strings in Java.
 * A whole bunch of static methods that show things off — does not do
 * anything else that is useful. Updated for Java 21+.
 * @author Ilkka Kokkarinen
 */
public class DataDemo {

    // A utility class used to format decimal numbers for console output.
    private static final DecimalFormat df = new DecimalFormat();
    // A static initializer block runs once when the JVM loads this class.
    // Handy for configuring static members — like a "constructor" for the class itself.
    static {
        df.setMinimumFractionDigits(3);
        df.setMaximumFractionDigits(3);
    }

    // -----------------------------------------------------------------------
    // ASSIGNMENT
    // -----------------------------------------------------------------------

    /**
     * Demonstrate the behaviour of the assignment statement in Java.
     */
    public static void assignmentDemo() {
        System.out.println("Assignment demo begins:");
        int a = 3;
        int b = a + 2;
        System.out.println("a is now " + a + " and b is now " + b); // 3 5
        a = 5; // Note that b doesn't change — Java is not a spreadsheet.
        System.out.println("a is now " + a + " and b is now " + b); // 5 5
        b = b + 2; // Same variable on both sides of = is perfectly fine.
        System.out.println("a is now " + a + " and b is now " + b); // 5 7
        System.out.println("Assignment demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // STRINGS
    // -----------------------------------------------------------------------

    /**
     * Demonstrate the String class and some of its operations for text.
     */
    public static void stringDemo() {
        System.out.println("String demo begins:");
        String s = "Hello, world!"; // the canonical example text
        System.out.println("s equals \"" + s + "\"");
        System.out.println("Converted to lowercase: \"" + s.toLowerCase() + "\"");
        System.out.println("Character at index 5: " + s.charAt(5));
        System.out.println("Substring from 5 to end: \"" + s.substring(5) + "\"");
        System.out.println("Substring from 3 to 7: \"" + s.substring(3, 8) + "\"");

        // String is immutable: operations that "modify" it always create a new String.
        String s2 = s.replaceAll("ello", "owdy");
        System.out.println("s  = \"" + s + "\"");
        System.out.println("s2 = \"" + s2 + "\"");

        // --- Modern String methods (Java 11+) ---
        // strip() is Unicode-aware, unlike the older trim().
        System.out.println("\"  padded  \".strip() = \"" + "  padded  ".strip() + "\"");
        // repeat() replaces manual StringBuilder loops.
        System.out.println("\"ha\".repeat(5) = \"" + "ha".repeat(5) + "\"");
        // isBlank() checks for empty or all-whitespace.
        System.out.println("\"  \".isBlank() = " + "  ".isBlank());
        // indent() adds or removes leading spaces from each line.
        System.out.println("Indented two spaces:\n" + "line one\nline two".indent(2));

        // --- Text blocks (Java 15+) ---
        // Multi-line string literals. The closing """ determines the left margin;
        // incidental whitespace to the left of it is stripped automatically.
        String json = """
                {
                    "name": "Ilkka",
                    "course": "CCPS 209",
                    "year": 2026
                }
                """;
        System.out.println("A JSON text block:\n" + json);

        // --- formatted() (Java 15+) ---
        // An instance method alternative to String.format(). Reads more naturally.
        String greeting = "Hello, %s! You are %d years old.".formatted("Alice", 25);
        System.out.println(greeting);

        System.out.println("String demo ends.\n");
    }

    /**
     * Demonstrate the difference between reference equality ({@code ==}) and
     * content equality ({@code equals}).
     */
    public static void stringEqualityDemo() {
        System.out.println("String equality demo begins:");
        String a = "Hello";
        String b = "Hello"; // reuses the same compile-time string literal (interning)
        String c = new String("Hello"); // new always creates a fresh object
        String d = "Hello world!".substring(0, 5); // dynamically extracted at runtime

        // == compares memory addresses (reference identity), not content.
        if (a == b) { System.out.println("a == b"); }   // true: same interned literal
        if (a == c) { System.out.println("a == c"); }   // false: new object
        if (a == d) { System.out.println("a == d"); }   // false: runtime extraction
        // equals() compares character-by-character content.
        if (a.equals(b) && b.equals(c) && c.equals(d)) {
            System.out.println("All four strings are content-equal.");
        }
        System.out.println("String equality demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // PRIMITIVE TYPES
    // -----------------------------------------------------------------------

    /**
     * Showcase the primitive types in Java and some of their basic operations.
     */
    public static void primitiveTypesDemo() {
        System.out.println("Primitive types demo begins:");
        // Other than byte and short, the six primitive types and their literals:
        int a = 100 * 1000 * 1000 * 1000 * 1000; // overflows silently — no warning!
        long b = 100_000_000_000_000L; // L suffix required; underscores are optional sugar
        double c = 123456789.987654321;
        float d = 123.456f;            // f suffix required
        char e = '\u04c1';             // Unicode character literal
        boolean f = true;              // true and false are the only boolean literals

        // Widening conversions happen automatically; narrowing requires an explicit cast.
        long h = a;        // int -> long: OK (widening)
        int i = (int) b;   // long -> int: cast required (narrowing, bits lost)
        double j = a + c;  // int -> double: OK (widening)
        double k = d;       // float -> double: OK (widening)

        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("c = " + df.format(c));
        System.out.println("d = " + df.format(d));
        System.out.println("e = " + e);
        System.out.println("f = " + f);
        System.out.println("i = " + i + " (higher bits irrevocably lost)");
        System.out.println("j = " + j + " (double absorbs any int value)");
        System.out.println("Primitive types demo ends.\n");
    }

    /**
     * Demonstrate the utility methods of {@code Character} for Unicode characters.
     * @param c The character whose properties are examined.
     */
    public static void characterDemo(char c) {
        System.out.println("Character demo begins:");
        System.out.println("Examining character " + c + " (code point " + (int) c + ")");
        System.out.println("Uppercase:  " + Character.toUpperCase(c));
        System.out.println("Lowercase:  " + Character.toLowerCase(c));
        System.out.println("Titlecase:  " + Character.toTitleCase(c));
        System.out.println("isDigit:    " + Character.isDigit(c));
        System.out.println("numericVal: " + Character.getNumericValue(c));
        System.out.println("isLetter:   " + Character.isLetter(c));
        System.out.println("isAlpha:    " + Character.isAlphabetic(c));
        System.out.println("isUpper:    " + Character.isUpperCase(c));
        System.out.println("isLower:    " + Character.isLowerCase(c));
        System.out.println("isSpace:    " + Character.isWhitespace(c));
        System.out.println("isIdeo:     " + Character.isIdeographic(c));
        System.out.println("isMirrored: " + Character.isMirrored(c));
        System.out.println("isJavaIdPart:  " + Character.isJavaIdentifierPart(c));
        System.out.println("isJavaIdStart: " + Character.isJavaIdentifierStart(c));

        // Java 9+: Character.toString(int codePoint) handles supplementary characters
        // beyond the Basic Multilingual Plane (emojis, ancient scripts, etc.).
        int smiley = 0x1F600; // 😀 — a code point above U+FFFF
        System.out.println("Supplementary character: " + Character.toString(smiley)
                + " (U+" + Integer.toHexString(smiley).toUpperCase() + ")");
        System.out.println("Character demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // MATH
    // -----------------------------------------------------------------------

    /**
     * Demonstrate the utility methods of {@code Math} for floating point numbers.
     */
    public static void mathDemo() {
        System.out.println("Math demo begins:");
        double x = Math.cbrt(Math.pow(Math.sqrt(2) * Math.PI, Math.E));
        System.out.println("Before conversion, x = " + x);

        // Round-trip: double -> String -> double
        String xs = String.valueOf(x);
        System.out.println("xs = \"" + xs + "\"");
        x = Double.parseDouble(xs);
        System.out.println("After conversion, x = " + x);

        // A selection of java.lang.Math methods:
        System.out.println("log(x)     = " + Math.log(x));
        System.out.println("log10(x)   = " + Math.log10(x));
        System.out.println("exp(x)     = " + Math.exp(x));
        System.out.println("floor(x)   = " + Math.floor(x));
        System.out.println("ceil(x)    = " + Math.ceil(x));
        System.out.println("round(x)   = " + Math.round(x));
        System.out.println("signum(x)  = " + Math.signum(x));
        System.out.println("nextUp(x)  = " + Math.nextUp(x));
        System.out.println("nextDown(x)= " + Math.nextDown(x));

        // When the result is undefined, IEEE 754 gives us NaN (Not a Number).
        System.out.println("sqrt(-1)   = " + Math.sqrt(-1));
        System.out.println("0.0 / 0.0  = " + (0.0 / 0.0));

        // Math.clamp (Java 21): clamp a value to a range. Replaces the common
        // pattern Math.max(lo, Math.min(hi, value)).
        System.out.println("clamp(150, 0, 100) = " + Math.clamp(150, 0, 100));
        System.out.println("clamp(-5, 0, 100)  = " + Math.clamp(-5, 0, 100));
        System.out.println("clamp(42, 0, 100)  = " + Math.clamp(42, 0, 100));

        System.out.println("Math demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // INTEGER ARITHMETIC
    // -----------------------------------------------------------------------

    /**
     * Showcase integer arithmetic in Java.
     */
    public static void intDemo() {
        System.out.println("Integer demo begins:");
        // These bounds are GUARANTEED in every Java environment (two's complement).
        System.out.println("Largest int:  " + Integer.MAX_VALUE);
        System.out.println("Smallest int: " + Integer.MIN_VALUE);
        System.out.println("Largest long: " + Long.MAX_VALUE);
        System.out.println("Smallest long:" + Long.MIN_VALUE);

        int x = 1729; // Anything interesting about this number, Mr. Ramanujan?
        System.out.println(x + " / 100  = " + x / 100);   // integer division truncates
        System.out.println(x + " % 100  = " + x % 100);   // integer remainder
        int y = -x;
        System.out.println(y + " / 100  = " + y / 100);   // truncates towards zero
        System.out.println(y + " % 100  = " + y % 100);   // sign matches the dividend
        System.out.println(y + " % -100 = " + y % -100);

        // floorDiv and floorMod: truncation towards negative infinity, like Python's //.
        System.out.println("floorDiv(" + y + ", 100)  = " + Math.floorDiv(y, 100));
        System.out.println("floorMod(" + y + ", 100)  = " + Math.floorMod(y, 100));

        // BigInteger: arbitrary-precision integers for when long isn't enough.
        var b1 = BigInteger.TWO;
        var b3 = b1.pow(4423).subtract(BigInteger.ONE); // a known Mersenne prime
        // b3 has 1332 digits (says Wikipedia). Uncomment to see them all:
        // System.out.println("2^4423 - 1 = " + b3);
        // isProbablePrime: no false negatives; false positive probability < 1/2^argument.
        System.out.println("Is 2^4423 - 1 prime? " + b3.isProbablePrime(100));

        // Generate a random probable prime. RandomGenerator is the modern API (Java 17+).
        var rng = RandomGenerator.getDefault();
        System.out.println("A 1000-bit probable prime:");
        System.out.println(BigInteger.probablePrime(1000, new java.util.Random()));
        // Note: BigInteger.probablePrime still requires java.util.Random (legacy API),
        // since it predates RandomGenerator. A small wart in the standard library.

        System.out.println("Integer demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // FLOATING POINT
    // -----------------------------------------------------------------------

    /**
     * Demonstrate the curiosities of floating-point encoding of decimal numbers.
     */
    public static void floatingPointDemo() {
        System.out.println("Floating point demo begins:");

        // IEEE 754 binary encoding cannot represent many familiar base-10 fractions
        // exactly — numbers like 0.1, 0.2, or 0.3 are infinite repeating fractions
        // in binary, just as 1/3 is in decimal.
        System.out.println("0.1 + 0.2 = " + (0.1 + 0.2));
        System.out.println("Formatted: " + df.format(0.1 + 0.2));
        System.out.printf("With printf: %.3f%n", 0.1 + 0.2);

        // Rule of thumb: any program that compares floating-point values for exact
        // equality is broken by design. Also, never use float or double to represent
        // actual money in a real program.

        // IEEE 754 special values that integer types do not have:
        double x = 1.0 / 0.0;  // Infinity (not an error for doubles!)
        double y = -1.0 / 0.0; // -Infinity
        x = x + 5;             // Infinity + 5 = Infinity
        y = y * 10;            // -Infinity * 10 = -Infinity
        double z = x + y;      // Infinity + (-Infinity) = NaN (undefined)
        System.out.println("x = " + x);
        System.out.println("y = " + y);
        System.out.println("z = " + z); // Batmaaan!
        System.out.println("Largest double:  " + Double.MAX_VALUE);
        System.out.println("Smallest double: " + Double.MIN_VALUE); // closest to zero

        // NaN is the only value that is not equal to itself.
        System.out.println("z == z? " + (z == z));                   // false!
        System.out.println("Double.isNaN(z)? " + Double.isNaN(z));   // true

        // BigDecimal: arbitrary-precision decimal arithmetic in base 10.
        // Always construct from a String, never from a double literal.
        var bd1 = new BigDecimal("0.1");
        var bd2 = new BigDecimal("0.2");
        var bd3 = bd1.add(bd2);
        System.out.println("BigDecimal: 0.1 + 0.2 = " + bd3); // exactly 0.3

        // MathContext controls precision and rounding.
        var mc = new MathContext(5, RoundingMode.HALF_DOWN);
        var a = new BigDecimal("1.000000", mc);
        var b = new BigDecimal("3.000000", mc);
        // divide() must be told how to round when the result is not exact.
        a = a.divide(b, RoundingMode.HALF_DOWN);
        System.out.println("1.0 / 3.0 with precision 5 = " + a);

        System.out.println("Floating point demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // PRINTF
    // -----------------------------------------------------------------------

    /**
     * Demonstrate C-style formatted output using printf, and its modern
     * instance-method cousin, String.formatted() (Java 15+).
     */
    public static void printfDemo() {
        int a = 42;
        double b = 123.456789;
        double c = -987.654321;
        String s = "Hello world!";

        System.out.println("Printf demo begins:");
        // Classic printf with format specifiers:
        System.out.printf("a = %d, hex = 0x%x, octal = %o%n", a, a, a);
        System.out.printf("Two decimals:  b = %.2f, c = %.2f%n", b, c);
        System.out.printf("Six decimals:  b = %.6f, c = %.6f%n", b, c);
        System.out.printf("Scientific:    b = %e, c = %e%n", b, c);
        System.out.printf("Left-padded:   '%20s'%n", s);
        System.out.printf("Right-padded:  '%-20s'%n", s);

        // String.formatted() (Java 15+): same format specifiers, but called
        // as an instance method on the format string itself. Reads more naturally,
        // especially when building a String rather than printing directly.
        String report = """
                Summary:
                  integer a  = %d
                  double  b  = %.4f
                  string  s  = "%s"
                """.formatted(a, b, s);
        System.out.println(report);

        System.out.println("Printf demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------

    /**
     * Run all the demonstration methods. The Scanner demo is excluded because
     * it blocks on console input.
     */
    public static void main(String[] args) {
        assignmentDemo();
        stringDemo();
        stringEqualityDemo();
        primitiveTypesDemo();
        characterDemo('$');
        mathDemo();
        intDemo();
        floatingPointDemo();
        printfDemo();
    }
}