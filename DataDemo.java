import java.util.Scanner;
import java.text.DecimalFormat; 
import java.math.*;

/**
 * A class demonstrating various aspects of primitive types and Strings in Java.
 * Whole bunch of static methods that demonstrate things, does not do anything
 * else that is useful.
 * @author Ilkka Kokkarinen
 */

public class DataDemo {

    // A utility class used to format decimal numbers for console output purposes.
    private static final DecimalFormat df = new DecimalFormat();
    // A block of code executed when JVM loads the class bytecode. Handy to
    // initialize static members of the class, as if this were the "constructor"
    // for the class as a whole, in a sense.
    static {
        df.setMinimumFractionDigits(3);
        df.setMaximumFractionDigits(3);
    }

    /**
     * Demonstrate the behaviour of the assignment statement in Java.
     */
    public static void assignmentDemo() {
        System.out.println("Assignment demo begins:");
        int a = 3;
        int b = a + 2;
        System.out.println("a is now " + a + " and b is now " + b); // 3 5
        a = 5; // Note that b doesn't change, Java is not a spreadsheet.
        System.out.println("a is now " + a + " and b is now " + b); // 5 5
        b = b + 2; // Same variable in both LHS and RHS of assignment is fine.
        System.out.println("a is now " + a + " and b is now " + b); // 5 7
        System.out.println("Assignment demo ends.\n");
    }

    /**
     * Demonstrate the String class and some of its operations for text.
     */
    public static void stringDemo() {
        System.out.println("String demo begins:");
        String s = "Hello, world!"; // the canonical example text
        System.out.println("s equals \"" + s + "\""); // escape sequences
        System.out.println("Converted to lowercase, it is \"" + s.toLowerCase() + "\"");
        System.out.println("Character in location 5 is " + s.charAt(5));
        System.out.println("Substring from location 5 to end is \"" + s.substring(5) + "\"");
        System.out.println("Substring from location 3 to 7 is \"" + s.substring(3, 8) + "\"");
        // String is immutable: all operations that modify it create a new result string
        String s2 = s.replaceAll("ello", "owdy");
        System.out.println("s equals \"" + s + "\"");
        System.out.println("s2 equals \"" + s2 + "\"");
        System.out.println("String demo ends.\n");
    }

    /** 
     * Demonstrate the difference between memory address equality with {@code ==} 
     * versus the object content equality with {@code equals}.
     */
    public static void stringEqualityDemo() {
        System.out.println("String equality demo begins:");
        String a = "Hello";
        String b = "Hello"; // reuse the same compile time string literal
        String c = new String("Hello"); // new always creates a new object
        String d = "Hello world!".substring(0, 5); // dynamic extraction
        if(a == b) { System.out.println("a == b"); } // true
        if(a == c) { System.out.println("a == c"); } // false
        if(a == d) { System.out.println("a == d"); } // false
        if(a.equals(b) && b.equals(c) && c.equals(d)) { // true
            System.out.println("All four strings are content equal.");
        }
        System.out.println("String equality demo ends.\n");
    }

    /** 
     * Demonstrate how to read text input from the console using the {@code Scanner}
     * utility class. Otherwise, console text input would get quite hairy.
     */
    public static void scannerDemo() {
        System.out.println("Scanner demo begins:");
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter your name: ");
        String name = sc.nextLine();
        System.out.println("Hello there, " + name + ".");
        System.out.println("Your name has " + name.replaceAll(" ", "").length() 
        + " non-whitespace characters.");
        System.out.print("How old are you? ");
        int age = sc.nextInt();
        System.out.println("So you are " + age + " years old.");
        System.out.println("Scanner demo ends.\n");
    }        

    /** 
     * Showcase the primitive types in Java and some of their basic operations.
     */
    public static void primitiveTypesDemo() {
        System.out.println("Primitive types demo begins:");
        // Other than byte and short, six primitive data types and their literals.
        int a = 100 * 1000 * 1000 * 1000 * 1000; // overflow without warning
        long b = 100_000_000_000_000L; // L at the end is necessary, underscores optional
        double c = 123456789.987654321;
        float d = 123.456f; // ditto for f
        char e = '\u04c1'; // e.g. http://en.wikipedia.org/wiki/List_of_Unicode_characters
        boolean f = true; // truth values true and false are the boolean literals

        long h = a; // ok
        int i = (int)b; // cast is necessary
        double j = a + c; // also ok, don't need a cast
        double k = d; // ok

        System.out.println("a equals " + a);
        System.out.println("b equals " + b);
        System.out.println("c equals " + df.format(c)); // convert double to decent String
        System.out.println("d equals " + df.format(d)); // representation using DecimalFormat
        System.out.println("e equals " + e);
        System.out.println("f equals " + f);
        System.out.println("i equals " + i); // higher bits were irrevocably lost
        System.out.println("Primitive types demo ends.\n");
    }

