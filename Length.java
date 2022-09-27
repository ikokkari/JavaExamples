/**
 * The first proper class in this course is a class whose instances represent
 * lengths. The same length can be expressed in both centimeters and inches.
 * (It is still the same physical quantity, even if the units that we use to
 * talk about it are different.)
 * 
 * "All services offered by a module should be available through a uniform
 * notation, which does not betray whether they are implemented through storage
 * or through computation." -- Bertrand Meyer
 * 
 * https://martinfowler.com/bliki/UniformAccessPrinciple.html
 * 
 * @author Ilkka Kokkarinen
*/

public class Length {

    // We choose to store the length in centimeters. We could have just as well
    // chosen to store the length in inches, in which case the following methods
    // would have been written as mirror images.
    private double cm;
    
    /**
     * Setter method for centimeters.
     * @param newCM The new length in centimeters.
     */
    public void setCentimeters(double newCM) {
        cm = newCM; // Assign parameter value to the data field inside the object.
    }
    
    /**
     * Getter method for centimeters.
     * @return The current length in centimeters.
     */
    public double getCentimeters() {
        return cm; // Return the value from data field inside the object.
    }
    
    // First the conversion factor: to declare a field static means that it is
    // shared between all objects of this class, and to also declare it final
    // means that it is a "named constant" whose value cannot be changed later.
    // You should prefer named constants to "magic numbers" hardwired in code.
    private static final double CM_PER_INCH = 2.54;
    
    /**
     * Setter method for inches.
     * @param inches The new length in inches.
     */
    public void setInches(double inches) {
        cm = inches * CM_PER_INCH; // Convert inches to cm for storage.
    }
    
    /**
     * Getter method for inches.
     * @return The current length in inches.
     */
    public double getInches() {
        return cm / CM_PER_INCH; // Convert stored cm to inches.
    }
    
    // As an exercise, try modifying this class to allow the length to be accessed in
    // some more exotic units such as furlongs or light seconds. The idea is the same
    // as with inches, you just need to look up or compute the appropriate conversion
    // factor to be defined as a similar named constant as CM_PER_INCH.
    
    /**
     * Constructor for this class.
     * @param newCM The length in centimeters.
     */
    public Length(double newCM) {
        // We can call existing methods in the class, instead of duplicating their code.
        // This does not make a big difference here, but might in a different class where
        // the field initialization requires some more complex computations and checks.
        setCentimeters(newCM);
    }
    
    /**
     * Compute and return the String representation of this Length object.
     * @return The length stored in this object in centimeters.
     */
    public String toString() {
        return "Length of " + this.cm + " cm."; // String representation of this object.
    }
    
    /**
     * A main method for demonstration purposes. This class is not really intended
     * as a standalone application, but is to be used as a part of a larger system.
     */
    public static void main(String[] args) {
        Length a = new Length(20); // Create a new object with the operator new.
        System.out.println(a); // A length of 20 cm.
        Length b = new Length(20); // Another new object.
        System.out.println("a in centimeters is " + a.getCentimeters()); // 20
        System.out.println("a in inches is " + a.getInches()); // 7.874015748031496
        System.out.println("b in centimeters is " + b.getCentimeters()); // 20
        System.out.println("b in inches is " + b.getInches()); // 7.874015748031496
        a.setInches(20);
        System.out.println("a in centimeters is " + a.getCentimeters()); // 50.8
        System.out.println("a in inches is " + a.getInches()); // 20.0
        System.out.println("b in centimeters is " + b.getCentimeters()); // 20
        System.out.println("b in inches is " + b.getInches()); // 7.874015748031496
        // See the DataDemo example to find out how to print decimal numbers up
        // to some more reasonable precision.
    }
}