package com.silkline.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API rate limiter.
 * <ul>
 *   <li>Free users: {@link Constants#FREE_LIMIT} requests total, ever (lifetime cap; never resets).</li>
 *   <li>Paid users: {@link Constants#PAID_LIMIT} requests per {@link Constants#WINDOW_SECONDS}-second window.</li>
 * </ul>
 * Implement {@link #rateLimiter(String)} below. Everything else is scaffolding used by the tests.
 */
public final class RateLimiter {

    private RateLimiter() {}

    /** Map from user ID to tier. Tests set this; the rate limiter reads it. */
    public static final Map<String, UserTier> USER_TIERS = new ConcurrentHashMap<>();

    /**
     * Returns true if the request is allowed, false if it is rate limited.
     *
     * TODO: implement per SPEC.md.
     *   - Look up the user's tier in USER_TIERS.
     *   - FREE: allow the first FREE_LIMIT requests ever, then always deny.
     *   - PAID: allow at most PAID_LIMIT requests per WINDOW_SECONDS-second window.
     * Keep per-user state in memory (e.g. static maps in this class).
     */
    public static boolean rateLimiter(String userId) {
        // Stub: replace with your implementation.
        return false;
    }
}
