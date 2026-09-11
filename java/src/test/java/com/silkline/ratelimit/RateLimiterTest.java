package com.silkline.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.silkline.ratelimit.Constants.FREE_LIMIT;
import static com.silkline.ratelimit.Constants.PAID_LIMIT;
import static com.silkline.ratelimit.Constants.WINDOW_SECONDS;
import static com.silkline.ratelimit.RateLimiter.USER_TIERS;
import static com.silkline.ratelimit.RateLimiter.rateLimiter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for the API rate limiter. See SPEC.md section 3 for what each test verifies.
 *
 * <p>All expectations are derived from FREE_LIMIT / PAID_LIMIT / WINDOW_SECONDS, so changing
 * those constants in Constants.java changes what the tests expect.
 *
 * <p>Tests that wait for the paid window sleep for real, so the full suite takes roughly
 * 6 * (WINDOW_SECONDS + 1) seconds (about 36s with the defaults).
 */
class RateLimiterTest {

    /** Wait a little longer than the window so we are safely on the other side of it. */
    private static final long WAIT_SECONDS = WINDOW_SECONDS + 1L;

    private static void waitForWindow() throws InterruptedException {
        TimeUnit.SECONDS.sleep(WAIT_SECONDS);
    }

    private static void expectAllowed(String user, int count, String label) {
        for (int i = 0; i < count; i++) {
            assertTrue(rateLimiter(user), label + ": request " + (i + 1) + " of " + count + " should be allowed");
        }
    }

    private static void expectDenied(String user, String label) {
        assertFalse(rateLimiter(user), label + ": should be denied");
    }

    @BeforeEach
    void clearTiers() {
        USER_TIERS.clear();
    }

    /** Passes even with the unimplemented stub. If this fails, your environment is broken. */
    @Test
    void harnessSmoke() {
        assertTrue(FREE_LIMIT > 0);
        assertTrue(PAID_LIMIT > 0);
        assertTrue(WINDOW_SECONDS > 0);
        assertTrue(USER_TIERS.isEmpty());
        rateLimiter("smoke-user");
    }

    @Test
    void freeUser_allowsThenDenies() {
        String user = "free-user-1";
        USER_TIERS.put(user, UserTier.FREE);

        expectAllowed(user, FREE_LIMIT, "free");
        expectDenied(user, "request beyond FREE_LIMIT");
        expectDenied(user, "request beyond FREE_LIMIT");
    }

    @Test
    void freeUser_neverResets() throws InterruptedException {
        String user = "free-user-2";
        USER_TIERS.put(user, UserTier.FREE);

        expectAllowed(user, FREE_LIMIT, "free");
        expectDenied(user, "request beyond FREE_LIMIT");

        // Waiting past a paid window must NOT help a free user: the cap is for life.
        waitForWindow();
        expectDenied(user, "free cap must not reset after waiting");
        expectDenied(user, "free cap must not reset after waiting");
    }

    @Test
    void paidUser_allowsThenDenies() {
        String user = "paid-user-1";
        USER_TIERS.put(user, UserTier.PAID);

        expectAllowed(user, PAID_LIMIT, "paid");
        expectDenied(user, "request beyond PAID_LIMIT in the window");
    }

    @Test
    void paidUser_allowsAgainAfterWindow() throws InterruptedException {
        String user = "paid-user-2";
        USER_TIERS.put(user, UserTier.PAID);

        expectAllowed(user, PAID_LIMIT, "window 1");
        expectDenied(user, "window 1: beyond PAID_LIMIT");

        waitForWindow();

        expectAllowed(user, PAID_LIMIT, "after window");
        expectDenied(user, "after window: beyond PAID_LIMIT");
    }

    @Test
    void paidUser_singleRequestThenWindowExpiry() throws InterruptedException {
        String user = "paid-single-window";
        USER_TIERS.put(user, UserTier.PAID);

        expectAllowed(user, 1, "first request");
        waitForWindow();
        expectAllowed(user, PAID_LIMIT, "old request must have expired");
        expectDenied(user, "after window: beyond PAID_LIMIT");
    }

    @Test
    void paidUser_twoFullWindows() throws InterruptedException {
        String user = "paid-two-windows";
        USER_TIERS.put(user, UserTier.PAID);

        for (int w = 1; w <= 3; w++) {
            expectAllowed(user, PAID_LIMIT, "window " + w);
            expectDenied(user, "window " + w + ": beyond PAID_LIMIT");
            if (w < 3) waitForWindow();
        }
    }

    @Test
    void isolation_betweenFreeUsers() {
        String userA = "free-isolation-a";
        String userB = "free-isolation-b";
        USER_TIERS.put(userA, UserTier.FREE);
        USER_TIERS.put(userB, UserTier.FREE);

        expectAllowed(userA, FREE_LIMIT, "user A");
        expectDenied(userA, "user A beyond FREE_LIMIT");

        expectAllowed(userB, FREE_LIMIT, "user B must not be affected by user A");
        expectDenied(userB, "user B beyond FREE_LIMIT");
    }

    @Test
    void isolation_betweenPaidUsers() {
        String userA = "paid-isolation-a";
        String userB = "paid-isolation-b";
        USER_TIERS.put(userA, UserTier.PAID);
        USER_TIERS.put(userB, UserTier.PAID);

        expectAllowed(userA, PAID_LIMIT, "user A");
        expectDenied(userA, "user A beyond PAID_LIMIT");

        expectAllowed(userB, PAID_LIMIT, "user B must not be affected by user A");
        expectDenied(userB, "user B beyond PAID_LIMIT");
    }

    /**
     * EXTRA CREDIT: free user upgrades to paid.
     * After the upgrade, paid rules apply AND requests made while free still count toward the
     * current paid window, so the user does NOT get a fresh window just by upgrading.
     */
    @Test
    void extraCredit_freeUpgradesToPaid() throws InterruptedException {
        assumeTrue(PAID_LIMIT <= FREE_LIMIT, "upgrade test assumes PAID_LIMIT <= FREE_LIMIT");
        String user = "upgrade-user-1";
        USER_TIERS.put(user, UserTier.FREE);

        expectAllowed(user, PAID_LIMIT, "as free");

        USER_TIERS.put(user, UserTier.PAID);

        // Already made PAID_LIMIT requests inside this window -> denied.
        expectDenied(user, "past free requests must count toward the paid window");

        waitForWindow();

        expectAllowed(user, PAID_LIMIT, "after window");
        expectDenied(user, "after window: beyond PAID_LIMIT");
    }

    /**
     * EXTRA CREDIT (constants): the implementation must read Constants.FREE_LIMIT rather than
     * hard-coding 5. Java constants cannot be overridden at runtime, so this test only counts;
     * to really check, change FREE_LIMIT in Constants.java and re-run.
     */
    @Test
    void extraCredit_constantsRespected() {
        String user = "free-constants-check";
        USER_TIERS.put(user, UserTier.FREE);
        int allowed = 0;
        for (int i = 0; i < FREE_LIMIT + 2; i++) {
            if (rateLimiter(user)) allowed++;
        }
        assertEquals(FREE_LIMIT, allowed, "expected exactly FREE_LIMIT allowed");
    }
}
