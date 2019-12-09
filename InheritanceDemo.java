// This doesn't do anything useful yet, just demonstrates the language.

public class InheritanceDemo {

    // When a nested class is declared static, objects of that class can
    // exist on their own, without an outer class context object. 
    
    private static class A { // superclass
        public int x; // can be accessed everywhere
        private int y; // can be accessed in same class only
        protected int z; // can be accessed by same class and subclasses
        
        public A() {
            System.out.println("Executing constructor of A");
            x = 17; y = 42; z = 99;    
        }
        
        public void outputValues() {
            System.out.println("x is " + x);
            System.out.println("y is " + y);
            System.out.println("z is " + z);        
        }
        
        public void greet() {
            System.out.println("Hello");    
        }
    }

    private static class B extends A { // subclass
        public int w; // adding new fields
        public int x; // (yes, even this is possible, but bad)
    
        public B() { // Constructors are executed as chain down the line.
            System.out.println("Executing constructor of B");
            w = -123; x = -999;
        }
        
        @Override
        public void outputValues() { // overriding an inherited method
            super.outputValues(); // we first call the superclass version of method
            System.out.println("w is " + w); // then output the new field
            System.out.println("x defined in B is " + x);
            System.out.println("x inherited from A is " + super.x);
        }
            
        @Override
        public void greet() { // overriding an inherited method
            System.out.println("Hi there");
            // Note that in this method, we don't call the superclass version.
        }
    
        public void bye() { // defining whole new methods in the subclass
            System.out.println("Bye");
        }
    }

    public static void main(String[] args) {
        A first = new A();
        A second = new B();
        B third = new B();
        
        first.greet(); // Hello
        second.greet(); // Hi there
        third.greet(); // Hi there
        
        System.out.println("Output the values of first object:");
        first.outputValues();
        System.out.println("Output the values of second object:");
        second.outputValues();
        System.out.println("Output the values of third object:");        
        third.outputValues();
        
        // first.bye(); wouldn't compile
        // second.bye(); wouldn't compile either
        third.bye(); // compiles and runs
    }
}