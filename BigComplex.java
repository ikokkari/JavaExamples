import java.math.*;

public class BigComplex {

    private static final int INITPRECISION = 10;
    public static MathContext mc = new MathContext(INITPRECISION);
    public static final BigComplex ZERO = new BigComplex(0, 0);
    public static final BigComplex ONE = new BigComplex(1, 0);
    public static final BigComplex I = new BigComplex(0, 1);
    
    // A complex number consists of real and imaginary parts.
    private final BigDecimal re;
    private final BigDecimal im;
    
    public BigComplex(double re, double im) {
        this.re = new BigDecimal(re, mc);
        this.im = new BigDecimal(im, mc);
    }
    
    public BigComplex(BigDecimal re, BigDecimal im) {
        this.re = re; this.im = im;
    }
    
    public BigDecimal getRe() { return re; }
    public BigDecimal getIm() { return im; }
    
    public BigComplex add(BigComplex other) {
        return new BigComplex(this.re.add(other.re, mc), this.im.add(other.im, mc));
    }
    
    public BigComplex subtract(BigComplex other) {
        return new BigComplex(this.re.subtract(other.re, mc), this.im.subtract(other.im, mc));
    }
    
    public BigComplex multiply(BigComplex other) {
        BigDecimal r1 = this.re.multiply(other.re, mc);
        BigDecimal r2 = this.im.multiply(other.im, mc);
        BigDecimal i1 = this.im.multiply(other.re, mc);
        BigDecimal i2 = other.im.multiply(this.re, mc);
        return new BigComplex(r1.subtract(r2, mc), i1.add(i2, mc));
    }
    
    public BigDecimal getAbsSq() {
        return this.re.multiply(this.re, mc).add(this.im.multiply(this.im, mc), mc);
    }
    
    public String toString() {
        if(this.im.compareTo(BigDecimal.ZERO) >= 0) {
            return "(" + this.re + "+" + this.im + "i)";
        }
        else {
            return "(" + this.re + "" + this.im + "i)";
        }
    }
}