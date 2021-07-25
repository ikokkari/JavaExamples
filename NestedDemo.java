public class NestedDemo {

    // Private variables, both static and instance, cannot be accessed from the
    // outside. However, the murderer is already inside the house...
    private static int count = 0;
    private final int x;

    public NestedDemo(int x) {
        // Local variable z is effectively final without us having to actually
        // declare that with the modifier final, since the compiler can see that
        // the variable is never again assigned to.
        int z = x*x*x; // final int z = x*x*x;
        
        // A local class is an inner class defined inside the only method that
        // ever uses it. Inner and local classes are used to separate some part
        // of the behaviour of the outer class into a separate entity that is
        // not shown to the outside world.
        class Local { 
            private final int x;
            public Local() {
                x = count++; // Methods can access all members of the outer class.
            }
            public void report(int x) {
                // Note also how to disambiguate between three different variables named x.
                System.out.println("Parameter x equals " + x);
                System.out.println("Field x in the inner object equals " + this.x);
                System.out.println("Field x in the context object equals " + NestedDemo.this.x);
                System.out.println("Local variable z of surrounding method equals " + z);
            }
        }
        
        System.out.println("Creating a new NestedDemo object.");
        this.x = x;
        Local i1 = new Local();
        Local i2 = new Local();
        i1.report(17);
        i2.report(99);
        System.out.println("Finished creating the NestedDemo object.\n");
    }

    public static void main(String[] args) {
        new NestedDemo(42);
        new NestedDemo(99);
    }
}
