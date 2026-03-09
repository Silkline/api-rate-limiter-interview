package com.silkline.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API rate limiter: free = FREE_LIMIT requests ever, paid = PAID_LIMIT per WINDOW_SECONDS.
 */
public final class RateLimiter {

    /**
     * Map from user ID to tier. Tests and callers set this; rate limiter reads it.
     */
    public static final Map<String, UserTier> USER_TIERS = new ConcurrentHashMap<>();

    /**
     * Returns true if the request is allowed, false if rate limited.
     * TODO: Implement per SPEC — free = lifetime cap of FREE_LIMIT, paid = PAID_LIMIT per WINDOW_SECONDS window.
     */
    public static boolean rateLimiter(String userId) {
        // Stub: replace with your implementation
        return false;
    }
}
