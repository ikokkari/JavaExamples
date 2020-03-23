// Java 5 came out in 2005. Its features are no longer new, but part of language.

// Ordinary imports
import java.util.*;
import java.lang.reflect.Field;
import java.lang.annotation.*;

// Static imports
import static java.lang.System.*;
import static java.lang.Math.*;

public class Java5Demo {
    
    // Annotations. To define your annotation type, use @interface.
    // Annotations can themselves be annotated with meta-annotations,
    // especially to define their retention policy and target.
    @Retention(RetentionPolicy.RUNTIME) // keep for runtime
    @Target(ElementType.TYPE) // can be applied to classes and other types
    @interface Author {
        String name();
        int year() default 2014;
    }
    
    // Let us use our new annotation type on a little class.
    @Author(name = "Ilkka Kokkarinen")
    private static class AnnotationTest { /* whatevs */ }
    
    // And then use reflection to examine it at the runtime. If the
    // Author annotation were not retained until runtime, but discarded
    // after compilation, it would not show up in the following output.
    public static void annotationDemo() {
        AnnotationTest at = new AnnotationTest();
        Class c = at.getClass();
        for(Annotation ant: c.getAnnotations()) {
            System.out.print("Found annotation: " + ant);
        }
    }
    
    // Boxing and unboxing
    public static void boxingDemo() {
        Integer a = 42; // boxing
        int b = new Integer(9999); // unboxing
        System.out.println(a + b); // conversions take place in arithmetic
        Integer c = 42;
        Integer d = 9999;
        Integer e = new Integer(9999);
        if(a == c) { out.println("a == c"); } // true
        if(b == d) { out.println("b == d"); } // true (primitive == wrapper)
        if(d == e) { out.println("d == e"); } // false (can you explain why?)
    }
    
    // Varargs
    public static String concatenate(Object... rest) {
        String result = "";
        for(Object o: rest) { result += o + " "; }
        return result;
    }
    
    // C-style printf
    public static void printfDemo() {
        int a = 99; char b = 'x'; String c = "Hello";
        out.printf("Now a = %d, b = %c and c = %s\n", a, b, c);    
        double d = 1234.56789;
        out.printf("With two decimal places, sqrt(d) = %.2f\n", sqrt(d));
    }
    
    // Adapted from a thread in Stack Overflow. Truly evil.
    public static void orwellian() throws Exception {
        Class cache = Integer.class.getDeclaredClasses()[0];
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
        Queue<Integer> aq = new PriorityQueue<Integer>();
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