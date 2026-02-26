import java.io.Closeable;
import java.io.IOException;

/**
 * Demonstrate Java exception handling: try/catch/finally, checked vs unchecked,
 * the call stack, and some delightful brain teasers about control flow.
 * Updated for Java 21+ with multi-catch, try-with-resources, suppressed
 * exceptions, and a custom sealed exception hierarchy.
 * @author Ilkka Kokkarinen
 */
public class ExceptionDemo {

    // -----------------------------------------------------------------------
    // BASIC EXCEPTION PROPAGATION
    // -----------------------------------------------------------------------

    // This method always fails by throwing a checked IOException.
    private static Object failOne() throws IOException {
        System.out.println("Entered method failOne.");
        if (true) { // fool the compiler into thinking the last line is reachable
            throw new IOException("testing");
        }
        System.out.println("Execution does not get here.");
        return new IOException("testing"); // dead code, but compiler doesn't know
    }

    // Calls failOne but does not handle its exception. Therefore this method
    // must declare that it too may throw IOException — the buck keeps passing.
    private static void failTwo() throws IOException {
        try {
            System.out.println("Calling method failOne from failTwo.");
            failOne(); // throws
            System.out.println("Execution does not get here.");
        } finally {
            // A finally block executes no matter what: normal return, exception,
            // or even an explicit return statement (see returnDemo below).
            System.out.println("But the execution does get here.");
        }
    }

