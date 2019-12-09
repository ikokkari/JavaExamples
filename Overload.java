/* 
 * Overloading is resolved at the compile time, based on declared parameter types.
 * Overriding is resolved at runtime, based on the type of the object "this".
 * This example class demonstrates this crucial difference between these two.
 */

public class Overload {
   
    // Just that we have an inheritance hierarchy to use as a parameter type.
    private static class Bird {}
    private static class Hawk extends Bird {}
    private static class Sparrow extends Bird {}
    
    // Then the classes that we use to demonstrate overloading and overriding.
    private static class A {
        public int get(Bird b) { return 1; } // #1
        public int get(Hawk h) { return 2; } // #2
    }
    
    private static class B extends A {
        @Override public int get(Bird b) { return 3; } // #3
        public int get(Sparrow s) { return 4; } // #4
    }
    
    // The demonstration itself.
    public static void main(String[] args) {
        // Our setup for this problem.
        A x = new A();
        A y = new B();
        Bird heckle = new Hawk();
        Sparrow jeckle = new Sparrow();
        
        // Now, what does each method call output?
        System.out.println(x.get(heckle)); // #1
        System.out.println(x.get(jeckle)); // #1
        System.out.println(y.get(heckle)); // #3
        System.out.println(y.get(jeckle)); // #3
        
        // Without defining any new variables, how could you call #2 and #4 ?
        // By downcasting these references to have their correct runtime type
        // already at the compile time.
        System.out.println(x.get((Hawk)heckle)); // #2
        System.out.println(((B)y).get(jeckle)); // #4
        
        // Some languages have "dynamic dispatching" in which the overloading
        // is resolved at runtime based on the argument object types. Java
        // resolves overloading at compile time when objects do not exist.
    }
}