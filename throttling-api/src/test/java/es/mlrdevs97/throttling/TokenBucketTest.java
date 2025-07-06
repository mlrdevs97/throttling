package es.mlrdevs97.throttling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketTest {
    private TokenBucket tokenBucket;
    private final long CAPACITY = 5;
    private final long REFILL_RATE = 1;

    @BeforeEach
    void setUp() {
        tokenBucket = new TokenBucket(CAPACITY, REFILL_RATE);
    }

    @Test
    void whenNewBucketIsCreated_thenItIsFull() {
        // Arrange (handled in setUp)

        // Act
        long currentTokens = tokenBucket.getCurrentTokens();

        // Assert
        assertEquals(CAPACITY, currentTokens);
    }

    @Test
    void whenTokensAreConsumedWithinCapacity_thenConsumptionSucceeds() {
        // Arrange (handled in setUp)

        // Act & Assert
        for (int i = 0; i < CAPACITY; i++) {
            assertTrue(tokenBucket.tryConsume());
            assertEquals(CAPACITY - (i + 1), tokenBucket.getCurrentTokens());
        }
    }

    @Test
    void whenAllTokensAreConsumed_thenFurtherConsumptionFails() {
        // Arrange: Consume all available tokens
        for (int i = 0; i < CAPACITY; i++) {
            tokenBucket.tryConsume();
        }

        // Act
        boolean result = tokenBucket.tryConsume();

        // Assert
        assertFalse(result);
        assertEquals(0, tokenBucket.getCurrentTokens());
    }

    @Test
    void whenBucketIsEmpty_thenItRefillsOverTime() throws InterruptedException {
        // Arrange: Consume all tokens
        for (int i = 0; i < CAPACITY; i++) {
            tokenBucket.tryConsume();
        }
        assertEquals(0, tokenBucket.getCurrentTokens());

        // Act & Assert: Wait for one token to refill
        Thread.sleep(REFILL_RATE * 1000);
        assertEquals(1, tokenBucket.getCurrentTokens());
        assertTrue(tokenBucket.tryConsume());

        // Act & Assert: Wait for the bucket to be full again
        Thread.sleep(CAPACITY * REFILL_RATE * 1000);
        assertEquals(CAPACITY, tokenBucket.getCurrentTokens());
        assertTrue(tokenBucket.tryConsume());
    }

    @Test
    void whenBucketRefills_thenItDoesNotExceedCapacity() throws InterruptedException {
        // Arrange: Consume all tokens and wait for longer than needed to refill
        for (int i = 0; i < CAPACITY; i++) {
            tokenBucket.tryConsume();
        }
        Thread.sleep((CAPACITY + 2) * 1000);

        // Act
        long currentTokens = tokenBucket.getCurrentTokens();

        // Assert
        assertEquals(CAPACITY, currentTokens);
    }

    @Test
    void whenMultipleThreadsConsumeTokens_thenAccessIsHandledSafely() throws InterruptedException {
        // Arrange
        final int numThreads = 5;
        final int requestsPerThread = (int) (CAPACITY * 2);
        final int[] grantedAccess = {0};
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    if (tokenBucket.tryConsume()) {
                        synchronized (grantedAccess) {
                            grantedAccess[0]++;
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        // Act
        long testStart = System.currentTimeMillis();
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        long testEnd = System.currentTimeMillis();
        long timeElapsed = testEnd - testStart;

        // Assert
        assertTrue(grantedAccess[0] >= 0);
        assertTrue(grantedAccess[0] <= (REFILL_RATE * timeElapsed));
    }
}