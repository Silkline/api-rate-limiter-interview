package com.silkline.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

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
}
