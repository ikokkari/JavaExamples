import java.util.concurrent.locks.*;

// A standard exercise of Concurrency Intro is to prove that Semaphores are
// equivalent to Locks and Condition variables in power, shown by implementing
// each one with the other. Here, a simple Semaphore implementation.

public class MySemaphore {
    private int permits;
    private Lock mutex;
    private Condition permitAvailable;
    
    public MySemaphore(int permits, boolean fair) {
        this.permits = permits;
        this.mutex = new ReentrantLock(fair);
        this.permitAvailable = mutex.newCondition();
    }
    
    public void acquire() throws InterruptedException {
        mutex.lock();
        try {
            while(permits < 1) {
                // Wait for one permit to become available.
                permitAvailable.await();
            }
            assert permits > 0;
            permits--;
        }
        // Ensure that mutex is unlocked even if an exception is thrown.
        finally { mutex.unlock(); }
    }
    
    public void release() {
        mutex.lock();
        permits++;
        // One waiting thread can now acquire a permit.
        if(permits > 0) { permitAvailable.signal(); }
        mutex.unlock();
    }
}

// Puzzle: assuming that the Lock mutex is FIFO, is this Semaphore also FIFO in that
// the threads that call acquire are always guaranteed to get the permit in the order
// in which they made these calls?

// Another puzzle: what would happen if you commented out the mutex operations from
// the release method? Devise a scenario where the semaphore would no longer work
// correctly. 