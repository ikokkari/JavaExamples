/**
 * The first proper class in this course is a class whose instances represent
 * lengths. The same length can be expressed in both centimeters and inches.
 * (It is still the same physical quantity, even if the units that we use to
 * talk about it are different.)
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
     * @param newcm The new length in centimeters.
     */
    public void setCentimeters(double newcm) {
        cm = newcm; // Assign parameter value to the data field inside the object. 
    }
    
    /**
     * Getter method for centimeters.
     * @return The current length in centimeters.
     */
    public double getCentimeters() {
        return cm; // Return the value from data field inside the object.
    }
    
    // First the conversion factor: to declare a field static means that it is
    // shared between all objects of this class, and to declare it final means
    // that it is a "named constant" whose value cannot be changed later. You
    // should prefer named constants to "magic numbers" hardwired in code.
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
     * @param newcm The length in centimeters.
     */
    public Length(double newcm) {
        // We can call existing methods in the class, instead of duplicating their code.
        // This does not make a big difference here, but might in a different class where
        // the field initialization requires some more complex computations and checks.
        setCentimeters(newcm);
    }
    
    /**
     * Compute and return the String representation of this Length object.
     * @return The length stored in this object in centimeters.
     */
    public String toString() {
        return "A length of " + this.cm + " cm."; // String representation of this object.
    }
    
    /**
     * A main method for demonstration purposes. This class is not really intended to be
     * a standalone application, but to be used as a useful part of a larger application.
     */
    public static void main(String[] args) {
        Length a = new Length(20); // Create a new object with the operator new.
        System.out.println(a); // A length of 20 cm.
        System.out.println(a.getCentimeters()); // 20
        System.out.println(a.getInches()); // 7.874015748031496
        // See the DataDemo example next week to find out how to print decimal
        // numbers up to some more reasonable precision.
    }
}