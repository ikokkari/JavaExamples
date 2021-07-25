import java.util.*;
import java.math.*;

public class ShortHamming {

    private static final int HEAPSIZE = 10000000;
    
    private static final short[] heap = new short[HEAPSIZE * 3];
    private static int size = 0;
    private static int expensiveCount = 0;
    
    private static final BigInteger two = BigInteger.valueOf(2);
    private static final BigInteger three = BigInteger.valueOf(3);
    private static final BigInteger five = BigInteger.valueOf(5);
    
    private static final double logOf2 = Math.log(2);
    private static final double logOf3 = Math.log(3);
    private static final double logOf5 = Math.log(5);
    
    private static BigInteger getValue(int pos) {
        return two.pow(heap[pos]).multiply(three.pow(heap[pos+1])).multiply(five.pow(heap[pos+2]));
    }
    
    private static boolean lessThan(int pos1, int pos2) {
        assert pos1 != pos2;
        if(heap[pos1] == heap[pos2] && heap[pos1+1] == heap[pos2+1] && heap[pos1+2] == heap[pos2+2]) {
            return false;
        }
        if(heap[pos1] <= heap[pos2] && heap[pos1+1] <= heap[pos2+1] && heap[pos1+2] <= heap[pos2+2]) {
            return true;
        }
        if(heap[pos1] >= heap[pos2] && heap[pos1+1] >= heap[pos2+1] && heap[pos1+2] >= heap[pos2+2]) {
            return false;
        }
        double log1 = heap[pos1] * logOf2 + heap[pos1+1] * logOf3 + heap[pos1+2] * logOf5;
        double log2 = heap[pos2] * logOf2 + heap[pos2+1] * logOf3 + heap[pos2+2] * logOf5;
        if(Math.abs(log1 - log2) > 0.000000001) {
            return log1 < log2;
        }
        expensiveCount++;
        return getValue(pos1).compareTo(getValue(pos2)) < 0;
    }
    
    private static void push(short a, short b, short c) {
        int idx1 = ++size;
        int pos = 3 * size;
        heap[pos] = a; heap[pos + 1] = b; heap[pos + 2] = c; 
        while(idx1 > 1) {
            int idx2 = idx1 / 2;
            int pos1 = 3 * idx1;
            int pos2 = 3 * idx2;
            if(lessThan(pos1, pos2)) {
                short tmp = heap[pos1]; heap[pos1] = heap[pos2]; heap[pos2] = tmp;
                tmp = heap[pos1+1]; heap[pos1+1] = heap[pos2+1]; heap[pos2+1] = tmp;
                tmp = heap[pos1+2]; heap[pos1+2] = heap[pos2+2]; heap[pos2+2] = tmp;
                idx1 = idx2;
            }
            else { return; }            
        }
    }
    
    private static void extractMin() {
        int pos = 3 * size--;
        heap[3] = heap[pos]; heap[4] = heap[pos+1]; heap[5] = heap[pos+2];
        int idx = 1;
        while(2 * idx <= size) {
            pos = 3 * idx;
            int largest = 2 * idx;
            int posl = 3 * largest;
            int posr = 3 * (largest + 1);
            if(largest + 1 <= size) {
                if(lessThan(posr, posl)) { posl = posr; largest++; }                
            }
            if(lessThan(posl, pos)) {
                short tmp = heap[pos]; heap[pos] = heap[posl]; heap[posl] = tmp;
                tmp = heap[pos + 1]; heap[pos + 1] = heap[posl + 1]; heap[posl + 1] = tmp;
                tmp = heap[pos + 2]; heap[pos + 2] = heap[posl + 2]; heap[posl + 2] = tmp;
                idx = largest;
            }
            else { return; }
        }        
    }
    
    public static BigInteger hamming(long n) {
        int maxSize;
        size = maxSize = expensiveCount = 0;
        short a, b, c;
        long startTime = System.currentTimeMillis();
        push((short)0, (short)0, (short)0);
        for(long i = 1; i < n; i++) {
            a = heap[3]; b = heap[4]; c = heap[5];            
            if(i % 100000000 == 0) { 
                System.out.println(i + ": (" + a + ", " + b + ", " + c + ")");
            }
            extractMin();
            if(a == 0 && b == 0) {
                push(a, b, (short)(c + 1));
            }
            if(a == 0) {
                push(a, (short)(b + 1), c);
            }
            push((short)(a + 1), b, c);
            if(size > maxSize) { maxSize = size; }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Solution found in " + (endTime - startTime) + " millis.");
        System.out.println("Maximum heap size was " + maxSize + " using " + expensiveCount + " explicit comparisons.");
        System.out.println(getValue(3) + " (" + heap[3] + ", " + heap[4] + ", " + heap[5] + ")");
        return getValue(3);
    }
}
