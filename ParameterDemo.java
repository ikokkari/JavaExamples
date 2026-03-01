/**
 * Java parameter passing: it's <em>always</em> call-by-value, but the same
 * mechanism produces very different observable effects depending on what
 * type of value is being passed.
 * <p>
 * The three cases demonstrated here:
 * <ol>
 *   <li><b>Primitive</b> — the value is copied. The method gets its own
 *       independent copy, so modifying it has no effect on the caller.</li>
 *   <li><b>Array / object reference</b> — the <em>reference</em> is copied.
 *       The method gets its own copy of the pointer, but that pointer still
 *       aims at the same object. So modifying the object's contents through
 *       the copy is visible to the caller.</li>
 *   <li><b>Reassigning a reference</b> — the method can make its local copy
 *       point to a different object, but this does not affect the caller's
 *       reference. This is the key proof that Java is call-by-value for
 *       references, not call-by-reference.</li>
 * </ol>
 * <p>
 * The mental model: imagine every argument is photocopied before being handed
 * to the method. For a primitive, the photocopy is an independent number.
 * For a reference, the photocopy is a second arrow pointing to the same box.
 * You can reach into the box through either arrow, but snapping one arrow to
 * point somewhere else doesn't affect the other arrow.
 *
 * @author Ilkka Kokkarinen
 */
public class ParameterDemo {

    // -----------------------------------------------------------------------
    // Case 1: Primitive parameter — the value itself is copied.
    // -----------------------------------------------------------------------

    private static void incrementPrimitive(int value) {
        value++;
        // This modifies the LOCAL COPY only. The caller's variable is untouched.
        System.out.println("  Inside method: value = " + value);
    }

    public static void demoPrimitive() {
        System.out.println("--- Case 1: Primitive (int) ---");
        int number = 5;
        System.out.println("Before call: number = " + number);       // 5
        incrementPrimitive(number);                                    // prints 6
        System.out.println("After call:  number = " + number);       // still 5!
        System.out.println("The original is unchanged — the method only had a copy.\n");
    }

    // -----------------------------------------------------------------------
    // Case 2: Array (object) parameter — the reference is copied, but both
    //         the original and the copy point to the SAME array object.
    // -----------------------------------------------------------------------

    private static void incrementArrayElement(int[] array) {
        array[42]++;
        // This modifies the SHARED OBJECT that both caller and method can see.
        System.out.println("  Inside method: array[42] = " + array[42]);
    }

    public static void demoArray() {
        System.out.println("--- Case 2: Array reference (int[]) ---");
        var numbers = new int[100];
        numbers[42] = 5;
        System.out.println("Before call: numbers[42] = " + numbers[42]); // 5
        incrementArrayElement(numbers);                                    // prints 6
        System.out.println("After call:  numbers[42] = " + numbers[42]); // 6!
        System.out.println("The original IS changed — both references pointed to the same array.\n");
    }

    // -----------------------------------------------------------------------
    // Case 3: Reassigning a reference parameter — the crucial test.
    //
    // If Java were truly call-by-reference, reassigning the parameter
    // inside the method would also change the caller's variable. It doesn't.
    // This proves that Java passes references BY VALUE.
    // -----------------------------------------------------------------------

    private static void tryToReplace(int[] array) {
        // We can modify the object that the reference points to:
        array[0] = 999;
        System.out.println("  After modifying contents: array[0] = " + array[0]);

        // But reassigning the local reference does NOT affect the caller:
        array = new int[]{-1, -2, -3};
        System.out.println("  After reassignment:       array[0] = " + array[0]);
        // This new array exists only inside this method.
    }

    public static void demoReassignment() {
        System.out.println("--- Case 3: Reassigning a reference ---");
        var original = new int[]{10, 20, 30};
        System.out.println("Before call: original[0] = " + original[0]); // 10
        tryToReplace(original);
        System.out.println("After call:  original[0] = " + original[0]); // 999, not -1!
        System.out.println("""
                The content change (999) was visible because both references shared the object.
                The reassignment (-1) was NOT visible because only the local copy was redirected.
                This is the proof that Java is call-by-value, even for references.
                """);
    }

    // -----------------------------------------------------------------------
    // Main — run all three demonstrations in order.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        demoPrimitive();
        demoArray();
        demoReassignment();
    }
}