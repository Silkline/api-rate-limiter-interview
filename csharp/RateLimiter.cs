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

    private static readonly ConcurrentDictionary<string, List<long>> RequestTimestamps = new();

    private static UserTier GetTier(string userId)
    {
        return UserTiers.TryGetValue(userId, out var tier) ? tier : UserTier.Free;
    }

    /// <summary>
    /// Returns true if the request is allowed, false if rate limited.
    /// </summary>
    public static bool AllowRequest(string userId)
    {
        var tier = GetTier(userId);
        var now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var windowMs = Constants.WindowSeconds * 1000L;
        var cutoff = now - windowMs;

        var timestamps = RequestTimestamps.GetOrAdd(userId, _ => new List<long>());
        lock (timestamps)
        {
            if (tier == UserTier.Free)
            {
                if (timestamps.Count >= Constants.FreeLimit)
                    return false;
                timestamps.Add(now);
                return true;
            }

            // Paid: only count requests in current window
            var inWindow = timestamps.Count(t => t > cutoff);
            if (inWindow >= Constants.PaidLimit)
                return false;
            timestamps.Add(now);
            return true;
        }
    }
}
