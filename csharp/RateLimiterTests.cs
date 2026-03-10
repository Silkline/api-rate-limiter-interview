using Xunit;
using ApiRateLimiter;

namespace ApiRateLimiterTests;

public class RateLimiterTests : IDisposable
{
    public void Dispose() => RateLimiter.UserTiers.Clear();

    [Fact]
    public void FreeUser_AllowsThenDenies()
    {
        var user = "free-user-1";
        RateLimiter.UserTiers[user] = UserTier.Free;

        for (var i = 0; i < Constants.FreeLimit; i++)
            Assert.True(RateLimiter.AllowRequest(user), $"request {i + 1}");
        Assert.False(RateLimiter.AllowRequest(user));
        Assert.False(RateLimiter.AllowRequest(user));
    }

    [Fact]
    public void FreeUser_NeverResets()
    {
        var user = "free-user-2";
        RateLimiter.UserTiers[user] = UserTier.Free;

        for (var i = 0; i < Constants.FreeLimit; i++)
            RateLimiter.AllowRequest(user);
        Assert.False(RateLimiter.AllowRequest(user));
        Assert.False(RateLimiter.AllowRequest(user));
    }

    [Fact]
    public void PaidUser_AllowsThenDenies()
    {
        var user = "paid-user-1";
        RateLimiter.UserTiers[user] = UserTier.Paid;

        Assert.True(RateLimiter.AllowRequest(user));
        Assert.True(RateLimiter.AllowRequest(user));
        Assert.False(RateLimiter.AllowRequest(user));
    }

    [Fact]
    public void PaidUser_AllowsAgainAfterWindow()
    {
        var user = "paid-user-2";
        RateLimiter.UserTiers[user] = UserTier.Paid;

        RateLimiter.AllowRequest(user);
        RateLimiter.AllowRequest(user);
        Assert.False(RateLimiter.AllowRequest(user));

        Thread.Sleep(TimeSpan.FromSeconds(Constants.WindowSeconds + 1));

        Assert.True(RateLimiter.AllowRequest(user));
        Assert.True(RateLimiter.AllowRequest(user));
        Assert.False(RateLimiter.AllowRequest(user));
    }

    [Fact]
    public void IsolationBetweenUsers()
    {
        var userA = "free-isolation-a";
        var userB = "free-isolation-b";
        RateLimiter.UserTiers[userA] = UserTier.Free;
        RateLimiter.UserTiers[userB] = UserTier.Free;

        for (var i = 0; i < 3; i++)
            Assert.True(RateLimiter.AllowRequest(userA));
        for (var i = 0; i < Constants.FreeLimit; i++)
            Assert.True(RateLimiter.AllowRequest(userB));
        Assert.False(RateLimiter.AllowRequest(userB));
    }

    [Fact]
    public void PaidUser_SingleRequestThenWindowExpiry()
    {
        var user = "paid-single-window";
        RateLimiter.UserTiers[user] = UserTier.Paid;

        Assert.True(RateLimiter.AllowRequest(user));
        Thread.Sleep(TimeSpan.FromSeconds(Constants.WindowSeconds + 1));
        Assert.True(RateLimiter.AllowRequest(user));
        Assert.True(RateLimiter.AllowRequest(user));
        Assert.False(RateLimiter.AllowRequest(user));
    }

    [Fact]
    public void PaidUser_TwoFullWindows()
    {
        var user = "paid-two-windows";
        RateLimiter.UserTiers[user] = UserTier.Paid;

        Assert.True(RateLimiter.AllowRequest(user));
        Assert.True(RateLimiter.AllowRequest(user));
        Assert.False(RateLimiter.AllowRequest(user));
        Thread.Sleep(TimeSpan.FromSeconds(Constants.WindowSeconds + 1));
        Assert.True(RateLimiter.AllowRequest(user));
        Assert.True(RateLimiter.AllowRequest(user));
        Assert.False(RateLimiter.AllowRequest(user));
        Thread.Sleep(TimeSpan.FromSeconds(Constants.WindowSeconds + 1));
        Assert.True(RateLimiter.AllowRequest(user));
        Assert.True(RateLimiter.AllowRequest(user));
        Assert.False(RateLimiter.AllowRequest(user));
    }

    /// <summary>
    /// EXTRA CREDIT: Free user upgrades to paid.
    /// After upgrade, paid rules apply and past requests (made when free) must count
    /// toward the paid 2-per-window limit, so the user does NOT get a fresh paid window.
    /// </summary>
    [Fact]
    public void ExtraCredit_FreeUpgradesToPaid()
    {
        var user = "upgrade-user-1";
        RateLimiter.UserTiers[user] = UserTier.Free;

        Assert.True(RateLimiter.AllowRequest(user));
        Assert.True(RateLimiter.AllowRequest(user));

        RateLimiter.UserTiers[user] = UserTier.Paid;

        // Paid limit is 2 per window; we already have 2 in this window
        Assert.False(RateLimiter.AllowRequest(user));

        Thread.Sleep(TimeSpan.FromSeconds(Constants.WindowSeconds + 1));

        Assert.True(RateLimiter.AllowRequest(user));
        Assert.True(RateLimiter.AllowRequest(user));
        Assert.False(RateLimiter.AllowRequest(user));
    }

    /// <summary>
    /// EXTRA CREDIT (constants): Implementation must use Constants.FreeLimit.
    /// Change the value in Constants.cs and re-run tests; behavior should match.
    /// </summary>
    [Fact]
    public void ExtraCredit_ConstantsRespected()
    {
        var user = "free-constants-check";
        RateLimiter.UserTiers[user] = UserTier.Free;
        var allowed = 0;
        for (var i = 0; i < Constants.FreeLimit + 2; i++)
        {
            if (RateLimiter.AllowRequest(user)) allowed++;
        }
        Assert.Equal(Constants.FreeLimit, allowed);
    }
}
