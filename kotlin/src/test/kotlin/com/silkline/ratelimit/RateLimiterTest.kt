package com.silkline.ratelimit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class RateLimiterTest {

    @BeforeEach
    fun clearTiers() {
        RateLimiter.userTiers.clear()
    }

    @Test
    fun freeUser_allowsThenDenies() {
        val user = "free-user-1"
        RateLimiter.userTiers[user] = UserTier.FREE

        repeat(Constants.FREE_LIMIT) { i ->
            assertTrue(RateLimiter.rateLimiter(user), "request ${i + 1}")
        }
        assertFalse(RateLimiter.rateLimiter(user))
        assertFalse(RateLimiter.rateLimiter(user))
    }

    @Test
    fun freeUser_neverResets() {
        val user = "free-user-2"
        RateLimiter.userTiers[user] = UserTier.FREE

        repeat(Constants.FREE_LIMIT) {
            RateLimiter.rateLimiter(user)
        }
        assertFalse(RateLimiter.rateLimiter(user))
        assertFalse(RateLimiter.rateLimiter(user))
    }

    @Test
    fun paidUser_allowsThenDenies() {
        val user = "paid-user-1"
        RateLimiter.userTiers[user] = UserTier.PAID

        assertTrue(RateLimiter.rateLimiter(user))
        assertTrue(RateLimiter.rateLimiter(user))
        assertFalse(RateLimiter.rateLimiter(user))
    }

    @Test
    fun paidUser_allowsAgainAfterWindow() {
        val user = "paid-user-2"
        RateLimiter.userTiers[user] = UserTier.PAID

        RateLimiter.rateLimiter(user)
        RateLimiter.rateLimiter(user)
        assertFalse(RateLimiter.rateLimiter(user))

        TimeUnit.SECONDS.sleep(Constants.WINDOW_SECONDS + 1L)

        assertTrue(RateLimiter.rateLimiter(user))
        assertTrue(RateLimiter.rateLimiter(user))
        assertFalse(RateLimiter.rateLimiter(user))
    }

    @Test
    fun isolationBetweenUsers() {
        val userA = "free-isolation-a"
        val userB = "free-isolation-b"
        RateLimiter.userTiers[userA] = UserTier.FREE
        RateLimiter.userTiers[userB] = UserTier.FREE

        repeat(3) { RateLimiter.rateLimiter(userA) }
        repeat(Constants.FREE_LIMIT) { assertTrue(RateLimiter.rateLimiter(userB)) }
        assertFalse(RateLimiter.rateLimiter(userB))
    }

    @Test
    fun paidUser_singleRequestThenWindowExpiry() {
        val user = "paid-single-window"
        RateLimiter.userTiers[user] = UserTier.PAID

        assertTrue(RateLimiter.rateLimiter(user))
        TimeUnit.SECONDS.sleep(Constants.WINDOW_SECONDS + 1L)
        assertTrue(RateLimiter.rateLimiter(user))
        assertTrue(RateLimiter.rateLimiter(user))
        assertFalse(RateLimiter.rateLimiter(user))
    }

    @Test
    fun paidUser_twoFullWindows() {
        val user = "paid-two-windows"
        RateLimiter.userTiers[user] = UserTier.PAID

        assertTrue(RateLimiter.rateLimiter(user))
        assertTrue(RateLimiter.rateLimiter(user))
        assertFalse(RateLimiter.rateLimiter(user))
        TimeUnit.SECONDS.sleep(Constants.WINDOW_SECONDS + 1L)
        assertTrue(RateLimiter.rateLimiter(user))
        assertTrue(RateLimiter.rateLimiter(user))
        assertFalse(RateLimiter.rateLimiter(user))
        TimeUnit.SECONDS.sleep(Constants.WINDOW_SECONDS + 1L)
        assertTrue(RateLimiter.rateLimiter(user))
        assertTrue(RateLimiter.rateLimiter(user))
        assertFalse(RateLimiter.rateLimiter(user))
    }

    /**
     * EXTRA CREDIT: Free user upgrades to paid.
     * After upgrade, paid rules apply and past requests (made when free) must count
     * toward the paid 2-per-window limit, so the user does NOT get a fresh paid window.
     */
    @Test
    fun extraCredit_freeUpgradesToPaid() {
        val user = "upgrade-user-1"
        RateLimiter.userTiers[user] = UserTier.FREE

        assertTrue(RateLimiter.rateLimiter(user))
        assertTrue(RateLimiter.rateLimiter(user))

        RateLimiter.userTiers[user] = UserTier.PAID

        // Paid limit is 2 per window; we already have 2 in this window
        assertFalse(RateLimiter.rateLimiter(user))

        TimeUnit.SECONDS.sleep(Constants.WINDOW_SECONDS + 1L)

        assertTrue(RateLimiter.rateLimiter(user))
        assertTrue(RateLimiter.rateLimiter(user))
        assertFalse(RateLimiter.rateLimiter(user))
    }

    /**
     * EXTRA CREDIT (constants): Implementation must use Constants.FREE_LIMIT.
     * Change the value in Constants.kt and re-run tests; behavior should match.
     */
    @Test
    fun extraCredit_constantsRespected() {
        val user = "free-constants-check"
        RateLimiter.userTiers[user] = UserTier.FREE
        var allowed = 0
        repeat(Constants.FREE_LIMIT + 2) {
            if (RateLimiter.rateLimiter(user)) allowed++
        }
        assertEquals(Constants.FREE_LIMIT, allowed)
    }
}
