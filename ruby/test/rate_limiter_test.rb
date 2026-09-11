# frozen_string_literal: true

# Tests for the API rate limiter. See ../../SPEC.md section 3 for what each test verifies.
#
# All expectations are derived from FREE_LIMIT / PAID_LIMIT / WINDOW_SECONDS, so changing
# those constants in lib/rate_limiter.rb changes what the tests expect.
#
# Tests that wait for the paid window sleep for real, so the full suite takes roughly
# 6 * (WINDOW_SECONDS + 1) seconds (about 36s with the defaults).
#
# Run:  ruby -Ilib test/rate_limiter_test.rb          (minitest ships with Ruby)

require "minitest/autorun"
require_relative "../lib/rate_limiter"

class RateLimiterTest < Minitest::Test
  # Wait a little longer than the window so we are safely on the other side of it.
  WAIT = WINDOW_SECONDS + 1

  def setup
    USER_TIERS.clear
  end

  def expect_allowed(user, count, label)
    count.times do |i|
      assert rate_limiter(user), "#{label}: request #{i + 1} of #{count} should be allowed"
    end
  end

  def expect_denied(user, label)
    refute rate_limiter(user), "#{label}: should be denied"
  end

  # Passes even with the unimplemented stub. If this fails, your environment is broken.
  def test_harness_smoke
    assert FREE_LIMIT > 0
    assert PAID_LIMIT > 0
    assert WINDOW_SECONDS > 0
    assert_empty USER_TIERS
    assert_includes [true, false], rate_limiter("smoke-user")
  end

  def test_free_user_allows_then_denies
    user = "free-user-1"
    USER_TIERS[user] = "free"

    expect_allowed(user, FREE_LIMIT, "free")
    expect_denied(user, "request beyond FREE_LIMIT")
    expect_denied(user, "request beyond FREE_LIMIT")
  end

  def test_free_user_never_resets
    user = "free-user-2"
    USER_TIERS[user] = "free"

    expect_allowed(user, FREE_LIMIT, "free")
    expect_denied(user, "request beyond FREE_LIMIT")

    # Waiting past a paid window must NOT help a free user: the cap is for life.
    sleep WAIT
    expect_denied(user, "free cap must not reset after waiting")
    expect_denied(user, "free cap must not reset after waiting")
  end

  def test_paid_user_allows_then_denies
    user = "paid-user-1"
    USER_TIERS[user] = "paid"

    expect_allowed(user, PAID_LIMIT, "paid")
    expect_denied(user, "request beyond PAID_LIMIT in the window")
  end

  def test_paid_user_allows_again_after_window
    user = "paid-user-2"
    USER_TIERS[user] = "paid"

    expect_allowed(user, PAID_LIMIT, "window 1")
    expect_denied(user, "window 1: beyond PAID_LIMIT")

    sleep WAIT

    expect_allowed(user, PAID_LIMIT, "after window")
    expect_denied(user, "after window: beyond PAID_LIMIT")
  end

  def test_paid_single_request_then_window_expiry
    user = "paid-single-window"
    USER_TIERS[user] = "paid"

    expect_allowed(user, 1, "first request")
    sleep WAIT
    expect_allowed(user, PAID_LIMIT, "old request must have expired")
    expect_denied(user, "after window: beyond PAID_LIMIT")
  end

  def test_paid_two_full_windows
    user = "paid-two-windows"
    USER_TIERS[user] = "paid"

    (1..3).each do |w|
      expect_allowed(user, PAID_LIMIT, "window #{w}")
      expect_denied(user, "window #{w}: beyond PAID_LIMIT")
      sleep WAIT if w < 3
    end
  end

  def test_isolation_between_free_users
    user_a = "free-isolation-a"
    user_b = "free-isolation-b"
    USER_TIERS[user_a] = "free"
    USER_TIERS[user_b] = "free"

    expect_allowed(user_a, FREE_LIMIT, "user A")
    expect_denied(user_a, "user A beyond FREE_LIMIT")

    expect_allowed(user_b, FREE_LIMIT, "user B must not be affected by user A")
    expect_denied(user_b, "user B beyond FREE_LIMIT")
  end

  def test_isolation_between_paid_users
    user_a = "paid-isolation-a"
    user_b = "paid-isolation-b"
    USER_TIERS[user_a] = "paid"
    USER_TIERS[user_b] = "paid"

    expect_allowed(user_a, PAID_LIMIT, "user A")
    expect_denied(user_a, "user A beyond PAID_LIMIT")

    expect_allowed(user_b, PAID_LIMIT, "user B must not be affected by user A")
    expect_denied(user_b, "user B beyond PAID_LIMIT")
  end

  # --- EXTRA CREDIT: free user upgrades to paid ---
  # After the upgrade, paid rules apply AND requests made while free still count toward the
  # current paid window, so the user does NOT get a fresh window just by upgrading.
  def test_extra_credit_free_upgrades_to_paid
    skip "upgrade test assumes PAID_LIMIT <= FREE_LIMIT" if PAID_LIMIT > FREE_LIMIT

    user = "upgrade-user-1"
    USER_TIERS[user] = "free"

    expect_allowed(user, PAID_LIMIT, "as free")

    USER_TIERS[user] = "paid"

    # Already made PAID_LIMIT requests inside this window -> denied.
    expect_denied(user, "past free requests must count toward the paid window")

    sleep WAIT

    expect_allowed(user, PAID_LIMIT, "after window")
    expect_denied(user, "after window: beyond PAID_LIMIT")
  end

  # --- EXTRA CREDIT: constants are respected ---
  # Overrides FREE_LIMIT at runtime. Passes only if the implementation reads the constant on
  # every call instead of copying it once.
  def test_extra_credit_constants_respected
    original = FREE_LIMIT
    new_limit = original + 3
    Object.send(:remove_const, :FREE_LIMIT)
    Object.const_set(:FREE_LIMIT, new_limit)

    user = "free-constants-check"
    USER_TIERS[user] = "free"
    expect_allowed(user, new_limit, "overridden FREE_LIMIT=#{new_limit}")
    expect_denied(user, "request beyond overridden FREE_LIMIT")
  ensure
    Object.send(:remove_const, :FREE_LIMIT)
    Object.const_set(:FREE_LIMIT, original)
  end
end
