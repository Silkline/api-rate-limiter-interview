package com.silkline.ratelimit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

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

    // --- HARD MODE: Concurrency safety ---
    // Skipped unless HARD_MODE is set (e.g. HARD_MODE=1 ./gradlew test). The naive
    // check-then-record pattern races between reading the count and recording
    // the request, letting more than the limit through under concurrency.

    /** Releases [threads] threads at once against the same user; returns how many were allowed. */
    private fun countConcurrentAllowed(user: String, threads: Int): Int {
        val start = CountDownLatch(1)
        val done = CountDownLatch(threads)
        val allowed = AtomicInteger()
        val errors = AtomicInteger()
        repeat(threads) {
            thread {
                try {
                    start.await()
                    if (RateLimiter.rateLimiter(user)) allowed.incrementAndGet()
                } catch (t: Throwable) {
                    errors.incrementAndGet()
                } finally {
                    done.countDown()
                }
            }
        }
        start.countDown()
        assertTrue(done.await(30, TimeUnit.SECONDS), "threads did not finish in time")
        assertEquals(0, errors.get(), "rate limiter threw under concurrency")
        return allowed.get()
    }

    /**
     * HARD MODE: 100 concurrent requests for one paid user — exactly PAID_LIMIT may succeed.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "HARD_MODE", matches = ".+")
    fun hardMode_paidUser_concurrentRequests() {
        val user = "paid-concurrent-1"
        RateLimiter.userTiers[user] = UserTier.PAID

        assertEquals(
            Constants.PAID_LIMIT,
            countConcurrentAllowed(user, 100),
            "expected exactly PAID_LIMIT allowed under concurrency",
        )
    }

    /**
     * HARD MODE: 100 concurrent requests for one free user — exactly FREE_LIMIT may succeed.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "HARD_MODE", matches = ".+")
    fun hardMode_freeUser_concurrentRequests() {
        val user = "free-concurrent-1"
        RateLimiter.userTiers[user] = UserTier.FREE

        assertEquals(
            Constants.FREE_LIMIT,
            countConcurrentAllowed(user, 100),
            "expected exactly FREE_LIMIT allowed under concurrency",
        )
    }
}
