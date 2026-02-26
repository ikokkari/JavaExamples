import java.math.BigDecimal;
import java.math.MathContext;

/**
 * A simple immutable complex number class backed by BigDecimal for arbitrary
 * precision. Also provides a static double-precision fast path for the
 * Mandelbrot iteration, which is ~100x faster than BigDecimal and sufficient
 * for zoom levels down to about 10^-13.
 */
public class BigComplex {

    // The MathContext used for all BigDecimal arithmetic.
    public static MathContext mc = new MathContext(20);

    private final BigDecimal re;
    private final BigDecimal im;

    public BigComplex(BigDecimal re, BigDecimal im) {
        this.re = re;
        this.im = im;
    }

    public BigComplex(double re, double im) {
        this(new BigDecimal(re, mc), new BigDecimal(im, mc));
    }

    public BigDecimal getRe() { return re; }
    public BigDecimal getIm() { return im; }

    public BigComplex add(BigComplex other) {
        return new BigComplex(re.add(other.re, mc), im.add(other.im, mc));
    }

    public BigComplex subtract(BigComplex other) {
        return new BigComplex(re.subtract(other.re, mc), im.subtract(other.im, mc));
    }

    public BigComplex multiply(BigComplex other) {
        // (a+bi)(c+di) = (ac-bd) + (ad+bc)i
        return new BigComplex(
                re.multiply(other.re, mc).subtract(im.multiply(other.im, mc), mc),
                re.multiply(other.im, mc).add(im.multiply(other.re, mc), mc)
        );
    }

    @Override
    public String toString() {
        return "(" + re.toPlainString() + " + " + im.toPlainString() + "i)";
    }

    // ========================================================================
    // Double-precision fast path for Mandelbrot iteration.
    // Returns: positive escape count (with fractional smoothing), or
    //          negative value (-iters) if did not escape within maxIter.
    // ========================================================================

    /**
     * Iterate z = z^2 + c starting from (zr, zi) for the point (cr, ci).
     * Returns a double: if positive, it's the smooth escape iteration count.
     * If negative, its absolute value is the number of iterations performed
     * (the point did not escape).
     *
     * @param zr    current real part of z
     * @param zi    current imaginary part of z
     * @param cr    real part of c
     * @param ci    imaginary part of c
     * @param startIter iteration count already done
     * @param maxIter   total iterations to perform this round
     * @return smooth escape count (positive) or -totalIters (negative)
     */
    public static double iterateDouble(double zr, double zi, double cr, double ci,
                                       int startIter, int maxIter) {
        double zr2 = zr * zr;
        double zi2 = zi * zi;
        int iter = startIter;
        // Use escape radius of 256 for smoother coloring (log2(log2(256)) = 3)
        while (zr2 + zi2 <= 65536.0 && iter < startIter + maxIter) {
            zi = 2.0 * zr * zi + ci;
            zr = zr2 - zi2 + cr;
            zr2 = zr * zr;
            zi2 = zi * zi;
            iter++;
        }
        if (zr2 + zi2 > 65536.0) {
            // Smooth coloring: fractional escape count
            double log_zn = Math.log(zr2 + zi2) / 2.0;
            double nu = Math.log(log_zn / Math.log(2.0)) / Math.log(2.0);
            return iter + 1.0 - nu;
        }
        return -iter;
    }
}