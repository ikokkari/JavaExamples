import java.util.EnumMap;
import java.util.EnumSet;

/**
 * Demonstrate Java enumerated types. Originally a "nothing much to see here"
 * demo from circa 2010, now updated for Java 21+ to show how much enums have
 * grown: enhanced switch expressions, enums with abstract methods per constant,
 * enums implementing sealed interfaces, and pattern matching on enum values.
 *
 * Surely enums haven't changed since 2010 or so. (LOL, said the tensor.)
 *
 * @author Ilkka Kokkarinen
 */
public class EnumDemo {

    // -----------------------------------------------------------------------
    // BASIC ENUM WITH METHODS
    // -----------------------------------------------------------------------

    private enum Day {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;

        // Enum types can have fields and methods, just like classes.
        public boolean isWeekday() {
            return this != SATURDAY && this != SUNDAY;
        }
    }

    // -----------------------------------------------------------------------
    // ENUM WITH FIELDS, CONSTRUCTOR, AND ABSTRACT METHOD PER CONSTANT
    // -----------------------------------------------------------------------
    // Each constant can override an abstract method with its own implementation.
    // This replaces the old switch-on-enum pattern with true polymorphism.

    private enum Planet {
        MERCURY(3.303e+23, 2.4397e6),
        VENUS  (4.869e+24, 6.0518e6),
        EARTH  (5.976e+24, 6.37814e6),
        MARS   (6.421e+23, 3.3972e6);

        private final double mass;    // kilograms
        private final double radius;  // metres

        Planet(double mass, double radius) {
            this.mass = mass;
            this.radius = radius;
        }

        // Enums can have concrete methods that use their fields.
        static final double G = 6.67300E-11;

        double surfaceGravity() {
            return G * mass / (radius * radius);
        }

        double surfaceWeight(double otherMass) {
            return otherMass * surfaceGravity();
        }
    }

    // -----------------------------------------------------------------------
    // ENUM IMPLEMENTING AN INTERFACE
    // -----------------------------------------------------------------------
    // Enums can implement interfaces, which makes them usable as strategy
    // objects. Each constant provides its own implementation.

    private interface MathOperation {
        double apply(double a, double b);
    }

    private enum Operator implements MathOperation {
        ADD("+")  { public double apply(double a, double b) { return a + b; } },
        SUB("-")  { public double apply(double a, double b) { return a - b; } },
        MUL("×")  { public double apply(double a, double b) { return a * b; } },
        DIV("÷")  { public double apply(double a, double b) { return a / b; } };

        private final String symbol;
        Operator(String symbol) { this.symbol = symbol; }

        @Override public String toString() { return symbol; }
    }

    // -----------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        // --- Basic enum: values(), ordinal(), name() ---
        System.out.println("=== Basic enum ===");
        for (Day d : Day.values()) {
            System.out.println(d.ordinal() + ": " + d.name()
                    + (d.isWeekday() ? " is" : " is not") + " a weekday");
        }

        // --- Enhanced switch EXPRESSION (Java 14+) ---
        // Arrow syntax: no break needed, no fall-through bugs. The switch is
        // an expression that produces a value, not a statement with side effects.
        System.out.println("\n=== Enhanced switch expression ===");
        for (Day d : Day.values()) {
            String kind = switch (d) {
                case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "a weekday";
                case SATURDAY, SUNDAY -> "the weekend";
            };
            // No default needed: the compiler knows all enum constants are covered.
            System.out.println(d + " is " + kind);
        }

        // --- Switch expression returning a computed value ---
        System.out.println("\n=== Switch with yield ===");
        for (Day d : Day.values()) {
            int hoursOfSleep = switch (d) {
                case FRIDAY, SATURDAY -> {
                    // When an arrow case needs multiple statements, use a block
                    // and 'yield' to return the value.
                    System.out.println("  (" + d + ": sleeping in!)");
                    yield 9;
                }
                case SUNDAY  -> 8;
                default      -> 7;
            };
            System.out.println(d + ": " + hoursOfSleep + " hours of sleep");
        }

        // --- EnumSet: the efficient Set implementation for enums ---
        // Internally a bit vector — O(1) add/remove/contains, zero boxing.
        System.out.println("\n=== EnumSet ===");
        EnumSet<Day> weekdays = EnumSet.range(Day.MONDAY, Day.FRIDAY);
        EnumSet<Day> weekend  = EnumSet.complementOf(weekdays);
        System.out.println("Weekdays: " + weekdays);
        System.out.println("Weekend:  " + weekend);

        // Set operations work naturally:
        EnumSet<Day> midweek = EnumSet.of(Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY);
        System.out.println("Midweek:  " + midweek);
        System.out.println("Weekdays contains midweek? " + weekdays.containsAll(midweek));

        // --- EnumMap: the efficient Map implementation for enums ---
        // Internally a flat array indexed by ordinal — fast and compact.
        System.out.println("\n=== EnumMap ===");
        var schedule = new EnumMap<Day, String>(Day.class);
        schedule.put(Day.MONDAY,    "Algorithms lecture");
        schedule.put(Day.WEDNESDAY, "Lab session");
        schedule.put(Day.FRIDAY,    "Office hours");
        schedule.forEach((day, event) ->
                System.out.println(day + ": " + event));

        // --- Enum with fields and computed methods ---
        System.out.println("\n=== Planet enum ===");
        double earthWeight = 75.0; // kg
        double mass = earthWeight / Planet.EARTH.surfaceGravity();
        for (Planet p : Planet.values()) {
            System.out.printf("Your weight on %s is %6.2f N%n", p, p.surfaceWeight(mass));
        }

        // --- Enum implementing interface (strategy pattern) ---
        System.out.println("\n=== Operator enum as strategy ===");
        double a = 10, b = 3;
        for (Operator op : Operator.values()) {
            System.out.printf("%.0f %s %.0f = %.4f%n", a, op, b, op.apply(a, b));
        }

        // --- Pattern matching with enums (Java 21) ---
        // While enums work in plain switch cases, they also participate in
        // the pattern matching system. Combined with guarded patterns (when),
        // you can express complex dispatch concisely.
        System.out.println("\n=== Pattern matching with guards ===");
        for (Day d : Day.values()) {
            String description = switch (d) {
                case Day dd when dd.ordinal() == 0   -> dd + ": start of the week";
                case Day dd when dd.ordinal() == 6   -> dd + ": end of the week";
                case Day dd when dd.isWeekday()      -> dd + ": midweek grind";
                default -> d + ": ???";
                // The 'when' guard adds a condition beyond just matching the type.
                // dd binds to the matched value for use in both the guard and the body.
            };
            System.out.println(description);
        }
    }
}