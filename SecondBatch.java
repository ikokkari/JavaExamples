import java.util.Random;

public class SecondBatch {

    // In Lake Wobegon, all students are above average.
    public static int aboveAverage(double[] a) {
        if(a.length == 0) { return 0; }
        double sum = 0;
        for(double e: a) { sum += e; }
        double avg = sum / a.length;
        int count = 0;
        for(double e: a) {
            if(e > avg) { count++; }
        }
        return count;
    }

    // Simple counting problem.
    public static int hammingDistance(boolean[] a, boolean[] b) {
        int count = 0;
        for(int i = 0; i < a.length; i++) {
            if(a[i] != b[i]) { count++; }
        }
        return count;
    }

    // An important statistical quantity is surprisingly easy to compute.
    public static double variance(double[] a) {
        if(a.length == 0) { return 0; }
        double sum = 0, sqSum = 0;
        for(double e: a) {
            sum += e;
            sqSum += e*e;
        }
        double avg = sum / a.length;
        double sqAvg = sqSum / a.length;
        return sqAvg - avg * avg; // square root of this is standard deviation
    }

    // Evaluate the terms one by one.
    public static double eval(double[] coeff, double x) {
        double sum = 0;
        double pow = 1;
        // Two multiplications and one add per coefficient.
        for(double e: coeff) {
            sum += e * pow;
            pow = pow * x;
        }
        return sum;
    }

    // Horner's rule from inside out saves one multiplication per round.
    public static double evalHornerRule(double[] coeff, double x) {
        double sum = coeff[coeff.length - 1];
        // One multiplication and one add per coefficient.
        for(int i = coeff.length - 2; i >= 0; i--) {
            sum = sum * x + coeff[i];
        }
        // Also more numerically stable using floating point.
        return sum;
    }

    // Random sampling without replacement with the aid of boolean array.
    public static double[] sample(double[] a, int k, Random rng) {
        // Keep track of which elements have already been taken in.
        boolean[] alreadyTaken = new boolean[a.length];
        double[] b = new double[k];
        for(int i = 0; i < k; i++) {
            int j;
            do {
                j = rng.nextInt(a.length);
            } while(alreadyTaken[j]);
            alreadyTaken[j] = true;
            b[i] = a[j];
        }
        return b;
    }

    // Reservoir sampling, an online algorithm. Assumes k <= a.length.
    public static double[] reservoirSampling(double[] a, int k, Random rng) {
        double[] b = new double[k];
        // Establish the reservoir of first k elements.
        System.arraycopy(a, 0, b, 0, k);
        // Other elements may be swapped into the reservoir.
        for(int i = k; i < a.length; i++) {
            int j = rng.nextInt(i+1);
            if(j < k) {
                b[j] = a[i];
            }
        }
        return b;
    }

    // Showcases the idea of numerically simulating some process to
    // find out its average, when we don't know the analytical solution. 
    public static double couponCollector(int n, int trials, Random rng) {
        int total = 0;
        for(int i = 0; i < trials; i++) {
            boolean[] coupons = new boolean[n];
            int remain = n;
            while(remain > 0) {
                int c = rng.nextInt(n);
                if(!coupons[c]) { coupons[c] = true; remain--; }
                total++;
            }
        }
        return total / (double)trials;
    }
}