package com.silkline.ratelimit

import java.util.concurrent.ConcurrentHashMap

/**
 * API rate limiter: free = FREE_LIMIT requests ever, paid = PAID_LIMIT per WINDOW_SECONDS.
 */
object RateLimiter {

    /**
     * Map from user ID to tier. Tests and callers set this; rate limiter reads it.
     */
    val userTiers = ConcurrentHashMap<String, UserTier>()

    /**
     * Returns true if the request is allowed, false if rate limited.
     * TODO: Implement per SPEC — free = lifetime cap of FREE_LIMIT, paid = PAID_LIMIT per WINDOW_SECONDS window.
     */
    @Suppress("UNUSED_PARAMETER")
    fun rateLimiter(userId: String): Boolean {
        // Stub: replace with your implementation
        return false
    }
}
