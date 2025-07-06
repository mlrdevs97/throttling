package es.mlrdevs97.throttling;
/**
 * Implements the Leaky Bucket algorithm for rate limiting.
 */
public class LeakyBucket {
    // The maximum number of requests the bucket (queue) can hold.
    private final long CAPACITY;

    // The rate at which requests are processed (leaked) per second.
    private final long LEAK_RATE;

    private long currentSize;
    private long lastLeakTime;

    public LeakyBucket(long CAPACITY, long LEAK_RATE) {
        this.CAPACITY = CAPACITY;
        this.LEAK_RATE = LEAK_RATE;
        this.currentSize = 0; // Start with an empty bucket.
        this.lastLeakTime = System.currentTimeMillis();
    }

    /**
     * Attempts to add a request to the bucket.
     * @return true if the request is accepted (added to the queue), false otherwise (request dropped).
     */
    public synchronized boolean tryAdd() {
        leak();
        if (currentSize >= CAPACITY) {
            return false;
        }

        currentSize++;
        return true;
    }

    /**
     * Gets the current number of requests in the bucket after accounting for any leaks.
     * @return The current size of the bucket.
     */
    public synchronized long getCurrentSize() {
        leak();
        return currentSize;
    }

    /**
     * Calculates and removes requests from the bucket that have "leaked" out over time.
     */
    private void leak() {
        long now = System.currentTimeMillis();
        long timeElapsed = now - lastLeakTime;
        if (timeElapsed <= 0) {
            return;
        }

        long leakRequests = timeElapsed * LEAK_RATE / 1000;
        if (leakRequests <= 0) {
            return;
        }

        currentSize = Math.max(0, currentSize - leakRequests);
        lastLeakTime = now;
    }
}