    /**
     * Demonstrate the utility methods of {@code Character} for Unicode characters.
     * @param c The character whose properties are examined.
     */
    public static void characterDemo(char c) {
        System.out.println("Character demo begins:");
        System.out.println("Examining character " + c  + " with Unicode code point " + (int) c);
        System.out.println("Converted to uppercase, it is " + Character.toUpperCase(c));
        System.out.println("Converted to lowercase, it is " + Character.toLowerCase(c));
        System.out.println("Converted to titlecase, it is " + Character.toTitleCase(c));
        System.out.println("Is it a digit? " + Character.isDigit(c));
        System.out.println("Its numeric value is " + Character.getNumericValue(c));
        System.out.println("Is it a letter? " + Character.isLetter(c));
        System.out.println("Is it alphabetic? " + Character.isAlphabetic(c));
        System.out.println("Is it uppercase? " + Character.isUpperCase(c));
        System.out.println("Is it lowercase? " + Character.isLowerCase(c));
        System.out.println("Is it a whitespace character? " + Character.isWhitespace(c));
        System.out.println("Is it ideographic? " + Character.isIdeographic(c));
        System.out.println("Is it mirrored? " + Character.isMirrored(c));
        System.out.println("Can it be used in Java identifier? " + Character.isJavaIdentifierPart(c));
        System.out.println("Can it start a Java identifier? " + Character.isJavaIdentifierStart(c));
        System.out.println("Character demo ends.\n");
    }
    
    /**
     * Demonstrate the utility methods of {@code Math} for floating point numbers.
     */
    public static void mathDemo() {
        System.out.println("Math demo begins:");
        double x = Math.cbrt(Math.pow(Math.sqrt(2) * Math.PI, Math.E));
        System.out.println("Before conversion, x equals " + x);
        // Conversion from double to String
        String xs = String.valueOf(x);
        System.out.println("xs equals \"" + xs + "\"");
        // Conversion from String to double
        x = Double.parseDouble(xs);
        System.out.println("After conversion, x equals " + x);
        // Whole bunch of mathematical methods in java.lang.Math
        System.out.println("log(x) equals " + Math.log(x));
        System.out.println("log10(x) equals " + Math.log10(x));
        System.out.println("exp(x) equals " + Math.exp(x));
        System.out.println("floor(x) equals " + Math.floor(x));
        System.out.println("ceil(x) equals " + Math.ceil(x));
        System.out.println("round(x) equals " + Math.round(x));
        System.out.println("signum(x) equals " + Math.signum(x));
        System.out.println("nextUp(x) equals " + Math.nextUp(x));
        System.out.println("nextDown(x) equals " + Math.nextDown(x));
        // Sometimes the result of the operation is undefined, and is NaN.
        System.out.println("sqrt(-1) equals " + Math.sqrt(-1));
        System.out.println("0.0 / 0.0 equals " + (0.0 / 0.0));
        System.out.println("Math demo ends.\n");
    }

    /**
     * Showcase the integer arithmetic in Java.
     */
    public static void intDemo() {
        System.out.println("Integer demo begins:");
        // Everything below before BigIntegers is GUARANTEED in every Java environment.
        System.out.println("Largest positive int equals " + Integer.MAX_VALUE);
        System.out.println("Largest positive long equals " + Long.MAX_VALUE);
        System.out.println("Smallest negative int equals " + Integer.MIN_VALUE);
        System.out.println("Smallest negative long equals " + Long.MIN_VALUE);
        int x = 1729; // Anything interesting about this number, Mr. Ramanujan?
        System.out.println(x + " / 100 equals " + x / 100); // integer division truncates
        System.out.println(x + " % 100 equals " + x % 100); // integer remainder
        int y = -x;
        System.out.println(y + " / 100 equals " + y / 100); // truncate is done towards zero
        System.out.println(y + " % 100 equals " + y % 100); // sign of remainder equals sign of first operand
        System.out.println(y + " % -100 equals " + y % -100);
        System.out.println("floorDiv(" + y + ", 100) equals " + Math.floorDiv(y, 100)); // towards -Inf
        System.out.println("floorMod(" + y + ", 100) equals " + Math.floorMod(y, 100));
        
        // If you want to play with numbers bigger than fit into even a long, use BigInteger.
        BigInteger b1 = new BigInteger("2");
        BigInteger b3 = b1.pow(4423).subtract(BigInteger.ONE); // one of the known Mersenne primes
        // System.out.println("2^4423 - 1 equals " + b3); // 1332 digits, says Wikipedia
        // No false negatives, probability of false positive is less than 1 / (2^argument).
        System.out.println("Is 2^4432 - 1 a prime number? " + b3.isProbablePrime(100));
        // You could represent unimaginable integers with millions of digits easily, and perform
        // basic arithmetic operations on these monsters with the fast algorithms of BigInteger.
        // But even 300-digit numbers are far beyond human comprehension or need, except in
        // cryptography and some other special fields of mathematics.
        System.out.println("Here is a large probable prime number made of 1000 bits:");
        System.out.println(BigInteger.probablePrime(1000, new java.util.Random()));
        System.out.println("Integer demo ends.\n");
    }

