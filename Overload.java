/**
 * Overloading vs. overriding — two mechanisms that sound similar but work
 * very differently in Java.
 *
 * <ul>
 *   <li><b>Overloading</b> is resolved at <em>compile time</em>, based on the
 *       <em>declared</em> (static) types of the arguments.</li>
 *   <li><b>Overriding</b> is resolved at <em>runtime</em>, based on the
 *       actual (dynamic) type of the object whose method is being called.</li>
 * </ul>
 *
 * This distinction trips up even experienced Java developers. The example
 * below is designed to make the difference crystal clear.
 *
 * Updated for Java 17+ with sealed classes and detailed resolution comments.
 *
 * @author Ilkka Kokkarinen
 */

public class Overload {

    // -----------------------------------------------------------------------
    // A small sealed inheritance hierarchy to use as parameter types.
    // The "sealed" keyword (Java 17+) restricts which classes can extend
    // Bird, making the hierarchy explicit and closed. The "permits" clause
    // lists all allowed subclasses. Each subclass must be final, sealed,
    // or non-sealed.
    // -----------------------------------------------------------------------

    private static sealed class Bird permits Hawk, Sparrow {}
    private static final class Hawk extends Bird {}
    private static final class Sparrow extends Bird {}

    // -----------------------------------------------------------------------
    // Two classes that demonstrate the interplay of overloading and overriding.
    //
    // Class Parent has two overloaded versions of "get":
    //   #1  get(Bird)   — accepts any Bird
    //   #2  get(Hawk)   — more specific overload for Hawks
    //
    // Class Child extends Parent:
    //   #3  get(Bird)   — overrides Parent's #1 (same signature, @Override)
    //   #4  get(Sparrow) — a NEW overload (different parameter type, no override)
    // -----------------------------------------------------------------------

    private static class Parent {
        public int get(Bird bird)  { return 1; } // #1
        public int get(Hawk hawk)  { return 2; } // #2
    }

    private static class Child extends Parent {
        @Override
        public int get(Bird bird)     { return 3; } // #3 (overrides #1)
        public int get(Sparrow sparrow) { return 4; } // #4 (new overload)
    }

    // -----------------------------------------------------------------------
    // The demonstration itself.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        // Our setup: note the declared (static) types carefully.
        Parent asParent = new Parent();  // declared Parent, actual Parent
        Parent asChild  = new Child();   // declared Parent, actual Child
        Bird bird       = new Hawk();    // declared Bird, actual Hawk
        Sparrow sparrow = new Sparrow(); // declared Sparrow, actual Sparrow

        System.out.println("=== Which method gets called? ===");
        System.out.println();

        // CALL 1: asParent.get(bird)
        // Overload resolution (compile time): bird is declared as Bird → picks #1 get(Bird)
        // Override resolution (runtime): asParent is a Parent → stays at #1
        // Result: 1
        System.out.println("asParent.get(bird)    = " + asParent.get(bird));    // #1 → 1

        // CALL 2: asParent.get(sparrow)
        // Overload resolution: sparrow is declared as Sparrow, which IS-A Bird.
        //   Parent has get(Bird) and get(Hawk). Sparrow matches Bird but not Hawk.
        //   → picks #1 get(Bird)
        // Override resolution: asParent is a Parent → stays at #1
        // Result: 1
        System.out.println("asParent.get(sparrow) = " + asParent.get(sparrow)); // #1 → 1

        // CALL 3: asChild.get(bird)
        // Overload resolution: bird is declared as Bird → picks #1 get(Bird)
        //   (The compiler sees asChild as type Parent, so #4 get(Sparrow) is invisible.)
        // Override resolution: asChild is actually a Child, and Child overrides
        //   get(Bird) with #3 → dispatches to #3
        // Result: 3
        System.out.println("asChild.get(bird)     = " + asChild.get(bird));     // #3 → 3

        // CALL 4: asChild.get(sparrow)
        // Overload resolution: sparrow is Sparrow, which IS-A Bird.
        //   The compiler sees asChild as type Parent → only #1 and #2 are visible.
        //   Sparrow matches Bird but not Hawk → picks #1 get(Bird).
        //   Crucially, #4 get(Sparrow) is NOT considered because the compiler
        //   only sees the Parent type, which doesn't have that overload.
        // Override resolution: asChild is a Child → #1 is overridden by #3
        // Result: 3  (NOT 4 — this is the big surprise!)
        System.out.println("asChild.get(sparrow)  = " + asChild.get(sparrow));  // #3 → 3

        System.out.println();
        System.out.println("=== How to reach #2 and #4 via downcasting ===");
        System.out.println();

        // To call #2: the compiler needs to see the argument as a Hawk.
        // Downcast bird (declared Bird, actual Hawk) to Hawk.
        System.out.println("asParent.get((Hawk) bird)      = "
                + asParent.get((Hawk) bird));               // #2 → 2

        // To call #4: the compiler needs to see the receiver as a Child
        // (so that get(Sparrow) is visible) AND the argument as a Sparrow.
        // sparrow is already declared Sparrow, so we only need to cast the receiver.
        System.out.println("((Child) asChild).get(sparrow) = "
                + ((Child) asChild).get(sparrow));          // #4 → 4

        System.out.println();
        System.out.println("=== Summary ===");
        System.out.println();
        System.out.println("""
                Overloading (which signature?) → resolved at COMPILE time
                  based on the DECLARED types of the receiver and arguments.
                Overriding  (which implementation?) → resolved at RUNTIME
                  based on the ACTUAL type of the receiver object.
                \
                Some languages use "multiple dispatch" where overloading is
                also resolved at runtime based on actual argument types.
                Java does not — it resolves overloading at compile time.""");
    }
}