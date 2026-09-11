package com.silkline.ratelimit

import com.silkline.ratelimit.Constants.FREE_LIMIT
import com.silkline.ratelimit.Constants.PAID_LIMIT
import com.silkline.ratelimit.Constants.WINDOW_SECONDS
import com.silkline.ratelimit.RateLimiter.rateLimiter
import com.silkline.ratelimit.RateLimiter.userTiers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

/**
 * Tests for the API rate limiter. See SPEC.md section 3 for what each test verifies.
 *
 * All expectations are derived from FREE_LIMIT / PAID_LIMIT / WINDOW_SECONDS, so changing
 * those constants in Constants.kt changes what the tests expect.
 *
 * Tests that wait for the paid window sleep for real, so the full suite takes roughly
 * 6 * (WINDOW_SECONDS + 1) seconds (about 36s with the defaults).
 */
class RateLimiterTest {

    /** Wait a little longer than the window so we are safely on the other side of it. */
    private val waitSeconds = WINDOW_SECONDS + 1L

    private fun waitForWindow() = TimeUnit.SECONDS.sleep(waitSeconds)

    private fun expectAllowed(user: String, count: Int, label: String) {
        repeat(count) { i ->
            assertTrue(rateLimiter(user), "$label: request ${i + 1} of $count should be allowed")
        }
    }

    private fun expectDenied(user: String, label: String) {
        assertFalse(rateLimiter(user), "$label: should be denied")
    }

    @BeforeEach
    fun clearTiers() {
        userTiers.clear()
    }

    /** Passes even with the unimplemented stub. If this fails, your environment is broken. */
    @Test
    fun harnessSmoke() {
        assertTrue(FREE_LIMIT > 0)
        assertTrue(PAID_LIMIT > 0)
        assertTrue(WINDOW_SECONDS > 0)
        assertTrue(userTiers.isEmpty())
        rateLimiter("smoke-user")
    }

    @Test
    fun freeUser_allowsThenDenies() {
        val user = "free-user-1"
        userTiers[user] = UserTier.FREE

        expectAllowed(user, FREE_LIMIT, "free")
        expectDenied(user, "request beyond FREE_LIMIT")
        expectDenied(user, "request beyond FREE_LIMIT")
    }

    @Test
    fun freeUser_neverResets() {
        val user = "free-user-2"
        userTiers[user] = UserTier.FREE

        expectAllowed(user, FREE_LIMIT, "free")
        expectDenied(user, "request beyond FREE_LIMIT")

        // Waiting past a paid window must NOT help a free user: the cap is for life.
        waitForWindow()
        expectDenied(user, "free cap must not reset after waiting")
        expectDenied(user, "free cap must not reset after waiting")
    }

    @Test
    fun paidUser_allowsThenDenies() {
        val user = "paid-user-1"
        userTiers[user] = UserTier.PAID

        expectAllowed(user, PAID_LIMIT, "paid")
        expectDenied(user, "request beyond PAID_LIMIT in the window")
    }

    @Test
    fun paidUser_allowsAgainAfterWindow() {
        val user = "paid-user-2"
        userTiers[user] = UserTier.PAID

        expectAllowed(user, PAID_LIMIT, "window 1")
        expectDenied(user, "window 1: beyond PAID_LIMIT")

        waitForWindow()

        expectAllowed(user, PAID_LIMIT, "after window")
        expectDenied(user, "after window: beyond PAID_LIMIT")
    }

    @Test
    fun paidUser_singleRequestThenWindowExpiry() {
        val user = "paid-single-window"
        userTiers[user] = UserTier.PAID

        expectAllowed(user, 1, "first request")
        waitForWindow()
        expectAllowed(user, PAID_LIMIT, "old request must have expired")
        expectDenied(user, "after window: beyond PAID_LIMIT")
    }

    @Test
    fun paidUser_twoFullWindows() {
        val user = "paid-two-windows"
        userTiers[user] = UserTier.PAID

        for (w in 1..3) {
            expectAllowed(user, PAID_LIMIT, "window $w")
            expectDenied(user, "window $w: beyond PAID_LIMIT")
            if (w < 3) waitForWindow()
        }
    }

    @Test
    fun isolation_betweenFreeUsers() {
        val userA = "free-isolation-a"
        val userB = "free-isolation-b"
        userTiers[userA] = UserTier.FREE
        userTiers[userB] = UserTier.FREE

        expectAllowed(userA, FREE_LIMIT, "user A")
        expectDenied(userA, "user A beyond FREE_LIMIT")

        expectAllowed(userB, FREE_LIMIT, "user B must not be affected by user A")
        expectDenied(userB, "user B beyond FREE_LIMIT")
    }

    @Test
    fun isolation_betweenPaidUsers() {
        val userA = "paid-isolation-a"
        val userB = "paid-isolation-b"
        userTiers[userA] = UserTier.PAID
        userTiers[userB] = UserTier.PAID

        expectAllowed(userA, PAID_LIMIT, "user A")
        expectDenied(userA, "user A beyond PAID_LIMIT")

        expectAllowed(userB, PAID_LIMIT, "user B must not be affected by user A")
        expectDenied(userB, "user B beyond PAID_LIMIT")
    }

    /**
     * EXTRA CREDIT: free user upgrades to paid.
     * After the upgrade, paid rules apply AND requests made while free still count toward the
     * current paid window, so the user does NOT get a fresh window just by upgrading.
     */
    @Test
    fun extraCredit_freeUpgradesToPaid() {
        assumeTrue(PAID_LIMIT <= FREE_LIMIT, "upgrade test assumes PAID_LIMIT <= FREE_LIMIT")
        val user = "upgrade-user-1"
        userTiers[user] = UserTier.FREE

        expectAllowed(user, PAID_LIMIT, "as free")

        userTiers[user] = UserTier.PAID

        // Already made PAID_LIMIT requests inside this window -> denied.
        expectDenied(user, "past free requests must count toward the paid window")

        waitForWindow()

        expectAllowed(user, PAID_LIMIT, "after window")
        expectDenied(user, "after window: beyond PAID_LIMIT")
    }

    /**
     * EXTRA CREDIT (constants): the implementation must read Constants.FREE_LIMIT rather than
     * hard-coding 5. Kotlin `const val` cannot be overridden at runtime, so this test only counts;
     * to really check, change FREE_LIMIT in Constants.kt and re-run.
     */
    @Test
    fun extraCredit_constantsRespected() {
        val user = "free-constants-check"
        userTiers[user] = UserTier.FREE
        var allowed = 0
        repeat(FREE_LIMIT + 2) {
            if (rateLimiter(user)) allowed++
        }
        assertEquals(FREE_LIMIT, allowed, "expected exactly FREE_LIMIT allowed")
    }
}
