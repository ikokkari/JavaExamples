/**
 * Inheritance and polymorphism in Java 21+.
 * <p>
 * Instead of abstract classes {@code A} and {@code B} with meaningless
 * fields, this demo uses a small domain (shapes) where inheritance
 * actually motivates the design: every shape has an area and a
 * perimeter, but each kind computes them differently.
 * <p>
 * Along the way we demonstrate:
 * <ol>
 *   <li>Sealed class hierarchies ({@code sealed ... permits})</li>
 *   <li>Records as concise value classes</li>
 *   <li>Abstract methods and concrete overrides</li>
 *   <li>Constructor chaining ({@code super(...)})</li>
 *   <li>The {@code @Override} contract</li>
 *   <li>Polymorphic dispatch (a variable of type {@code Shape}
 *       calling the right subclass method at runtime)</li>
 *   <li>Pattern matching in {@code switch} and {@code instanceof}</li>
 *   <li>Field hiding vs. method overriding (the classic trap)</li>
 * </ol>
 *
 * @author Ilkka Kokkarinen
 */
public class InheritanceDemo {

    // -----------------------------------------------------------------------
    // 1. SEALED HIERARCHY — the compiler knows every possible subtype.
    //    "sealed" means: only the classes listed after "permits" may
    //    extend Shape. This gives us exhaustive pattern matching in
    //    switch expressions and prevents uncontrolled subclassing.
    // -----------------------------------------------------------------------

    sealed interface Shape permits Circle, Rectangle, Triangle {
        double area();
        double perimeter();
        String describe();   // every shape can describe itself
    }

    // -----------------------------------------------------------------------
    // 2. RECORDS — ideal for simple value-carrying leaves of a hierarchy.
    //    A record gives you the constructor, getters, equals, hashCode,
    //    and toString for free. You just implement the interface methods.
    // -----------------------------------------------------------------------

    record Circle(double radius) implements Shape {

        // Compact constructor for validation — no parameter list needed.
        Circle {
            if (radius < 0) {
                throw new IllegalArgumentException(
                        "Radius must be non-negative: " + radius);
            }
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }

        @Override
        public double perimeter() {
            return 2 * Math.PI * radius;
        }

        @Override
        public String describe() {
            return "Circle with radius %.2f".formatted(radius);
        }
    }

    // -----------------------------------------------------------------------
    // 3. ABSTRACT CLASS — when you want shared state or default behaviour
    //    that records can't provide (records are implicitly final and
    //    can't extend a class). Rectangle is a regular class so that
    //    Square can extend it — illustrating the classic inheritance chain.
    // -----------------------------------------------------------------------

    static non-sealed class Rectangle implements Shape {
        // Using protected so subclasses can see these directly.
        protected final double width;
        protected final double height;

        public Rectangle(double width, double height) {
            if (width < 0 || height < 0) {
                throw new IllegalArgumentException(
                        "Dimensions must be non-negative: %f x %f"
                                .formatted(width, height));
            }
            this.width = width;
            this.height = height;
            // Note: if a superclass constructor existed, Java would
            // implicitly call super() here — or you'd write it
            // explicitly as the first statement.
            System.out.println("  (Rectangle constructor: %.1f x %.1f)"
                    .formatted(width, height));
        }

        @Override
        public double area() {
            return width * height;
        }

        @Override
        public double perimeter() {
            return 2 * (width + height);
        }

        @Override
        public String describe() {
            return "Rectangle %.2f x %.2f".formatted(width, height);
        }

        // This method exists only in Rectangle (not in Shape).
        public double diagonal() {
            return Math.hypot(width, height);
        }
    }

    // -----------------------------------------------------------------------
    // 4. SUBCLASS WITH CONSTRUCTOR CHAINING — Square is-a Rectangle
    //    whose width equals its height. The super(...) call delegates
    //    to Rectangle's constructor.
    // -----------------------------------------------------------------------

    static final class Square extends Rectangle {

        public Square(double side) {
            super(side, side); // constructor chaining
            System.out.println("  (Square constructor: side %.1f)"
                    .formatted(side));
        }

        // Override describe() to give a more specific message.
        // area(), perimeter(), and diagonal() are inherited and correct.
        @Override
        public String describe() {
            return "Square with side %.2f".formatted(width);
        }
    }

    // -----------------------------------------------------------------------
    // 5. RECORD IMPLEMENTING THE SEALED INTERFACE — a triangle defined
    //    by its three side lengths (SSS). Shows that you can mix records
    //    and classes in the same sealed hierarchy.
    // -----------------------------------------------------------------------

