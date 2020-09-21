import java.math.*; // for BigInteger and BigDecimal   

/** The class Fraction implements the integer fractions and some
 *  of their arithmetic and comparison operations. The fractions
 *  are internally implemented using Java's BigInteger class for
 *  arbitrarily large integer values.
 *  @author Ilkka Kokkarinen
 */

public class Fraction implements Comparable<Fraction> {

    // A fraction is internally encoded as numerator and denominator, both BigIntegers.
    
    private BigInteger num; // the numerator
    private BigInteger den; // the denominator, should always be > 0
    
    // The getter methods. Note that we don't have setter methods, since the Fraction
    // class is immutable, which means that an object, once created, cannot change.
    // (This has various advantages which may not be intuitive right now.)
    
    /**
     * Return the numerator of this fraction.
     * @return The numerator of this fraction.
     */
    public BigInteger getNum() { return num; }
    
    /**
     * Return the denominator of this fraction.
     * @return The denominator of this fraction.
     */
    public BigInteger getDen() { return den; }
    
    // To convert a BigInteger to an int, just call its method intValue().
    // To convert a Fraction to a double approximation is a bit harder.
    /**
     * Convert this fraction into the closest possible {@code double} value.
     * @return This fraction as the closest possible {@code double}.
     */
    public double doubleValue() {
        BigDecimal nd = new BigDecimal(num);
        BigDecimal dd = new BigDecimal(den);
        return nd.divide(dd, 20, BigDecimal.ROUND_UP).doubleValue();
    }
    
    /**
     * Construct a fraction from given numerator and denominator, as ints.
     * @param num The numerator of the fraction.
     * @param den The denominator of the fraction.
     */
    public Fraction(int num, int den) {
        this(new BigInteger("" + num), new BigInteger("" + den));
    }
    
    /**
     * Construct a fraction from given numerator and denominator, as BigIntegers.
     * @param num The numerator of the fraction.
     * @param den The denominator of the fraction.
     */
    public Fraction(BigInteger num, BigInteger den) {
        this.num = num;
        this.den = den;
        simplify();
    }
    
    /**
     * Construct a fraction that is an integer, from an int.
     * @param num The integer part of this fraction.
     */
    public Fraction(int num) {
        this(new BigInteger("" + num));
    } 
    
    /**
     * Construct a fraction that is an integer, from a BigInteger.
     * @param num The integer part of this fraction.
     */
    public Fraction(BigInteger num) {
        this.num = num;
        this.den = BigInteger.ONE;
        // no need to simplify, fraction is already in lowest terms
    }
    
    // Addition of fractions. Note that to add two fractions, call this method for one of them,
    // and pass the second one as parameter. This method doesn't modify either fraction, but
    // creates and returns a new fraction that contains the result.
    
    /**
     * Create a new fraction that is the sum of this fraction and the {@code other} fraction.
     * @param other The other fraction to add.
     * @return A fraction that contains the sum of the two fractions.
     */
    public Fraction add(Fraction other) {
        return new Fraction(this.num.multiply(other.den).add(this.den.multiply(other.num)), this.den.multiply(other.den));
    }
    
    /**
     * Create a new fraction that is the product of this fraction and the {@code other} fraction.
     * @param other The other fraction to multiply.
     * @return A fraction that contains the product of the two fractions.
     */
    public Fraction multiply(Fraction other) {
        return new Fraction(this.num.multiply(other.num), this.den.multiply(other.den));
    }
    
    /**
     * Create a new fraction that is the difference of this fraction and the {@code other} fraction.
     * @param other The other fraction to subtract.
     * @return A fraction that contains the difference of the two fractions.
     */
    public Fraction subtract(Fraction other) {
        return new Fraction(this.num.multiply(other.den).subtract(this.den.multiply(other.num)), this.den.multiply(other.den));
    }
    
    /**
     * Create a new fraction that is the quotient of this fraction and the {@code other} fraction.
     * @param other The other fraction to divide.
     * @return A fraction that contains the quotient of the two fractions.
     */
    public Fraction divide(Fraction other) {
        return new Fraction(this.num.multiply(other.den), this.den.multiply(other.num));
    }
    
    /**
     * Check the equality of this fraction and the {@code other} fraction.
     * @param other The other fraction of the equality comparison.
     * @return {@code true} if the fractions are equal, {@code false} otherwise.
     */
    public boolean equals(Object o) {
        if(o instanceof Fraction) {
            Fraction other = (Fraction) o; // downcast to correct subtype
            return (this.num.equals(other.num) && this.den.equals(other.den));
        }
        else {
            return false;
        }
    }    
    
    /**
     * Compute the hash code for this object. We combine the hash code from
     * the hash codes of the numerator and denominator. The bytes of the
     * denominator's hash code are swapped before combining results with
     * "exclusive or" operator ^, which you note does not mean the power
     * function in Java. Java does not have an integer power function at all.
     * Python's power function is **, with ^ in the same "xor" role as here.
     * @return The hash code of this Fraction.
     */
    public int hashCode() {
        int hd = den.hashCode();
        int hn = num.hashCode();
        // As not to hash a/b and b/a to the same value, do some bitwise
        // arithmetic to one of their hash codes to break the symmetry.
        hd = (hd >> 16) ^ ~(hd << 16);
        // Hash codes are often combined from pieces with bitwise arithmetic.
        return hn ^ hd; // ^ is bitwise xor, not exponentiation.
    }
    
    /**
     * The ordering comparison of fractions.
     * @param other The other fraction of the order comparison.
     * @return -1, 0 or +1 depending on the result of the comparison.
     */
    public int compareTo(Fraction other) {
        // We just subtract the fractions and return the sign of result.
        Fraction diff = this.subtract(other);
        return diff.getNum().signum();  
    }
    
    /**
     * Construct the {@code String} representation of this fraction.
     */
    public String toString() {
        if(den.equals(BigInteger.ONE)) { return num.toString(); }
        else { return num + "/" + den; }
    }
    
    // A private method for simplifying the initial value to lowest terms.
    private void simplify() { 
        if(den.signum() == -1) { // we want the denominator to always be positive
            den = den.negate(); num = num.negate();
        }
        
        BigInteger gcd = num.gcd(den); // handy!
        num = num.divide(gcd); // to simplify a fraction num/den, divide both num
        den = den.divide(gcd); // and den by their greatest common divisor
    }
}