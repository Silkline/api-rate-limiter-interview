package com.silkline.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @BeforeEach
    void clearTiers() {
        RateLimiter.USER_TIERS.clear();
    }

    @Test
    void freeUser_allowsThenDenies() {
        String user = "free-user-1";
        RateLimiter.USER_TIERS.put(user, UserTier.FREE);

        for (int i = 0; i < Constants.FREE_LIMIT; i++) {
            assertTrue(RateLimiter.rateLimiter(user), "request " + (i + 1));
        }
        assertFalse(RateLimiter.rateLimiter(user));
        assertFalse(RateLimiter.rateLimiter(user));
    }

    @Test
    void freeUser_neverResets() {
        String user = "free-user-2";
        RateLimiter.USER_TIERS.put(user, UserTier.FREE);

        for (int i = 0; i < Constants.FREE_LIMIT; i++) {
            RateLimiter.rateLimiter(user);
        }
        assertFalse(RateLimiter.rateLimiter(user));
        assertFalse(RateLimiter.rateLimiter(user));
    }

    @Test
    void paidUser_allowsThenDenies() {
        String user = "paid-user-1";
        RateLimiter.USER_TIERS.put(user, UserTier.PAID);

        assertTrue(RateLimiter.rateLimiter(user));
        assertTrue(RateLimiter.rateLimiter(user));
        assertFalse(RateLimiter.rateLimiter(user));
    }

    @Test
    void paidUser_allowsAgainAfterWindow() throws InterruptedException {
        String user = "paid-user-2";
        RateLimiter.USER_TIERS.put(user, UserTier.PAID);

        RateLimiter.rateLimiter(user);
        RateLimiter.rateLimiter(user);
        assertFalse(RateLimiter.rateLimiter(user));

        TimeUnit.SECONDS.sleep(Constants.WINDOW_SECONDS + 1);

        assertTrue(RateLimiter.rateLimiter(user));
        assertTrue(RateLimiter.rateLimiter(user));
        assertFalse(RateLimiter.rateLimiter(user));
    }

    @Test
    void isolationBetweenUsers() {
        String userA = "free-isolation-a";
        String userB = "free-isolation-b";
        RateLimiter.USER_TIERS.put(userA, UserTier.FREE);
        RateLimiter.USER_TIERS.put(userB, UserTier.FREE);

        for (int i = 0; i < 3; i++) {
            assertTrue(RateLimiter.rateLimiter(userA), "user A request " + (i + 1));
        }
        for (int i = 0; i < Constants.FREE_LIMIT; i++) {
            assertTrue(RateLimiter.rateLimiter(userB), "user B request " + (i + 1));
        }
        assertFalse(RateLimiter.rateLimiter(userB));
    }

    @Test
    void paidUser_singleRequestThenWindowExpiry() throws InterruptedException {
        String user = "paid-single-window";
        RateLimiter.USER_TIERS.put(user, UserTier.PAID);

        assertTrue(RateLimiter.rateLimiter(user));
        TimeUnit.SECONDS.sleep(Constants.WINDOW_SECONDS + 1);
        assertTrue(RateLimiter.rateLimiter(user));
        assertTrue(RateLimiter.rateLimiter(user));
        assertFalse(RateLimiter.rateLimiter(user));
    }

    @Test
    void paidUser_twoFullWindows() throws InterruptedException {
        String user = "paid-two-windows";
        RateLimiter.USER_TIERS.put(user, UserTier.PAID);

        assertTrue(RateLimiter.rateLimiter(user));
        assertTrue(RateLimiter.rateLimiter(user));
        assertFalse(RateLimiter.rateLimiter(user));
        TimeUnit.SECONDS.sleep(Constants.WINDOW_SECONDS + 1);
        assertTrue(RateLimiter.rateLimiter(user));
        assertTrue(RateLimiter.rateLimiter(user));
        assertFalse(RateLimiter.rateLimiter(user));
        TimeUnit.SECONDS.sleep(Constants.WINDOW_SECONDS + 1);
        assertTrue(RateLimiter.rateLimiter(user));
        assertTrue(RateLimiter.rateLimiter(user));
        assertFalse(RateLimiter.rateLimiter(user));
    }

    /**
     * EXTRA CREDIT: Free user upgrades to paid.
     * After upgrade, paid rules apply and past requests (made when free) must count
     * toward the paid 2-per-window limit, so the user does NOT get a fresh paid window.
     */
    @Test
    void extraCredit_freeUpgradesToPaid() throws InterruptedException {
        String user = "upgrade-user-1";
        RateLimiter.USER_TIERS.put(user, UserTier.FREE);

        assertTrue(RateLimiter.rateLimiter(user));
        assertTrue(RateLimiter.rateLimiter(user));

        RateLimiter.USER_TIERS.put(user, UserTier.PAID);

        // Paid limit is 2 per window; we already have 2 in this window
        assertFalse(RateLimiter.rateLimiter(user));

        TimeUnit.SECONDS.sleep(Constants.WINDOW_SECONDS + 1);

        assertTrue(RateLimiter.rateLimiter(user));
        assertTrue(RateLimiter.rateLimiter(user));
        assertFalse(RateLimiter.rateLimiter(user));
    }

    /**
     * EXTRA CREDIT (constants): Implementation must use Constants.FREE_LIMIT.
     * Change the value in Constants.java and re-run tests; behavior should match.
     */
    @Test
    void extraCredit_constantsRespected() {
        String user = "free-constants-check";
        RateLimiter.USER_TIERS.put(user, UserTier.FREE);
        int allowed = 0;
        for (int i = 0; i < Constants.FREE_LIMIT + 2; i++) {
            if (RateLimiter.rateLimiter(user)) allowed++;
        }
        assertEquals(Constants.FREE_LIMIT, allowed, "expected exactly FREE_LIMIT allowed");
    }

    // --- HARD MODE: Concurrency safety ---
    // Skipped unless HARD_MODE is set (e.g. HARD_MODE=1 mvn test). The naive
    // check-then-record pattern races between reading the count and recording
    // the request, letting more than the limit through under concurrency.

    /** Releases {@code threads} threads at once against the same user; returns how many were allowed. */
    private int countConcurrentAllowed(String user, int threads) throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger allowed = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    if (RateLimiter.rateLimiter(user)) allowed.incrementAndGet();
                } catch (Throwable t) {
                    errors.incrementAndGet();
                } finally {
                    done.countDown();
                }
            }).start();
        }
        start.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS), "threads did not finish in time");
        assertEquals(0, errors.get(), "rate limiter threw under concurrency");
        return allowed.get();
    }

    /**
     * HARD MODE: 100 concurrent requests for one paid user — exactly PAID_LIMIT may succeed.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "HARD_MODE", matches = ".+")
    void hardMode_paidUser_concurrentRequests() throws InterruptedException {
        String user = "paid-concurrent-1";
        RateLimiter.USER_TIERS.put(user, UserTier.PAID);

        assertEquals(Constants.PAID_LIMIT, countConcurrentAllowed(user, 100),
                "expected exactly PAID_LIMIT allowed under concurrency");
    }

    /**
     * HARD MODE: 100 concurrent requests for one free user — exactly FREE_LIMIT may succeed.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "HARD_MODE", matches = ".+")
    void hardMode_freeUser_concurrentRequests() throws InterruptedException {
        String user = "free-concurrent-1";
        RateLimiter.USER_TIERS.put(user, UserTier.FREE);

        assertEquals(Constants.FREE_LIMIT, countConcurrentAllowed(user, 100),
                "expected exactly FREE_LIMIT allowed under concurrency");
    }
}
