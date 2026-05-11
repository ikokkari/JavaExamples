import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

// https://en.wikipedia.org/wiki/Continued_fraction

/**
 * A continued fraction evaluator that converts a sequence of integer
 * coefficients [a0; a1, a2, a3, ...] into successively more accurate
 * rational approximations (convergents).
 * <p>
 * This class is an {@link Iterator} decorator: give it any source of
 * integer coefficients, and it produces the sequence of {@link Fraction}
 * values that the continued fraction defines.
 * <p>
 * The implementation uses the classical "matrix" recurrence for
 * convergents, which is both faster and numerically more transparent
 * than the original "invert and add" approach:
 * <pre>
 *   h(n) = a(n) * h(n-1) + h(n-2)
 *   k(n) = a(n) * k(n-1) + k(n-2)
 * </pre>
 * Each convergent is then h(n)/k(n), already in lowest terms when
 * the coefficients are positive (which they are for standard continued
 * fraction expansions of real numbers).
 *
 * @author Ilkka Kokkarinen
 */
public class ContinuedFraction implements Iterator<Fraction> {

    private final Iterator<Integer> coefficients;
    private boolean first = true;

    // Matrix recurrence state: h/k tracks the current convergent,
    // hPrev/kPrev tracks the one before it.
    private BigInteger h = BigInteger.ONE,  k = BigInteger.ZERO;  // h(-1)/k(-1)
    private BigInteger hPrev = BigInteger.ZERO, kPrev = BigInteger.ONE; // h(-2)/k(-2)

    public ContinuedFraction(Iterator<Integer> coefficients) {
        this.coefficients = coefficients;
    }

    @Override
    public boolean hasNext() {
        return coefficients.hasNext();
    }

    @Override
    public Fraction next() {
        var a = BigInteger.valueOf(coefficients.next());
        // The standard convergent recurrence:
        //   h_n = a_n * h_{n-1} + h_{n-2}
        //   k_n = a_n * k_{n-1} + k_{n-2}
        var hNew = a.multiply(h).add(hPrev);
        var kNew = a.multiply(k).add(kPrev);
        hPrev = h; kPrev = k;
        h = hNew; k = kNew;
        return new Fraction(h, k);
    }

    // -----------------------------------------------------------------------
    // Factory methods for common continued fraction sequences.
    // -----------------------------------------------------------------------

    /**
     * Return an iterator over the continued fraction coefficients of the
     * golden ratio: [1; 1, 1, 1, ...]. The simplest possible continued
     * fraction — every coefficient is 1. The convergents are ratios of
     * consecutive Fibonacci numbers.
     *
     * @param terms how many coefficients to produce
     */
    public static Iterator<Integer> goldenRatioCoefficients(int terms) {
        return IntStream.generate(() -> 1).limit(terms).iterator();
    }

    /**
     * Return an iterator over the continued fraction coefficients of
     * sqrt(2): [1; 2, 2, 2, ...].
     *
     * @param terms how many coefficients to produce
     */
    public static Iterator<Integer> sqrt2Coefficients(int terms) {
        return IntStream.concat(
                IntStream.of(1),
                IntStream.generate(() -> 2).limit(terms - 1)
        ).iterator();
    }

    /**
     * Return an iterator over the continued fraction coefficients of
     * <em>e</em> (Euler's number): [2; 1, 2, 1, 1, 4, 1, 1, 6, ...].
     * The pattern after the initial 2 is: 1, 2k, 1 for k = 1, 2, 3, ...
     *
     * @param terms how many coefficients to produce
     */
    public static Iterator<Integer> eulerCoefficients(int terms) {
        // Euler's beautiful pattern: a(0)=2, then for n>=1,
        // a(n) = 2*(n+1)/3 when (n % 3 == 2), else 1.
        return IntStream.concat(
                IntStream.of(2),
                IntStream.range(1, terms).map(n ->
                        (n % 3 == 2) ? 2 * (n + 1) / 3 : 1
                )
        ).limit(terms).iterator();
    }

    /**
     * Evaluate a continued fraction to a {@link BigDecimal} with the
     * given precision. This is a convenience method that exhausts the
     * iterator and divides.
     *
     * @param coefficients the coefficient source
     * @param precision    number of decimal places
     * @return the value of the continued fraction
     */
    public static BigDecimal evaluate(Iterator<Integer> coefficients,
                                       int precision) {
        var cf = new ContinuedFraction(coefficients);
        Fraction convergent = new Fraction(0);
        while (cf.hasNext()) {
            convergent = cf.next();
        }
        return new BigDecimal(convergent.getNum())
                .divide(new BigDecimal(convergent.getDen()),
                        precision, RoundingMode.HALF_EVEN);
    }

    // -----------------------------------------------------------------------
    // Demos
    // -----------------------------------------------------------------------

    /**
     * Pretty-print a {@link BigDecimal} with a label, showing digits
     * grouped into lines of {@code perLine} characters.
     */
    private static void printDigits(String label, BigDecimal value,
                                     int totalDigits, int perLine) {
        String text = value.toPlainString();
        int dot = text.indexOf('.') + 1;
        System.out.println(label);
        System.out.println("Integer part: " + text.substring(0, dot - 1));
        System.out.println();
        for (int pos = dot; pos < text.length(); pos += perLine) {
            int end = Math.min(pos + perLine, text.length());
            System.out.println(text.substring(pos, end));
        }
        System.out.println();
    }

    /**
     * Demonstrate continued fraction computation of the golden ratio,
     * sqrt(2), and Euler's number <em>e</em>, each to 1000 decimal places.
     */
    public static void main(String[] args) {
        final int DIGITS = 1000;
        final int PER_LINE = 50;

        // Golden ratio: [1; 1, 1, 1, ...] — the slowest-converging
        // continued fraction (needs ~2300 terms for 1000 digits).
        var golden = evaluate(goldenRatioCoefficients(2300), DIGITS);
        printDigits("Golden ratio φ = (1 + √5) / 2", golden, DIGITS, PER_LINE);

        // sqrt(2): [1; 2, 2, 2, ...] — converges faster than golden ratio.
        var root2 = evaluate(sqrt2Coefficients(1500), DIGITS);
        printDigits("√2", root2, DIGITS, PER_LINE);

        // Euler's number: [2; 1, 2, 1, 1, 4, 1, 1, 6, ...] — converges
        // quite quickly thanks to the growing coefficients.
        var euler = evaluate(eulerCoefficients(3000), DIGITS);
        printDigits("Euler's number e", euler, DIGITS, PER_LINE);

        // Show convergents of the golden ratio to illustrate how the
        // approximations are ratios of consecutive Fibonacci numbers.
        System.out.println("First 15 convergents of the golden ratio:");
        var cf = new ContinuedFraction(goldenRatioCoefficients(15));
        int n = 0;
        while (cf.hasNext()) {
            Fraction f = cf.next();
            BigDecimal approx = new BigDecimal(f.getNum())
                    .divide(new BigDecimal(f.getDen()), 15, RoundingMode.HALF_EVEN);
            System.out.printf("  c(%2d) = %8s / %-8s ≈ %s%n",
                    n++, f.getNum(), f.getDen(), approx);
        }
    }
}
