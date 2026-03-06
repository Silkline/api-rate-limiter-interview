namespace ApiRateLimiter;

/// <summary>
/// Rate limiter constants. Referenced by implementation and tests.
/// </summary>
public static class Constants
{
    /// <summary>Total requests a free user is allowed ever (lifetime cap).</summary>
    public const int FreeLimit = 5;

    /// <summary>Max requests a paid user gets per time window.</summary>
    public const int PaidLimit = 2;

    /// <summary>Window length in seconds for paid users.</summary>
    public const int WindowSeconds = 5;
}
