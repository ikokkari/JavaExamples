public class NestedDemo {

    private static int count = 0;
    private int x;

    public NestedDemo(int x) {
        int z = x*x*x;
        class Inner { // A nonstatic nested class, an "inner class"
            private int xx = count++; // We can access all members of the outer class.
            public void report(int xx) {
                System.out.println("Parameter x equals " + xx);
                System.out.println("Inner object x equals " + this.xx);
                System.out.println("Context object x equals " + x);
                System.out.println("Local variable z equals " + z);
            }
        }
        System.out.println("Creating a new NestedDemo object.");
        this.x = x;
        Inner i1 = new Inner();
        Inner i2 = new Inner();
        i1.report(17);
        i2.report(99);
        System.out.println("Finished creating the NestedDemo object.\n");
    }

    public static void main(String[] args) {
        new NestedDemo(12345);
        new NestedDemo(67890);
    }
}
