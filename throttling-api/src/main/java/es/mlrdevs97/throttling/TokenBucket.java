package es.mlrdevs97.throttling;

/**
 * Implements the Token Bucket algorithm for rate limiting.
 */
public class TokenBucket {

    // The maximum number of tokens the bucket can hold.
    private final long CAPACITY;

    // The number of tokens added to the bucket per second.
    private final long REFILL_RATE;

    private long currentTokens;
    private long lastRefillTime;

    public TokenBucket(long capacity, long refillRate) {
        this.CAPACITY = capacity;
        this.REFILL_RATE = refillRate;
        this.currentTokens = capacity; // Start with a full bucket.
        this.lastRefillTime = System.currentTimeMillis();
    }

    /**
     * Attempts to consume a single token from the bucket.
     * @return true if a token was consumed (request allowed), false otherwise (request throttled).
     */
    public synchronized boolean tryConsume() {
        refill();
        if (currentTokens <= 0) {
            return false;
        }

        currentTokens--;
        return true;
    }

    public synchronized long getCurrentTokens() {
        refill();
        return currentTokens;
    }

    /**
     * Calculates and adds new tokens to the bucket based on the elapsed time.
     */
    private void refill() {
        long now = System.currentTimeMillis();
        long timeElapsed = now - lastRefillTime;
        if (timeElapsed <= 0) {
            return;
        }

        long tokensToAdd = (timeElapsed * REFILL_RATE) / 1000;
        if (tokensToAdd <= 0) {
            return;
        }

        this.currentTokens = Math.min(CAPACITY, this.currentTokens + tokensToAdd);
        this.lastRefillTime = now;
    }
}