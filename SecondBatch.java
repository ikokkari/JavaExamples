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

    // Reservoir sampling, an online algorithm. Assumes k >= a.length.
    public static double[] sampleOnline(double[] a, int k, Random rng) {
        double[] b = new double[k];
        // Establish the reservoir of first k elements.
        for(int i = 0; i < k; i++) {
            b[i] = a[i];
        }
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

    // The "Tukey's Ninther" problem to quickly find an estimate for
    // the median of a large dataset without sorting data. Taken from
    // https://www.johndcook.com/blog/2009/06/23/tukey-median-ninther/
    // For simplicy, assumes that array length is a power of 3.
    public static int tukeysNinther(int[] a) {
        while(a.length > 1) {
            int[] b = new int[a.length / 3];
            for(int i = 0; i < a.length; i += 3) {
                int x1 = a[i], x2 = a[i + 1], x3 = a[i + 2];
                if(x1 < x2 && x1 < x3) {
                    b[i / 3] = x2 < x3 ? x2: x3;
                }
                else if(x1 > x2 && x1 > x3) {
                    b[i / 3] = x2 < x3 ? x3: x2;
                }
                else { b[i / 3] = x1; }                
            }
            a = b;
        }
        return a[0];
    }
    
    // Utility method to simplify the next method.
    private static int locationOfMaximum(double[] a) {
        int m = 0;
        for(int i = 1; i < a.length; i++) {
            if(a[i] > a[m]) m = i;
        }
        return m;
    }
    
    // Apportion congressional seats between states using the Huntington-Hill method.
    public static int[] apportionCongress(int states, int seats, int[] pop) {
        int[] res = new int[states];
        double[] pq = new double[states];
        for(int state = 0; state < states; state++) {
            res[state] = 1; // One automatic seat per state
            pq[state] = pop[state] / Math.sqrt(2); // Initial priority quantity
        }
        seats -= states;
        while(seats > 0) { // Distribute the remaining seats
            int next = locationOfMaximum(pq); // Who gets the next seat?
            res[next]++;
            pq[next] = pop[next] / Math.sqrt(res[next] * (res[next]+1));
            seats--;
        }
        return res;
    }
}