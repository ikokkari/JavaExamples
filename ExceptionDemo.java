import java.io.IOException;

public class ExceptionDemo {

    // Fails and throws an IOException, which we must declare since it's checked
    private static void failOne() throws IOException {
        if(true) { // fool the compiler to think that the last line is reachable
            throw new IOException("testing");
        }
        System.out.println("Execution does not get here.");
    }

    // Calls failOne but does not handle the exception that it can throw.
    // Therefore this method must also declare that it may throw IOExceptions.
    private static void failTwo() throws IOException {
        try {
            failOne(); // fails
            System.out.println("Execution does not get here.");
        }
        finally { // this code will be executed no matter what
            System.out.println("But the execution does get here.");
        }
    }
    
    // The test method catches and handles the exception that was thrown
    // from two levels below it in the method stack.
    public static void test() {
        try {
            failTwo(); // fails by calling something that fails
        } catch(IOException e) {
            System.out.println("Caught an exception " + e);
            System.out.println("Printing the stack trace: ");
            StackTraceElement[] trace = e.getStackTrace();
            for(int i = 0; i < trace.length; i++) {
                System.out.print(trace[i].getClassName() + " ");
                System.out.print(trace[i].getMethodName() + " ");
                System.out.println(trace[i].getLineNumber() + " ");
            }
        }
        finally {
            System.out.println("And we are finally done!");
        }
    }
}