    record Triangle(double a, double b, double c) implements Shape {

        Triangle {
            if (a <= 0 || b <= 0 || c <= 0) {
                throw new IllegalArgumentException(
                        "Sides must be positive: %f, %f, %f"
                                .formatted(a, b, c));
            }
            if (a + b <= c || a + c <= b || b + c <= a) {
                throw new IllegalArgumentException(
                        "Triangle inequality violated: %f, %f, %f"
                                .formatted(a, b, c));
            }
        }

        @Override
        public double area() {
            // Heron's formula.
            double s = (a + b + c) / 2;
            return Math.sqrt(s * (s - a) * (s - b) * (s - c));
        }

        @Override
        public double perimeter() {
            return a + b + c;
        }

        @Override
        public String describe() {
            return "Triangle with sides %.2f, %.2f, %.2f"
                    .formatted(a, b, c);
        }
    }

    // -----------------------------------------------------------------------
    // 6. FIELD HIDING — the classic inheritance trap.
    //    This is the one thing from the original demo worth preserving:
    //    if a subclass declares a field with the same name as one in the
    //    superclass, the subclass field HIDES (not overrides) the inherited
    //    one. Methods, by contrast, are overridden via virtual dispatch.
    // -----------------------------------------------------------------------

    static class Parent {
        public String name = "Parent";
        public String getName() { return name; }
    }

    static class Child extends Parent {
        public String name = "Child";  // hides Parent.name — don't do this!
        @Override
        public String getName() { return name; } // overrides — this is fine
    }

    // -----------------------------------------------------------------------
    // Main demo
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        // --- Polymorphism: Shape variables holding different subtypes ---

        System.out.println("=== Creating shapes ===\n");
        Shape[] shapes = {
                new Circle(5),
                new Rectangle(4, 7),
                new Square(3),
                new Triangle(3, 4, 5)
        };

        // Polymorphic dispatch: the right area()/perimeter()/describe()
        // is called based on the *runtime* type, not the declared type.
        System.out.println("\n=== Polymorphic dispatch ===\n");
        for (var shape : shapes) {
            System.out.printf("%-40s area = %8.3f   perimeter = %8.3f%n",
                    shape.describe(), shape.area(), shape.perimeter());
        }

        // --- Pattern matching with switch (Java 21+) ---
        // Because Shape is sealed, the compiler knows this switch is
        // exhaustive — no default branch is needed.

        System.out.println("\n=== Pattern matching switch ===\n");
        for (var shape : shapes) {
            String info = switch (shape) {
                case Circle c ->
                        "Circle — diameter is %.2f".formatted(2 * c.radius());
                case Rectangle r ->
                        "Rectangle — diagonal is %.2f".formatted(r.diagonal());
                case Triangle t ->
                        "Triangle — is right-angled? %s".formatted(isRightTriangle(t));
            };
            System.out.println(info);
        }

        // --- Pattern matching with instanceof ---

        System.out.println("\n=== instanceof with pattern variable ===\n");
        Shape mystery = shapes[2]; // it's a Square, but declared as Shape
        if (mystery instanceof Rectangle r) {
            // The compiler lets us use r (a Rectangle) here without a cast.
            System.out.println("mystery is a Rectangle with diagonal " + r.diagonal());
        }
        if (mystery instanceof Square) {
            // We could also check for the more specific type.
            System.out.println("...and more specifically, it's a Square!");
        }

        // --- Constructor chaining demonstration ---

        System.out.println("\n=== Constructor chaining ===\n");
        System.out.println("Creating a Square triggers Rectangle's constructor first:");
        var sq = new Square(10);
        System.out.println("Result: " + sq.describe());

        // --- Field hiding trap ---

        System.out.println("\n=== Field hiding vs. method overriding ===\n");
        Parent p = new Child();
        System.out.println("p.name      = " + p.name);       // "Parent" — field access uses declared type
        System.out.println("p.getName() = " + p.getName());   // "Child"  — method call uses runtime type
        System.out.println();
        System.out.println("This is why fields should be private, and accessed");
        System.out.println("through methods: methods are polymorphic, fields are not.");
    }

    // A helper to check if a triangle is right-angled (Pythagorean theorem).
    private static boolean isRightTriangle(Triangle t) {
        double[] sides = {t.a(), t.b(), t.c()};
        java.util.Arrays.sort(sides);
        double lhs = sides[0] * sides[0] + sides[1] * sides[1];
        double rhs = sides[2] * sides[2];
        return Math.abs(lhs - rhs) < 1e-9;
    }
}
