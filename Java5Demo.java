// Ordinary imports
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.PriorityQueue;
import java.util.Queue;

// Static imports
import static java.lang.Math.sqrt;
import static java.lang.System.out;

// Java 5 came out in 2005. Its features are no longer new, but established part of the
// language. Pretty soon I will have students who are younger than Java 5.

public class Java5Demo {
    
    // Annotations. To define your annotation type, use @interface.
    // Annotations can themselves be annotated with meta-annotations,
    // especially to define their retention policy and target.
    @Retention(RetentionPolicy.RUNTIME) // keep for runtime
    @Target(ElementType.TYPE) // May be applied to classes and other types
    @interface Author {
        String name();
        int year() default 2014; // How the time goes by...
    }
    
    // Let us use our new annotation type on a little class.
    @Author(name = "Ilkka Kokkarinen")
    private static class AnnotationTest { /* whatevs */ }
    
    // And then use reflection to examine it at the runtime. If the
    // Author annotation were not retained until runtime, but discarded
    // after compilation, it would not show up in the following output.
    public static void annotationDemo() {
        AnnotationTest at = new AnnotationTest();
        Class<? extends AnnotationTest> c = at.getClass();
        for(Annotation ant: c.getAnnotations()) {
            System.out.print("Found annotation: " + ant);
        }
    }
    
    // Boxing and unboxing demonstrated. IntelliJ IDEA will mark many of these redundant.
    public static void boxingDemo() {
        Integer a = 42;     // Boxing primitive to wrapper, small enough to be in cache.
        Integer b = 42;     // Boxing primitive to wrapper, reuses 42 from cache.
        int c = 42;         // Primitive int value.
        assert a == c;      // Primitive vs. wrapper, unbox and compare values.
        assert a == b;      // Wrapper vs. wrapper, memory address comparison.
        assert a.equals(b); // Wrapper vs. wrapper, object content comparison.
        
        Integer d = 84;     // Boxing primitive to wrapper, small enough to be in cache.
        Integer e = new Integer(84); // new always creates a new object, no matter what
        assert d != e;      // Memory address comparison, guaranteed unequal here.
        assert d.equals(e); // Wrapper vs. wrapper, object content comparison.
        assert a + b == e;  // Arithmetic automatically unboxes before the operation.
        assert 2 * a == e;
        assert e / 2 == b;
        assert a < e;       // As do the order comparisons.
        
        Integer f = 9999;   // Boxing primitive to wrapper, outside the cache range.
        Integer g = 9999;   // Boxing primitive to wrapper, outside the cache range.
        assert f != g;      // Memory address comparison, guaranteed different here.
        assert f.equals(g); // Wrapper vs. wrapper, object content comparison.
        
        System.out.println("Sleep well. Integers still work the way they are supposed to.");
    }
    
    // Varargs
    public static String concatenate(Object... rest) {
        StringBuilder result = new StringBuilder();
        for(Object o: rest) { result.append(o).append(" "); }
        return result.toString();
    }
    
    // C-style printf
    public static void printfDemo() {
        int a = 99; char b = 'x'; String c = "Hello";
        out.printf("Now a = %d, b = %c and c = %s\n", a, b, c);    
        double d = 1234.56789;
        out.printf("To two decimal places, sqrt(d) = %.2f\n", sqrt(d));
    }
    
    // Adapted from a thread in Stack Overflow. Truly evil.
    public static void orwellian() throws Exception {
        Class<?> cache = Integer.class.getDeclaredClasses()[0];
        Field c = cache.getDeclaredField("cache");
        c.setAccessible(true);
        Integer[] array = (Integer[]) c.get(cache);
        array[132] = array[133];
        // Two plus two is whatever The Party says it is, Winston.
        System.out.printf("%d", 2 + 2); // 5
        // Even the basic laws of arithmetic must yield to doublethink.
    }
    
    // Queue collections: an absurdity in terms. Saying that a Queue is-a
    // Collection is just like saying that a Car is-an Engine.
    public static void queueDemo() {
        Queue<Integer> aq = new PriorityQueue<>();
        aq.add(7); aq.offer(2); aq.offer(3);
        out.println("head element: " + aq.peek()); // 2
        out.println("popped element: " + aq.remove()); // 2
        out.println("head element: " + aq.peek()); // 3
        out.println("popped element: " + aq.remove()); // 3
        out.println("popped element: " + aq.remove()); // 7
    }
    
    // That said, PriorityQueue<T> often comes handy in clever algorithms.
    // Just don't pretend that it is also some kind of Collection. (Or a
    // floor wax. Or a dessert topping.)
}