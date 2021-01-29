// Demonstrate the difference in Java parameter passing when applied to primitive 
// types versus object references. The same discipline of call-by-value used in
// all of Java parameter passing produces very different results for each kind.

public class ParameterDemo {
    
    private void fooPrimitive(int x) {
        x++;
        System.out.println("Inside method foo, x is now " + x + "."); // 2
    }
    
    public void demoPrimitive() {
        int a = 5;
        System.out.println("To start, a is " + a + "."); // 1
        fooPrimitive(a);
        System.out.println("After call, a is " + a + "."); // 3
    }
    
    private void fooArray(int[] x) {
        x[42]++;
        System.out.println("Inside method foo, x[42] is now " + x[42] + ".");
    }
    
    public void demoArray() {
        int[] a = new int[100];
        a[42] = 5;
        System.out.println("To start, a[42] is " + a[42] + "."); // 1
        fooArray(a);
        System.out.println("After call, a[42] is " + a[42] + "."); // 3
    }
}
