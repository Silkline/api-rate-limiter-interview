using System.Collections.Concurrent;

namespace ApiRateLimiter;

/// <summary>
/// API rate limiter.
/// <list type="bullet">
///   <item>Free users: <see cref="Constants.FreeLimit"/> requests total, ever (lifetime cap; never resets).</item>
///   <item>Paid users: <see cref="Constants.PaidLimit"/> requests per <see cref="Constants.WindowSeconds"/>-second window.</item>
/// </list>
/// Implement <see cref="AllowRequest"/> below. Everything else is scaffolding used by the tests.
/// (The method is not named RateLimiter because a member cannot share its enclosing type's name.)
/// </summary>
public static class RateLimiter
{
    /// <summary>Map from user ID to tier. Tests set this; the rate limiter reads it.</summary>
    public static readonly ConcurrentDictionary<string, UserTier> UserTiers = new();

    /// <summary>
    /// Returns true if the request is allowed, false if it is rate limited.
    ///
    /// TODO: implement per SPEC.md.
    ///   - Look up the user's tier in <see cref="UserTiers"/>.
    ///   - Free: allow the first FreeLimit requests ever, then always deny.
    ///   - Paid: allow at most PaidLimit requests per WindowSeconds-second window.
    /// Keep per-user state in memory (e.g. static dictionaries in this class).
    /// </summary>
    public static bool AllowRequest(string userId)
    {
        // Stub: replace with your implementation.
        return false;
    }
}
