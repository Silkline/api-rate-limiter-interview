package com.silkline.ratelimit

import java.util.concurrent.ConcurrentHashMap

/**
 * API rate limiter.
 * - Free users: [Constants.FREE_LIMIT] requests total, ever (lifetime cap; never resets).
 * - Paid users: [Constants.PAID_LIMIT] requests per [Constants.WINDOW_SECONDS]-second window.
 *
 * Implement [rateLimiter] below. Everything else is scaffolding used by the tests.
 */
object RateLimiter {

    /** Map from user ID to tier. Tests set this; the rate limiter reads it. */
    val userTiers = ConcurrentHashMap<String, UserTier>()

    /**
     * Returns true if the request is allowed, false if it is rate limited.
     *
     * TODO: implement per SPEC.md.
     *   - Look up the user's tier in [userTiers].
     *   - FREE: allow the first FREE_LIMIT requests ever, then always deny.
     *   - PAID: allow at most PAID_LIMIT requests per WINDOW_SECONDS-second window.
     * Keep per-user state in memory (e.g. maps in this object).
     */
    @Suppress("UNUSED_PARAMETER")
    fun rateLimiter(userId: String): Boolean {
        // Stub: replace with your implementation.
        return false
    }
}
