require "minitest/autorun"
require_relative "../lib/rate_limiter"

class RateLimiterTest < Minitest::Test
  def setup
    USER_TIERS.clear
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

  def test_isolation_between_users
    user_a = "free-isolation-a"
    user_b = "free-isolation-b"
    USER_TIERS[user_a] = "free"
    USER_TIERS[user_b] = "free"

    3.times { rate_limiter(user_a) }
    FREE_LIMIT.times { assert rate_limiter(user_b) }
    refute rate_limiter(user_b)
  end

  def test_paid_single_request_then_window_expiry
    user = "paid-single-window"
    USER_TIERS[user] = "paid"

    assert rate_limiter(user)
    sleep WINDOW_SECONDS + 0.5
    assert rate_limiter(user)
    assert rate_limiter(user)
    refute rate_limiter(user)
  end

  def test_paid_two_full_windows
    user = "paid-two-windows"
    USER_TIERS[user] = "paid"

    assert rate_limiter(user)
    assert rate_limiter(user)
    refute rate_limiter(user)
    sleep WINDOW_SECONDS + 0.5
    assert rate_limiter(user)
    assert rate_limiter(user)
    refute rate_limiter(user)
    sleep WINDOW_SECONDS + 0.5
    assert rate_limiter(user)
    assert rate_limiter(user)
    refute rate_limiter(user)
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

  # --- EXTRA CREDIT: Constants respected (change FREE_LIMIT in test and things still work) ---
  def test_extra_credit_constants_respected
    orig = FREE_LIMIT
    Object.send(:remove_const, :FREE_LIMIT)
    Object.const_set(:FREE_LIMIT, 2)
    user = "free-constants-check"
    USER_TIERS[user] = "free"
    2.times { assert rate_limiter(user) }
    refute rate_limiter(user)
  ensure
    Object.send(:remove_const, :FREE_LIMIT)
    Object.const_set(:FREE_LIMIT, orig)
  end

  # --- HARD MODE: Concurrency safety ---
  # Skipped unless HARD_MODE is set (e.g. HARD_MODE=1 bundle exec ruby ...).
  # The naive check-then-record pattern races between reading the count and
  # recording the request. MRI's GVL makes the race rare in practice, so an
  # unsafe implementation may occasionally pass — treat this test as a floor
  # and discuss the locking strategy.

  # Releases `count` threads at once against the same user; returns how many
  # calls were allowed. Each thread makes several calls: with the GVL, a single
  # check-then-record rarely gets interrupted, but repeated overlapping calls
  # make the race fire reliably for unsafe implementations.
  def count_concurrent_allowed(user, count, calls_per_thread: 3)
    start = Queue.new
    threads = count.times.map do
      Thread.new do
        start.pop
        calls_per_thread.times.count { rate_limiter(user) }
      end
    end
    count.times { start << true }
    # Thread#value re-raises any exception from the thread, failing the test loudly.
    threads.sum(&:value)
  end

  # HARD MODE: 50 threads × 3 calls for one paid user — exactly PAID_LIMIT may
  # succeed. Several rounds with a fresh user each: under the GVL the race only
  # fires sometimes, so one round can miss an unsafe implementation that
  # repeated rounds reliably catch.
  def test_hard_mode_paid_user_concurrent_requests
    skip "HARD MODE: set HARD_MODE=1 to enable concurrency tests" unless ENV["HARD_MODE"]
    10.times do |round|
      user = "paid-concurrent-#{round}"
      USER_TIERS[user] = "paid"

      allowed = count_concurrent_allowed(user, 50)
      assert_equal PAID_LIMIT, allowed, "round #{round}: expected exactly PAID_LIMIT allowed under concurrency"
    end
  end

  # HARD MODE: 50 threads × 3 calls for one free user — exactly FREE_LIMIT may succeed.
  def test_hard_mode_free_user_concurrent_requests
    skip "HARD MODE: set HARD_MODE=1 to enable concurrency tests" unless ENV["HARD_MODE"]
    10.times do |round|
      user = "free-concurrent-#{round}"
      USER_TIERS[user] = "free"

      allowed = count_concurrent_allowed(user, 50)
      assert_equal FREE_LIMIT, allowed, "round #{round}: expected exactly FREE_LIMIT allowed under concurrency"
    end
  end
end
