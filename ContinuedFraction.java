import java.math.*;
import java.util.*;

// https://en.wikipedia.org/wiki/Continued_fraction

// A decorator that takes an existing Iterator<Integer> and treats the
// values it produces as coefficients of a continuing fraction, and
// produces the sequence of exact integer Fractions defined by these
// coefficients so far.

public class ContinuedFraction implements Iterator<Fraction> {
    
    // The continued fraction so far simplified to its lowest form.
    private Fraction state = new Fraction(1);
    // The iterator that produces the terms of this continued fraction.
    private Iterator<Integer> it;
    
    public ContinuedFraction(Iterator<Integer> it) { this.it = it; }
    
    public boolean hasNext() { 
        return it.hasNext();
    }
    
    public Fraction next() {
        int v = it.next();
        // If the current state is a/b, next state is given by 1/(v + a/b)...
        BigInteger a = state.getNum();
        BigInteger b = state.getDen();
        // ...which simplifies to 1/((bv+a)/b), which equals b/(bv+a) 
        state = new Fraction(b, b.multiply(new BigInteger(""+v)).add(a));
        return state;
    }
    
    /* 
     * Output the first 1000 digits after the decimal point of the Golden ratio.
     * Of all irrational numbers, the Golden ratio has the simplest possible
     * representation as a continued fraction, with each term of the infinite
     * series being equal to 1. Unfortunately, other famous irrationals such as
     * pi and e tend to have more complicated continued fraction forms. However,
     * this same idea generalizes to more powerful representations as sequences
     * of integers that allows us to compute even those irrationals out up to
     * any finite precision we wish.
     */
    
    public static void computeGoldenRatioDemo() {
        final int PREC = 1000; // How many decimal places the result is computed to.
        final int PERLINE = 50; // How many digits are printed per line.
        final int N = 2300; // How many terms of continuing fractions are generated.
        
        // I found the value of N for the result to converge by trial and error. With
        // some other numbers, you need some more sophisticated stopping criteria.
        
        // An iterator that produces a series of count copies of value v.
        class Repeat implements Iterator<Integer> {
            private int count, val;
            public Repeat(int val, int count) { this.val = val; this.count = count; }
            public boolean hasNext() { return count > 0; }
            public Integer next() { count--; return 1; }
        }
        
        // Iterator that produces ever more accurate approximations of Golden ratio.
        Iterator<Fraction> goldenApprox = new ContinuedFraction(new Repeat(1, N));
        // (Try what happens if your sequence repeats some other constant than one.)
        
        // Generate the approximation as result of continuing fraction.
        Fraction gf = new Fraction(1);
        while(goldenApprox.hasNext()) { gf = goldenApprox.next(); }

        // Create BigDecimal objects from BigInteger objects we have.
        BigDecimal num = new BigDecimal(gf.getNum());
        BigDecimal den = new BigDecimal(gf.getDen());        
        // Since BigDecimal divisions are generally non-terminating, you
        // need to specify how many decimal places your want, and how you
        // want the truncated decimal after the last one to be handled.
        BigDecimal golden = num.divide(den, PREC, BigDecimal.ROUND_FLOOR);
        // Extract the decimals and print them on console.
        String decimals = golden.toString(); 
        int pos = decimals.indexOf('.') + 1;
        System.out.println("After decimal point, first " + PREC + " decimals of Golden ratio:\n");
        while(pos < decimals.length()) {
            System.out.println(decimals.substring(pos, Math.min(pos + PERLINE, decimals.length())));
            pos += PERLINE;
        }
        
        // (The built-in double type has 51 bits (about 17 decimal digits)
        // of precision that must handle both the integer and the real part
        // of that number. Separate 12 bits of scale determine which bits
        // represent which powers of the base two. Therefore, the double
        // type cannot tell apart the numbers x and x+y whenever y is 18+
        // orders of magnitude smaller than x, and x == x+y evaluates to true.)
    }
}