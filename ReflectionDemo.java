import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Queue;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

// https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/package-summary.html

/**
 * Demonstrates Java reflection by performing a breadth-first traversal
 * of the object graph reachable from a starting object. Every reference
 * field (including private fields, array elements, and fields inherited
 * from superclasses) is followed up to a configurable depth limit.
 * <p>
 * This is a useful teaching tool for understanding:
 * <ul>
 *   <li>How objects in the JVM are connected by references</li>
 *   <li>The difference between identity ({@code ==}) and equality
 *       ({@code equals}), since we use {@link IdentityHashMap}</li>
 *   <li>How reflection can bypass access control to inspect private state</li>
 *   <li>The structure of arrays, wrapper types, and collections at the
 *       field level</li>
 * </ul>
 *
 * <h3>Module system caveat (Java 9+)</h3>
 * Reflective access to fields of JDK-internal classes (e.g. the internal
 * array inside {@link ArrayList}) requires the JVM flag
 * {@code --add-opens java.base/java.util=ALL-UNNAMED} (and similar for
 * other modules). Without it, {@code setAccessible(true)} will throw
 * {@link java.lang.reflect.InaccessibleObjectException} for those fields.
 * Run the demo with:
 * <pre>
 *   java --add-opens java.base/java.util=ALL-UNNAMED ReflectionDemo
 * </pre>
 *
 * @author Ilkka Kokkarinen
 */
public class ReflectionDemo {

    /**
     * Discover all objects reachable from {@code start} by following
     * reference fields, up to the given depth limit. The traversal is
     * breadth-first, using identity equality (not {@code equals}) to
     * avoid infinite loops on circular references.
     *
     * @param start   the root object
     * @param limit   maximum BFS depth to explore
     * @param verbose if true, print each object as it is processed
     * @return the set of all reachable objects (including {@code start})
     */
    public static Set<Object> findNeighbours(Object start, int limit,
                                              boolean verbose) {
        // IdentityHashMap uses == instead of equals(), which is essential
        // here: we want to track which *specific* object instances we've
        // seen, not which values. Two different Integer objects holding 42
        // are distinct nodes in the object graph.
        var discovered = new IdentityHashMap<Object, Integer>();
        discovered.put(start, 0);

        // ArrayDeque is the standard FIFO queue in modern Java —
        // LinkedList works but wastes memory on node objects.
        Queue<Object> frontier = new ArrayDeque<>();
        frontier.offer(start);

        while (!frontier.isEmpty()) {
            Object current = frontier.poll();
            int depth = discovered.get(current);
            if (verbose) {
                System.out.printf("  [depth %d] %s: %s%n",
                        depth, current.getClass().getSimpleName(),
                        truncate(current.toString(), 80));
            }
            if (depth >= limit) { continue; }

            // Walk every field in this class AND its superclasses.
            // The original only called getDeclaredFields() on the runtime
            // class, missing any fields defined in parent classes.
            for (var field : allFields(current.getClass())) {
                var fieldType = field.getType();

                // Skip primitives — they aren't references.
                if (fieldType.isPrimitive()) { continue; }
                // Skip static fields — they belong to the class, not
                // the instance, and following them leads into the
                // internals of the class loader.
                if (Modifier.isStatic(field.getModifiers())) { continue; }

                try {
                    field.setAccessible(true);
                } catch (RuntimeException e) {
                    // InaccessibleObjectException (module system boundary).
                    // Can't follow this field — skip it.
                    if (verbose) {
                        System.out.printf("    (cannot access field '%s': %s)%n",
                                field.getName(), e.getMessage());
                    }
                    continue;
                }

                try {
                    Object value = field.get(current);
                    if (value == null) { continue; }

                    if (fieldType.isArray()) {
                        if (verbose) {
                            System.out.printf("    field '%s' is array[%d]%n",
                                    field.getName(), Array.getLength(value));
                        }
                        int len = Array.getLength(value);
                        for (int i = 0; i < len; i++) {
                            Object elem = Array.get(value, i);
                            enqueueIfNew(elem, depth + 1, discovered, frontier);
                        }
                    } else {
                        if (verbose) {
                            System.out.printf("    field '%s' -> %s%n",
                                    field.getName(),
                                    value.getClass().getSimpleName());
                        }
                        enqueueIfNew(value, depth + 1, discovered, frontier);
                    }
                } catch (IllegalAccessException e) {
                    if (verbose) {
                        System.out.printf("    (error reading '%s': %s)%n",
                                field.getName(), e.getMessage());
                    }
                }
            }
        }
        return discovered.keySet();
    }

    /**
     * If {@code obj} is non-null and not yet discovered, add it to
     * the BFS frontier.
     */
    private static void enqueueIfNew(Object obj, int depth,
                                      IdentityHashMap<Object, Integer> discovered,
                                      Queue<Object> frontier) {
        if (obj != null && !discovered.containsKey(obj)) {
            discovered.put(obj, depth);
            frontier.offer(obj);
        }
    }

    /**
     * Collect all declared fields from {@code clazz} and every
     * superclass up to (but not including) {@code Object}.
     */
    private static List<Field> allFields(Class<?> clazz) {
        var fields = new ArrayList<Field>();
        for (var c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(List.of(c.getDeclaredFields()));
        }
        return fields;
    }

    /** Truncate a string for display, appending "..." if it was cut. */
    private static String truncate(String s, int maxLen) {
        // Also collapse any embedded newlines for cleaner output.
        String flat = s.replace('\n', ' ');
        return (flat.length() <= maxLen) ? flat
                : flat.substring(0, maxLen - 3) + "...";
    }

    // -----------------------------------------------------------------------
    // Demo
    // -----------------------------------------------------------------------

    /**
     * Build a small object graph with a circular reference and explore
     * it to depth 2. Run with:
     * <pre>
     *   java --add-opens java.base/java.util=ALL-UNNAMED ReflectionDemo
     * </pre>
     */
    public static void main(String[] args) {
        // Build a small object graph with a cycle.
        var list = new ArrayList<>();
        list.add("Hello");
        list.add(42);          // auto-boxed to Integer
        list.add(list);        // circular reference!
        list.add(List.of(1, 2, 3)); // nested immutable list
        list.add(new int[]{10, 20, 30}); // primitive array (elements are not references)

        System.out.println("Object graph exploration from an ArrayList");
        System.out.println("==========================================");
        var reachable = findNeighbours(list, 2, true);
        System.out.println("\nTotal distinct objects reachable: " + reachable.size());

        // Show a summary of what types were found.
        var typeCounts = new IdentityHashMap<Class<?>, Integer>();
        for (var obj : reachable) {
            typeCounts.merge(obj.getClass(), 1, Integer::sum);
        }
        System.out.println("\nBy type:");
        typeCounts.forEach((type, count) ->
                System.out.printf("  %-40s %d%n", type.getName(), count));
    }
}
