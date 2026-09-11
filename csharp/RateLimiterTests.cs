using Xunit;
using ApiRateLimiter;
using static ApiRateLimiter.Constants;
using static ApiRateLimiter.RateLimiter;

namespace ApiRateLimiterTests;

/// <summary>
/// Tests for the API rate limiter. See SPEC.md section 3 for what each test verifies.
///
/// All expectations are derived from FreeLimit / PaidLimit / WindowSeconds, so changing
/// those constants in Constants.cs changes what the tests expect.
///
/// Tests that wait for the paid window sleep for real, so the full suite takes roughly
/// 6 * (WindowSeconds + 1) seconds (about 36s with the defaults).
/// </summary>
public class RateLimiterTests : IDisposable
{
    /// <summary>Wait a little longer than the window so we are safely on the other side of it.</summary>
    private static readonly TimeSpan Wait = TimeSpan.FromSeconds(WindowSeconds + 1);

    // xUnit creates a new instance per test; clear shared state before and after each one.
    public RateLimiterTests() => UserTiers.Clear();
    public void Dispose() => UserTiers.Clear();

    private static void ExpectAllowed(string user, int count, string label)
    {
        for (var i = 0; i < count; i++)
            Assert.True(AllowRequest(user), $"{label}: request {i + 1} of {count} should be allowed");
    }

    private static void ExpectDenied(string user, string label)
        => Assert.False(AllowRequest(user), $"{label}: should be denied");

    /// <summary>Passes even with the unimplemented stub. If this fails, your environment is broken.</summary>
    [Fact]
    public void HarnessSmoke()
    {
        Assert.True(FreeLimit > 0);
        Assert.True(PaidLimit > 0);
        Assert.True(WindowSeconds > 0);
        Assert.Empty(UserTiers);
        _ = AllowRequest("smoke-user");
    }

    [Fact]
    public void FreeUser_AllowsThenDenies()
    {
        var user = "free-user-1";
        UserTiers[user] = UserTier.Free;

        ExpectAllowed(user, FreeLimit, "free");
        ExpectDenied(user, "request beyond FreeLimit");
        ExpectDenied(user, "request beyond FreeLimit");
    }

    [Fact]
    public void FreeUser_NeverResets()
    {
        var user = "free-user-2";
        UserTiers[user] = UserTier.Free;

        ExpectAllowed(user, FreeLimit, "free");
        ExpectDenied(user, "request beyond FreeLimit");

        // Waiting past a paid window must NOT help a free user: the cap is for life.
        Thread.Sleep(Wait);
        ExpectDenied(user, "free cap must not reset after waiting");
        ExpectDenied(user, "free cap must not reset after waiting");
    }

    [Fact]
    public void PaidUser_AllowsThenDenies()
    {
        var user = "paid-user-1";
        UserTiers[user] = UserTier.Paid;

        ExpectAllowed(user, PaidLimit, "paid");
        ExpectDenied(user, "request beyond PaidLimit in the window");
    }

    [Fact]
    public void PaidUser_AllowsAgainAfterWindow()
    {
        var user = "paid-user-2";
        UserTiers[user] = UserTier.Paid;

        ExpectAllowed(user, PaidLimit, "window 1");
        ExpectDenied(user, "window 1: beyond PaidLimit");

        Thread.Sleep(Wait);

        ExpectAllowed(user, PaidLimit, "after window");
        ExpectDenied(user, "after window: beyond PaidLimit");
    }

    [Fact]
    public void PaidUser_SingleRequestThenWindowExpiry()
    {
        var user = "paid-single-window";
        UserTiers[user] = UserTier.Paid;

        ExpectAllowed(user, 1, "first request");
        Thread.Sleep(Wait);
        ExpectAllowed(user, PaidLimit, "old request must have expired");
        ExpectDenied(user, "after window: beyond PaidLimit");
    }

    [Fact]
    public void PaidUser_TwoFullWindows()
    {
        var user = "paid-two-windows";
        UserTiers[user] = UserTier.Paid;

        for (var w = 1; w <= 3; w++)
        {
            ExpectAllowed(user, PaidLimit, $"window {w}");
            ExpectDenied(user, $"window {w}: beyond PaidLimit");
            if (w < 3) Thread.Sleep(Wait);
        }
    }

    [Fact]
    public void Isolation_BetweenFreeUsers()
    {
        var userA = "free-isolation-a";
        var userB = "free-isolation-b";
        UserTiers[userA] = UserTier.Free;
        UserTiers[userB] = UserTier.Free;

        ExpectAllowed(userA, FreeLimit, "user A");
        ExpectDenied(userA, "user A beyond FreeLimit");

        ExpectAllowed(userB, FreeLimit, "user B must not be affected by user A");
        ExpectDenied(userB, "user B beyond FreeLimit");
    }

    [Fact]
    public void Isolation_BetweenPaidUsers()
    {
        var userA = "paid-isolation-a";
        var userB = "paid-isolation-b";
        UserTiers[userA] = UserTier.Paid;
        UserTiers[userB] = UserTier.Paid;

        ExpectAllowed(userA, PaidLimit, "user A");
        ExpectDenied(userA, "user A beyond PaidLimit");

        ExpectAllowed(userB, PaidLimit, "user B must not be affected by user A");
        ExpectDenied(userB, "user B beyond PaidLimit");
    }

    /// <summary>
    /// EXTRA CREDIT: free user upgrades to paid.
    /// After the upgrade, paid rules apply AND requests made while free still count toward the
    /// current paid window, so the user does NOT get a fresh window just by upgrading.
    /// </summary>
    [Fact]
    public void ExtraCredit_FreeUpgradesToPaid()
    {
        // Assumes PaidLimit <= FreeLimit; otherwise a free user could not make PaidLimit requests.
        if (PaidLimit > FreeLimit) return;

        var user = "upgrade-user-1";
        UserTiers[user] = UserTier.Free;

        ExpectAllowed(user, PaidLimit, "as free");

        UserTiers[user] = UserTier.Paid;

        // Already made PaidLimit requests inside this window -> denied.
        ExpectDenied(user, "past free requests must count toward the paid window");

        Thread.Sleep(Wait);

        ExpectAllowed(user, PaidLimit, "after window");
        ExpectDenied(user, "after window: beyond PaidLimit");
    }

    /// <summary>
    /// EXTRA CREDIT (constants): the implementation must read Constants.FreeLimit rather than
    /// hard-coding 5. C# const cannot be overridden at runtime, so this test only counts;
    /// to really check, change FreeLimit in Constants.cs and re-run.
    /// </summary>
    [Fact]
    public void ExtraCredit_ConstantsRespected()
    {
        var user = "free-constants-check";
        UserTiers[user] = UserTier.Free;
        var allowed = 0;
        for (var i = 0; i < FreeLimit + 2; i++)
        {
            if (AllowRequest(user)) allowed++;
        }
        Assert.Equal(FreeLimit, allowed);
    }
}
