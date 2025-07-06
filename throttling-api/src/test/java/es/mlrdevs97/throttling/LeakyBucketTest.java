package es.mlrdevs97.throttling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LeakyBucketTest {
    private final long CAPACITY = 5;
    private final long LEAK_RATE = 2;
    private LeakyBucket leakyBucket;

    @BeforeEach
    void SetUp() {
        this.leakyBucket = new LeakyBucket(CAPACITY, LEAK_RATE);
    }

    @Test
    void whenNoPreviousRequestsPerformed_thenBucketShouldBeEmpty() {
        // Arrange

        // Act
        long currentSize = leakyBucket.getCurrentSize();

        // Assert
        assertEquals(currentSize, 0);
    }

    @Test
    void whenBucketIsNotFull_thenShouldAllowRequests() {
        // Arrange

        // Act
        boolean allAccepted = IntStream.range(0, (int) CAPACITY)
                .mapToObj(i -> leakyBucket.tryAdd())
                .allMatch(result -> result);
        long currentSize = leakyBucket.getCurrentSize();

        // Assert
        assertTrue(allAccepted);
        assertEquals(currentSize, CAPACITY);
    }

    @Test
    void whenBucketIsFull_thenShouldNotAllowRequests() {
        // Arrange

        // Act
        for (int i = 0; i < CAPACITY; i++) {
            leakyBucket.tryAdd();
        }

        // Assert
        assertFalse(leakyBucket.tryAdd());
        assertEquals(leakyBucket.getCurrentSize(), CAPACITY);
    }

    @Test
    void whenBucketIsNotEmpty_thenShouldLeakOverTime() throws InterruptedException {
        // Arrange

        // Act
        for (int i = 0; i < CAPACITY; i++) {
            leakyBucket.tryAdd();
        }

        Thread.sleep(LEAK_RATE * 1000);

        // Assert
        assertTrue(leakyBucket.tryAdd());
    }

    @Test
    void whenEnoughTimePass_thenBucketShouldBeEmptied() throws InterruptedException {
        // Arrange

        // Act
        for (int i = 0; i < CAPACITY; i++) {
            leakyBucket.tryAdd();
        }

        Thread.sleep(CAPACITY / LEAK_RATE * 1000);

        // Assert
        assertEquals(leakyBucket.getCurrentSize(), 1);
    }

    @Test
    void whenMultipleThreadsAddRequests_thenAccessIsHandledSafely() throws InterruptedException {
        final int numThreads = 5;
        final int numRequests = 10;

        Thread[] threads = new Thread[numThreads];
        int[] grantedRequest = {0};

        for (int i = 0; i < numThreads; i++) {
            Thread thread = threads[i];
            threads[i] = new Thread(() -> {
                for (int j = 0; j < numRequests; j++) {
                    if (leakyBucket.tryAdd()) {
                        synchronized(grantedRequest) {
                            grantedRequest[0]++;
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

        long start = System.currentTimeMillis();

        for (Thread thread: threads) {
            thread.start();
        }
        for (Thread thread: threads) {
            thread.join();
        }

        long end = System.currentTimeMillis();
        long timeElapsed = end - start;

        // Assert
        assertTrue(grantedRequest[0] > 0);
        assertTrue(grantedRequest[0] < LEAK_RATE * timeElapsed);
    }
}
