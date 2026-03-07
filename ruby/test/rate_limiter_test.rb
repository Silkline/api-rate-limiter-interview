require "minitest/autorun"
require_relative "../lib/rate_limiter"

class RateLimiterTest < Minitest::Test
  def setup
    USER_TIERS.clear
    REQUEST_TIMESTAMPS.clear
  end

  def test_free_user_allows_then_denies
    user = "free-user-1"
    USER_TIERS[user] = "free"

    FREE_LIMIT.times { assert rate_limiter(user), "expected true within limit" }
    refute rate_limiter(user), "expected false after limit"
    refute rate_limiter(user), "expected false (no reset)"
  end

  def test_free_user_never_resets
    user = "free-user-2"
    USER_TIERS[user] = "free"

    FREE_LIMIT.times { rate_limiter(user) }
    refute rate_limiter(user)
    refute rate_limiter(user)
  end

  def test_paid_user_allows_then_denies
    user = "paid-user-1"
    USER_TIERS[user] = "paid"

    assert rate_limiter(user), "first request expected true"
    assert rate_limiter(user), "second request expected true"
    refute rate_limiter(user), "third request expected false"
  end

  def test_paid_user_allows_again_after_window
    user = "paid-user-2"
    USER_TIERS[user] = "paid"

    rate_limiter(user)
    rate_limiter(user)
    refute rate_limiter(user), "expected false within window"

    sleep WINDOW_SECONDS + 0.5

    assert rate_limiter(user), "expected true after window"
    assert rate_limiter(user), "expected true"
    refute rate_limiter(user), "expected false"
  end

  # --- EXTRA CREDIT: Free user upgrades to paid ---
  # After upgrade, paid rules apply and past requests (made when free) must count
  # toward the paid 2-per-window limit, so the user does NOT get a fresh paid window.
  def test_extra_credit_free_upgrades_to_paid
    user = "upgrade-user-1"
    USER_TIERS[user] = "free"

    assert rate_limiter(user), "first free request expected true"
    assert rate_limiter(user), "second free request expected true"

    USER_TIERS[user] = "paid"

    # Paid limit is 2 per window; we already have 2 in this window
    refute rate_limiter(user), "expected false after upgrade (past requests count)"

    sleep WINDOW_SECONDS + 0.5

    assert rate_limiter(user), "expected true after window"
    assert rate_limiter(user), "expected true"
    refute rate_limiter(user), "expected false"
  end
end