    // The demo method catches and handles the exception that was thrown from
    // two levels below it in the call stack.
    public static void demo() {
        try {
            failTwo(); // fails by calling something that fails
        } catch (IOException e) {
            System.out.println("Caught an exception: " + e);
            System.out.println("Printing the stack trace:");
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement frame : trace) {
                // Each frame records the class, method, and source line number
                // at the point of the throw.
                System.out.println("  " + frame.getClassName()
                        + "." + frame.getMethodName()
                        + " (line " + frame.getLineNumber() + ")");
            }
        } finally {
            System.out.println("And we are finally done!");
        }
    }

    // -----------------------------------------------------------------------
    // BRAIN TEASERS: finally vs return/throw/break/continue
    // -----------------------------------------------------------------------
    // The JLS guarantees that finally always executes. When finally conflicts
    // with other control flow, finally wins. These methods are intentionally
    // weird — they exist to sharpen your understanding of the rules.

    // Will this method return 42 or 99? First place your bets, then run it.
    // The finally block's return silently overrides the try block's return.
    // Modern compilers and linters will warn about this — and rightly so.
    @SuppressWarnings("finally")
    public static int returnDemo() {
        try {
            return 42;
        } finally {
            return 99; // This wins. Don't write code like this.
        }
    }

    // What does this method throw — IllegalStateException or IOException?
    // First place your bets, then see!
    @SuppressWarnings("finally")
    public static void throwDemo() throws IOException {
        try {
            // throw "Hello world"; // Can't — String is not a Throwable.
            throw new IllegalStateException("first");  // unchecked
        } finally {
            try {
                throw new IOException("second");       // checked
            } catch (IOException ignored) { }          // swallowed — go away
        }
        // The finally block ran but its exception was caught internally,
        // so the original IllegalStateException propagates.
    }

    // Does this method terminate, or loop forever? Who wins the tug-of-war
    // between break and continue? What happens if you swap them?
    @SuppressWarnings("finally")
    public static void whileDemo() {
        while (true) {
            try {
                break;     // tries to exit the loop...
            } finally {
                continue;  // ...but finally overrides with "keep going"
            }
        }
        // Answer: infinite loop. finally always wins.
    }

    // -----------------------------------------------------------------------
    // MULTI-CATCH (Java 7+)
    // -----------------------------------------------------------------------
    // When different exception types need the same handling, multi-catch avoids
    // duplicating the catch body. The variable is implicitly final.

    public static void multiCatchDemo() {
        System.out.println("\nMulti-catch demo begins.");
        for (int i = 0; i < 3; i++) {
            try {
                switch (i) {
                    case 0 -> throw new IOException("disk on fire");
                    case 1 -> throw new NumberFormatException("not a number");
                    case 2 -> throw new IllegalStateException("everything is wrong");
                }
            } catch (IOException | NumberFormatException e) {
                // One catch block handles two unrelated checked/unchecked types.
                System.out.println("Handled together: " + e.getClass().getSimpleName()
                        + " — " + e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println("Handled separately: " + e.getMessage());
            }
        }
        System.out.println("Multi-catch demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // TRY-WITH-RESOURCES (Java 7+) AND SUPPRESSED EXCEPTIONS
    // -----------------------------------------------------------------------
    // Any object implementing AutoCloseable can be declared in the try header.
    // Java guarantees close() is called even if the body throws. If close()
    // also throws, that exception is "suppressed" — attached to the primary
    // exception rather than lost.

    // A tiny resource that can be configured to fail on close.
    private static class FlakyResource implements Closeable {
        private final String name;
        private final boolean failOnClose;

        FlakyResource(String name, boolean failOnClose) {
            this.name = name;
            this.failOnClose = failOnClose;
            System.out.println("  Opened " + name);
        }

        public void doWork() throws IOException {
            System.out.println("  " + name + " doing work...");
            throw new IOException(name + " broke during work");
        }

        @Override
        public void close() throws IOException {
            System.out.println("  Closing " + name
                    + (failOnClose ? " (will fail)" : " (success)"));
            if (failOnClose) {
                throw new IOException(name + " broke during close");
            }
        }
    }

    public static void tryWithResourcesDemo() {
        System.out.println("Try-with-resources demo begins.");

        // Java closes resources in reverse declaration order (LIFO), just like
        // a stack. If both doWork() and close() throw, close()'s exception is
        // suppressed — recoverable via getSuppressed().
        try (var r1 = new FlakyResource("resource-A", true);
             var r2 = new FlakyResource("resource-B", false)) {
            r1.doWork(); // throws IOException
        } catch (IOException e) {
            System.out.println("Primary exception: " + e.getMessage());
            // Suppressed exceptions from close() are attached here.
            for (Throwable suppressed : e.getSuppressed()) {
                System.out.println("  Suppressed: " + suppressed.getMessage());
            }
        }
        // Without try-with-resources, the close() exception would silently
        // replace the doWork() exception — a classic pre-Java-7 bug.

        System.out.println("Try-with-resources demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // CUSTOM SEALED EXCEPTION HIERARCHY (Java 17+)
    // -----------------------------------------------------------------------
    // Sealed classes work for exceptions too. A sealed exception hierarchy lets
    // the compiler verify that every error case is handled. This is especially
    // useful in domain-specific APIs where the set of failure modes is known.

    public abstract sealed static class AppException extends Exception
            permits AppException.NotFound, AppException.Forbidden, AppException.Conflict {

        public AppException(String message) { super(message); }

        // Each permitted subclass represents one kind of failure.
        public static final class NotFound extends AppException {
            public NotFound(String what) { super(what + " not found"); }
        }
        public static final class Forbidden extends AppException {
            public Forbidden(String action) { super(action + " is forbidden"); }
        }
        public static final class Conflict extends AppException {
            public Conflict(String detail) { super("conflict: " + detail); }
        }
    }

    // Simulate an operation that can fail in different known ways.
    private static String lookupResource(int id) throws AppException {
        return switch (id) {
            case 1 -> "found-resource-1";
            case 2 -> throw new AppException.NotFound("resource-2");
            case 3 -> throw new AppException.Forbidden("read resource-3");
            default -> throw new AppException.Conflict("duplicate id " + id);
        };
    }

    public static void sealedExceptionDemo() {
        System.out.println("Sealed exception hierarchy demo begins.");
        for (int id : new int[]{1, 2, 3, 99}) {
            try {
                String result = lookupResource(id);
                System.out.println("  id=" + id + " => " + result);
            } catch (AppException e) {
                // Pattern matching (Java 21) on the sealed hierarchy.
                // Because AppException is sealed and we list all permitted
                // subtypes, no default branch is needed.
                String action = switch (e) {
                    case AppException.NotFound nf   -> "show 404 page";
                    case AppException.Forbidden f   -> "redirect to login";
                    case AppException.Conflict c    -> "retry with new id";
                    // No default needed: compiler knows these are all the cases.
                    // (If someone adds a new permitted subtype, this won't compile
                    // until they add a case for it — that's the whole point.)
                };
                System.out.println("  id=" + id + " => " + e.getMessage()
                        + " — action: " + action);
            }
        }
        System.out.println("Sealed exception hierarchy demo ends.\n");
    }

    // -----------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        demo();

        System.out.println("\nreturnDemo() returns: " + returnDemo());

        try {
            throwDemo();
        } catch (Exception e) {
            System.out.println("Caught from throwDemo: " + e);
        }

        multiCatchDemo();
        tryWithResourcesDemo();
        sealedExceptionDemo();

        // whileDemo(); // Uncomment to see an infinite loop. You've been warned.
    }
}