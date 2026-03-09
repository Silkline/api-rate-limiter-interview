using System.Collections.Concurrent;

namespace ApiRateLimiter;

/// <summary>
/// API rate limiter: free = FreeLimit requests ever, paid = PaidLimit per WindowSeconds.
/// </summary>
public static class RateLimiter
{
    /// <summary>
    /// Map from user ID to tier. Tests and callers set this; rate limiter reads it.
    /// </summary>
    public static readonly ConcurrentDictionary<string, UserTier> UserTiers = new();

    /// <summary>
    /// Returns true if the request is allowed, false if rate limited.
    /// TODO: Implement per SPEC — free = lifetime cap, paid = per-window limit.
    /// </summary>
    public static bool AllowRequest(string userId)
    {
        // Stub: replace with your implementation
        return false;
    }
}
