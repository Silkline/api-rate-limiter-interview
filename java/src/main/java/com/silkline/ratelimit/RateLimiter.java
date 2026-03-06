package com.silkline.ratelimit;

import java.util.ArrayList;
import java.util.List;
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

    private static final Map<String, List<Long>> REQUEST_TIMESTAMPS = new ConcurrentHashMap<>();

    private static UserTier getTier(String userId) {
        return USER_TIERS.getOrDefault(userId, UserTier.FREE);
    }

    /**
     * Returns true if the request is allowed, false if rate limited.
     */
    public static boolean rateLimiter(String userId) {
        UserTier tier = getTier(userId);
        long now = System.currentTimeMillis();
        long windowMs = Constants.WINDOW_SECONDS * 1000L;
        long cutoff = now - windowMs;

        List<Long> timestamps = REQUEST_TIMESTAMPS.computeIfAbsent(userId, k -> new ArrayList<>());
        synchronized (timestamps) {
            if (tier == UserTier.FREE) {
                if (timestamps.size() >= Constants.FREE_LIMIT) {
                    return false;
                }
                timestamps.add(now);
                return true;
            }

            // PAID: only count requests in current window
            long inWindow = timestamps.stream().filter(t -> t > cutoff).count();
            if (inWindow >= Constants.PAID_LIMIT) {
                return false;
            }
            timestamps.add(now);
            return true;
        }
    }
}