    /**
     * Demonstrate the curiosities of the floating point encoding of decimal numbers.
     */
    public static void floatingPointDemo() {
        System.out.println("Floating point demo begins:");
        // Floating point encoding cannot represent exactly many familiar and seemingly
        // simple numbers (that is, simple in base 10) such as 0.1, 0.2 or 0.3.
        System.out.println("0.1 + 0.2 equals " + (0.1 + 0.2));
        System.out.println("Formatted, 0.1 + 0.2 equals " + df.format(0.1 + 0.2)); // DecimalFormat
        System.out.printf("With printf, 0.1 + 0.2 equals %.3f\n", 0.1 + 0.2); // old timey C way
        // As a rule, any program that compares floating point values for equality is
        // broken by design, and cannot be fixed without complete redesign. Also, never
        // use float or double to represent actual money in a real program.
        
        // Floating point encoding allows special numbers that integer types do not.
        double x = 1.0 / 0.0; // not an error with doubles (but would be with ints)
        double y = -1.0 / 0.0;
        x = x + 5; // Infinity + 5 equals Infinity
        y = y * 10; // ditto
        double z = x + y; // Infinity - Infinity is undefined, thus NaN
        System.out.println("x equals " + x);
        System.out.println("y equals " + y);
        System.out.println("z equals " + z); // Batmaaan!
        System.out.println("Largest positive double equals " + Double.MAX_VALUE); // huge
        System.out.println("Smallest positive double equals " + Double.MIN_VALUE); // close to zero
        
        // Analogous to BigInteger when the range of primitive types runs out, the
        // class BigDecimal allows arbitrary precision decimal arithmetic in base 10. 
        BigDecimal bd1 = new BigDecimal("0.1"); // always give argument as a String
        BigDecimal bd2 = new BigDecimal("0.2");
        BigDecimal bd3 = bd1.add(bd2);
        System.out.println("With BigDecimal, 0.1 + 0.2 equals exactly " + bd3 + ".");
        
        // Try the effect of changing 5 to something else in the MathContext object.
        MathContext mc = new MathContext(5, RoundingMode.HALF_DOWN);
        BigDecimal a = new BigDecimal("1.000000", mc);
        BigDecimal b = new BigDecimal("3.000000", mc);
        // The divide method must be told how to round the result that is not exact.
        a = a.divide(b, RoundingMode.HALF_DOWN);
        System.out.println("Using precision level of 5, 1.0 / 3.0 equals " + a);
        
        System.out.println("Floating point demo ends.\n");
    }

    /**
     * Demonstrate the C-style formatted output using the printf method.
     */
    public static void printfDemo() {
        int a = 42; 
        double b = 123.456789;
        double c = -987.654321;
        String s = "Hello world!";
        System.out.print("C-style printf demo begins:\n");
        System.out.printf("Now a equals %d, which is 0x%x in hexadecimal and %o in octal.\n", a, a, a);
        System.out.printf("With two decimals, b equals %.2f and c equals %.2f.\n", b, c);
        System.out.printf("With six decimals, b equals %.6f and c equals %.6f.\n", b, c);
        System.out.printf("Using scientific notation, b equals %e and c equals %e.\n", b, c);
        System.out.printf("String s equals '%s'\n", s);
        System.out.printf("Padding from left, that is '%20s'\n", s);
        System.out.printf("Padding from right, that is '%-20s'\n", s);
        System.out.print("C-style printf demo ends.\n");
    }
    
    /**
     * Run all the methods of this class, except the one that uses {@code Scanner}.
     */
    public static void main(String[] args) {
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