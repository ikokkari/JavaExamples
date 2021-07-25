/**
 * The first example class for CCPS 109. An object constructed from this
 * class has two capabilities, the ability to output a greeting, and the
 * ability to output a bye message. The objects contain no data fields or
 * other internal state.
 * @author Ilkka Kokkarinen
 */

public class Greeter {

    /**
     * Outputs a simple greeting to the console.
     */
    public void greet() {
        System.out.println("Hello, world!");
    }
    
    /**
     * Outputs a simple goodbye message to the console.
     */
    public void bye() {
        System.out.println("See you later!");
    }
    
    /**
     * Having the main method allows this class to execute as a
     * standalone application. When using BlueJ, you don't need
     * a main method in your class to try out its objects and
     * their methods interactively. Most classes are not intended
     * to be standalone applications, but to be used as parts of
     * some larger program.
     */
    public static void main(String[] args) {
        // Declare and create a new Greeter object.
        Greeter g = new Greeter();
        // Now we can call its methods.
        g.greet();
        g.bye();
        g.greet();
    }
}
