/**
 * Demonstration of all four kinds of nested classes in Java, each with a
 * clear purpose and use case.
 * <p>
 * Java has four flavors of nested class, ranging from most to least coupled
 * to the outer class:
 * <ol>
 *   <li><b>Inner class</b> (non-static member class) — each instance is
 *       permanently attached to an instance of the outer class, whose
 *       private fields it can freely access.</li>
 *   <li><b>Local class</b> — an inner class defined inside a method, visible
 *       only within that method. Can also access effectively final local
 *       variables of the enclosing method.</li>
 *   <li><b>Anonymous class</b> — a local class without a name, defined and
 *       instantiated in a single expression. Largely superseded by lambdas
 *       for functional interfaces, but still useful when you need to
 *       implement multiple methods or extend a class.</li>
 *   <li><b>Static nested class</b> — a class nested inside another for
 *       organizational purposes only. It has no reference to an outer
 *       instance and can only access the outer class's static members.</li>
 * </ol>
 * <p>
 * The key teaching point: nested classes can access the <em>private</em>
 * members of their enclosing class. As the saying goes, the murderer is
 * already inside the house.
 *
 * Updated for Java 21+ with all four nested class types demonstrated.
 *
 * @author Ilkka Kokkarinen
 */
public class NestedDemo {

    // Private fields — inaccessible from outside, but nested classes can
    // reach them freely. This is the whole point of nesting.
    private static int instanceCount = 0;
    private final int value;

    // -----------------------------------------------------------------------
    // 1. INNER CLASS (non-static member class)
    //
    // Each Inspector is attached to a specific NestedDemo instance and can
    // access its private fields via "NestedDemo.this".
    // -----------------------------------------------------------------------

    private class Inspector {
        private final int serialNumber;

        Inspector() {
            this.serialNumber = instanceCount++; // Accesses outer static field.
        }

        /**
         * Demonstrate the three-level name resolution for the name "value":
         * parameter → inner field → outer field.
         */
        void report(int value) {
            System.out.println("  Parameter 'value'                    = " + value);
            System.out.println("  Inspector's serialNumber (this.x)    = " + this.serialNumber);
            // NestedDemo.this refers to the enclosing NestedDemo instance.
            System.out.println("  Outer object's value (Outer.this.x)  = " + NestedDemo.this.value);
        }
    }

    // -----------------------------------------------------------------------
    // 2. STATIC NESTED CLASS
    //
    // No reference to an outer instance — can only access static members.
    // This is essentially a top-level class that is nested for organizational
    // purposes (it logically "belongs" to the outer class).
    // -----------------------------------------------------------------------

    private static class Statistics {
        /** Return how many NestedDemo instances have been created so far. */
        static String summary() {
            // Can access the outer class's private static field, but NOT
            // any instance field — there is no outer "this" to refer to.
            return "Total Inspector instances created: " + instanceCount;
        }
    }

    // -----------------------------------------------------------------------
    // Constructor — demonstrates local and anonymous classes.
    // -----------------------------------------------------------------------

    public NestedDemo(int value) {
        this.value = value;
        System.out.println("Creating a new NestedDemo with value = " + value);

        // ---------------------------------------------------------------
        // 3. LOCAL CLASS
        //
        // Defined inside a method, visible only here. Can access:
        //   - the enclosing object's private fields (like an inner class)
        //   - effectively final local variables of this method
        // ---------------------------------------------------------------

        // This local variable is "effectively final" — the compiler can see
        // it is never reassigned after initialization, so local/anonymous
        // classes are allowed to capture it. Adding the "final" keyword
        // explicitly is optional but makes the intent clearer.
        int cube = value * value * value;

        class LocalReporter {
            void report() {
                // Accesses the effectively final local variable "cube".
                System.out.println("  [Local class] cube of value = " + cube);
                // Accesses the outer object's private field.
                System.out.println("  [Local class] outer value   = " + NestedDemo.this.value);
            }
        }

        // ---------------------------------------------------------------
        // 4. ANONYMOUS CLASS
        //
        // A local class without a name — defined and instantiated in one
        // expression. Here we implement Runnable (a functional interface),
        // but we use an anonymous class instead of a lambda to show the
        // mechanism. Lambdas are preferred when the interface has only one
        // abstract method.
        // ---------------------------------------------------------------

        Runnable anonymousDemo = new Runnable() {
            @Override
            public void run() {
                System.out.println("  [Anonymous class] I can also see cube = " + cube);
                System.out.println("  [Anonymous class] and outer value     = "
                        + NestedDemo.this.value);
            }
        };

        // The same thing as a lambda — much more concise, but only works
        // for functional interfaces (interfaces with one abstract method).
        Runnable lambdaDemo = () -> {
            System.out.println("  [Lambda] cube = " + cube + ", outer value = "
                    + NestedDemo.this.value);
        };

        // --- Exercise each nested class ---

        System.out.println("\n  --- Inner class (Inspector) ---");
        var inspector1 = new Inspector();
        var inspector2 = new Inspector();
        inspector1.report(17);
        inspector2.report(99);

        System.out.println("\n  --- Local class ---");
        new LocalReporter().report();

        System.out.println("\n  --- Anonymous class ---");
        anonymousDemo.run();

        System.out.println("\n  --- Lambda (for comparison) ---");
        lambdaDemo.run();

        System.out.println("\n  --- Static nested class ---");
        System.out.println("  " + Statistics.summary());

        System.out.println("\nFinished creating NestedDemo with value = " + value + ".\n");
    }

    // -----------------------------------------------------------------------
    // Main — create a couple of instances to see everything in action.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        new NestedDemo(42);
        new NestedDemo(99);

        // Statistics is a static nested class — no NestedDemo instance needed.
        System.out.println("=== Final " + Statistics.summary() + " ===");
    }
}