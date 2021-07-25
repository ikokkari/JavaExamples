import java.math.BigInteger;
import java.util.*;

/**
 * Compute the {code n}:th Hamming number by generating Hamming numbers in a priority queue.
 * @author Ilkka Kokkarinen
 */

public class BigHamming {

    private static final BigInteger[] muls = {
        BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(5)
    };
    
    /**
     * Compute the {@code n}:th Hamming number, with quiet operation.
     * @param n The index of the Hammin number to compute.
     */
    public BigInteger computeHamming(int n) {
        return computeHamming(n, false);
    }
    
    /**
     * Compute the {@code n}:th Hamming number.
     * @param n The index of the Hamming number to compute.
     * @param verbose Whether the method should output the number and timing statistics.
     * @return The {code n}:th Hamming number.
     */
    public static BigInteger computeHamming(int n, boolean verbose) {
        long startTime = System.currentTimeMillis();
        // The elements of the search frontier
        PriorityQueue<BigInteger> frontierQ = new PriorityQueue<>();
        HashSet<BigInteger> frontierS = new HashSet<>();
        
        // Initialize the frontier
        frontierQ.offer(BigInteger.ONE);
        frontierS.add(BigInteger.ONE);
        
        // Invariant: we have generated all Hamming numbers up to the smallest
        // element in the frontier.
        while(true) {
            // Pop out the next Hamming number from the frontier
            BigInteger curr = frontierQ.poll();
            frontierS.remove(curr);
            //System.out.println(curr);
            if(--n == 0) {
                if(verbose) {
                    System.out.println("Time: " + (System.currentTimeMillis() - startTime) + " ms");
                    System.out.println("Result: " + curr);
                }
                return curr;
            }
            // Generate the next three numbers to the search frontier
            for(BigInteger e : muls) {
                assert curr != null;
                BigInteger newB = curr.multiply(e);
                if(!frontierS.contains(newB)) {
                    frontierQ.offer(newB);
                    frontierS.add(newB);
                }
            }
        }        
    }
}