import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

// As in http://tutorials.jenkov.com/java-reflection/index.html

public class ReflectionDemo {

    // Utility method needed to simplify the findNeighbours method below.
    private static void processElement(
    Object next,
    IdentityHashMap<Object,
    Integer> discovered,
    Queue<Object> frontier,
    int depth
    ) {
        if(next != null && !discovered.containsKey(next)) {
            frontier.offer(next);
            discovered.put(next, depth + 1);
        }
    } 

    /**
     * Uses reflection to discover all objects reachable from the given object by
     * following the reference fields, up to the distance limit. This method does
     * the search in breadth first order by using a FIFO queue as the frontier.
     * @param start The object from which to start the search.
     * @param limit The limit of how many levels to search.
     * @param verbose If true, the method prints out the objects that it finds.
     * @return A set of objects that are reachable from the start object.
     */
    public static Set<Object> findNeighbours(Object start, int limit, boolean verbose) {
        // The objects discovered during the search, using memory address equality
        // as the equivalence criterion.
        IdentityHashMap<Object, Integer> discovered = new IdentityHashMap<>();
        discovered.put(start, 0);
        // The search frontier of objects discovered and waiting to be processed.
        Queue<Object> frontier = new LinkedList<Object>();
        frontier.offer(start);
        // Repeatedly pop an object from the frontier, and add its undiscovered
        // neighbours into the frontier.
        while(frontier.size() > 0) {
            Object current = frontier.poll();
            if(verbose) {
                System.out.println("PROCESSING <" + current + "> AT DEPTH " + discovered.get(current));
            }
            int depth = discovered.get(current);
            if(depth < limit) {
                Class c = current.getClass();
                for(Field f: c.getDeclaredFields()) {
                    Class ft = f.getType();
                    if(ft.isArray()) { // Arrays must be handled with special care.
                        if(verbose) { System.out.println("Following field " + f.getName() + ":"); }
                        f.setAccessible(true); // Follow even private fields.
                        try {
                            Object elems = f.get(current);
                            if(elems == null) { continue; }
                            int len = Array.getLength(elems);
                            for(int i = 0; i < len; i++) {
                                Object elem = Array.get(elems, i);
                                if(elem != null) {
                                    processElement(elem, discovered, frontier, depth);
                                }
                            }
                        } catch(IllegalAccessException e) {
                            if(verbose) { System.out.println("Error: " + e); }
                        }
                    }
                    else if(!ft.isPrimitive()) { // Follow a reference field.
                        if(verbose) { System.out.println("Following field " + f.getName() + ":"); }
                        f.setAccessible(true); // Follow even private fields.
                        try {
                            processElement(f.get(current), discovered, frontier, depth);
                        } catch(IllegalAccessException e) {
                            if(verbose) { System.out.println("Error: " + e); }
                        }
                    }
                }
            }
        }
        return discovered.keySet();
    }

    public static void main(String[] args) {
        ArrayList<Object> a = new ArrayList<>();
        a.add("Hello");
        a.add(42);
        a.add(a);
        System.out.println("Demonstrating the discovery of object neighbourhood.");
        System.out.println(findNeighbours(a, 2, true).size());
    }
}