import java.util.Arrays;

/**
 * A class whose instances represent physical lengths. The same length can be
 * expressed in centimeters, inches, or other units — it is the same physical
 * quantity regardless of the units we use to talk about it.
 * <p>
 * This is the first proper class in the course. It demonstrates:
 * <ul>
 *   <li>Encapsulation — the internal representation is hidden behind getters/setters</li>
 *   <li>The Uniform Access Principle — callers cannot tell whether a "property" is
 *       stored directly or computed on the fly (Bertrand Meyer)</li>
 *   <li>Named constants vs. magic numbers</li>
 *   <li>Constructor delegation and factory methods</li>
 *   <li>{@code Comparable} for natural ordering</li>
 *   <li>Proper {@code equals}, {@code hashCode}, and {@code toString}</li>
 * </ul>
 *
 * <blockquote>"All services offered by a module should be available through a
 * uniform notation, which does not betray whether they are implemented through
 * storage or through computation." — Bertrand Meyer</blockquote>
 *
 * @author Ilkka Kokkarinen
 * @see <a href="https://martinfowler.com/bliki/UniformAccessPrinciple.html">
 *      Uniform Access Principle (Martin Fowler)</a>
 */
public class Length implements Comparable<Length> {

    // -----------------------------------------------------------------------
    // Internal representation — we store everything in centimeters.
    // We could have chosen inches (or meters, or furlongs); the rest of
    // the class would simply become a mirror image.
    // -----------------------------------------------------------------------

    private double cm;

    // -----------------------------------------------------------------------
    // Conversion factors — named constants instead of magic numbers.
    // Declaring a field "static final" makes it a constant shared by all
    // instances, whose value can never change after initialization.
    // -----------------------------------------------------------------------

    private static final double CM_PER_INCH  = 2.54;
    private static final double CM_PER_FOOT  = 30.48;
    private static final double CM_PER_METER = 100.0;

    // -----------------------------------------------------------------------
    // Constructor.
    // -----------------------------------------------------------------------

    /**
     * Create a Length from a value in centimeters.
     *
     * @param cm the length in centimeters
     */
    public Length(double cm) {
        setCentimeters(cm);
    }

    // -----------------------------------------------------------------------
    // Factory methods — named constructors for other units.
    //
    // Since Java constructors must be named after the class, we cannot have
    // both Length(double cm) and Length(double inches). Factory methods solve
    // this by giving each creation path a descriptive name.
    // -----------------------------------------------------------------------

    /** Create a Length from a value in inches. */
    public static Length fromInches(double inches) {
        return new Length(inches * CM_PER_INCH);
    }

    /** Create a Length from a value in feet. */
    public static Length fromFeet(double feet) {
        return new Length(feet * CM_PER_FOOT);
    }

    /** Create a Length from a value in meters. */
    public static Length fromMeters(double meters) {
        return new Length(meters * CM_PER_METER);
    }

    // -----------------------------------------------------------------------
    // Getters and setters — the Uniform Access Principle in action.
    //
    // From the caller's perspective, centimeters and inches look like
    // interchangeable "properties". The caller cannot tell that cm is
    // stored directly while inches are computed on the fly.
    // -----------------------------------------------------------------------

    /** Get the length in centimeters. */
    public double getCentimeters() { return cm; }

    /** Set the length in centimeters. */
    public void setCentimeters(double cm) { this.cm = cm; }

    /** Get the length in inches. */
    public double getInches() { return cm / CM_PER_INCH; }

    /** Set the length by specifying a value in inches. */
    public void setInches(double inches) { this.cm = inches * CM_PER_INCH; }

    /** Get the length in feet. */
    public double getFeet() { return cm / CM_PER_FOOT; }

    /** Set the length by specifying a value in feet. */
    public void setFeet(double feet) { this.cm = feet * CM_PER_FOOT; }

    /** Get the length in meters. */
    public double getMeters() { return cm / CM_PER_METER; }

    /** Set the length by specifying a value in meters. */
    public void setMeters(double meters) { this.cm = meters * CM_PER_METER; }

    // As an exercise, try adding support for more exotic units such as furlongs
    // or light-seconds. The pattern is always the same: define the conversion
    // factor, then write a getter, a setter, and a factory method.

    // -----------------------------------------------------------------------
    // Comparable — gives Length a natural ordering by physical size.
    // This also enables sorting with Arrays.sort or Collections.sort.
    // -----------------------------------------------------------------------

    @Override
    public int compareTo(Length other) {
        return Double.compare(this.cm, other.cm);
    }

    // -----------------------------------------------------------------------
    // equals and hashCode — two Length objects are equal if they represent
    // the same physical length.
    //
    // The general contract: if a.equals(b), then a.hashCode() == b.hashCode().
    // -----------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
        // The "instanceof pattern variable" (Java 16+) combines the type
        // check and the cast into a single expression.
        return obj instanceof Length other
                && Double.compare(this.cm, other.cm) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(cm);
    }

    // -----------------------------------------------------------------------
    // toString — the human-readable representation of this object.
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return "Length of %.2f cm (%.2f in)".formatted(cm, getInches());
    }

    // -----------------------------------------------------------------------
    // Main method — demonstrate the class.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        // --- Basic construction and access ---
        System.out.println("--- Construction and access ---");
        var a = new Length(20);
        var b = new Length(20);
        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.printf("a in cm: %.4f%n", a.getCentimeters());
        System.out.printf("a in inches: %.4f%n", a.getInches());

        // --- Mutation via setters ---
        System.out.println("\n--- After a.setInches(20) ---");
        a.setInches(20);
        System.out.printf("a in cm: %.4f%n", a.getCentimeters());
        System.out.printf("a in inches: %.4f%n", a.getInches());
        // b is a separate object — unchanged.
        System.out.printf("b in cm: %.4f (unchanged)%n", b.getCentimeters());

        // --- Factory methods ---
        System.out.println("\n--- Factory methods ---");
        var oneFoot = Length.fromFeet(1);
        var oneMeter = Length.fromMeters(1);
        System.out.println("1 foot  = " + oneFoot);
        System.out.println("1 meter = " + oneMeter);

        // --- equals and Comparable ---
        System.out.println("\n--- Equality and comparison ---");
        var x = new Length(254);
        var y = Length.fromInches(100);
        System.out.printf("254 cm == 100 inches? %s%n", x.equals(y));
        System.out.printf("1 foot < 1 meter? %s%n",
                oneFoot.compareTo(oneMeter) < 0);

        // --- Sorting ---
        System.out.println("\n--- Sorting an array of lengths ---");
        Length[] lengths = {
                Length.fromInches(12),
                new Length(50),
                Length.fromMeters(0.3),
                Length.fromFeet(1.5),
                new Length(10)
        };
        System.out.println("Before sort:");
        for (var len : lengths) { System.out.println("  " + len); }
        Arrays.sort(lengths); // Works because Length implements Comparable.
        System.out.println("After sort:");
        for (var len : lengths) { System.out.println("  " + len); }
    }
}