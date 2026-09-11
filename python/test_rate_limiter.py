"""
Tests for the API rate limiter. See ../SPEC.md section 3 for what each test verifies.

All expectations are derived from FREE_LIMIT / PAID_LIMIT / WINDOW_SECONDS, so changing
those constants in rate_limiter.py changes what the tests expect.

Tests that wait for the paid window sleep for real, so the full suite takes roughly
6 * (WINDOW_SECONDS + 1) seconds (about 36s with the defaults).
"""

import time

import pytest

import rate_limiter as rl
from rate_limiter import (
    FREE_LIMIT,
    PAID_LIMIT,
    WINDOW_SECONDS,
    rate_limiter,
    user_tiers,
)

# Wait a little longer than the window so we are safely on the other side of it.
WAIT = WINDOW_SECONDS + 1


@pytest.fixture(autouse=True)
def clear_tiers():
    user_tiers.clear()
    yield


# --- Harness smoke test: passes even with the unimplemented stub -------------------------
# If this test fails, your environment is broken (not your implementation).
def test_harness_smoke():
    assert FREE_LIMIT > 0
    assert PAID_LIMIT > 0
    assert WINDOW_SECONDS > 0
    assert user_tiers == {}
    assert rate_limiter("smoke-user") in (True, False)


# --- Core: free users --------------------------------------------------------------------
def test_free_user_allows_then_denies():
    user = "free-user-1"
    user_tiers[user] = "free"

    for i in range(FREE_LIMIT):
        assert rate_limiter(user) is True, f"free request {i + 1} of {FREE_LIMIT} should be allowed"
    assert rate_limiter(user) is False, "request beyond FREE_LIMIT should be denied"
    assert rate_limiter(user) is False


def test_free_user_never_resets():
    user = "free-user-2"
    user_tiers[user] = "free"

    for _ in range(FREE_LIMIT):
        assert rate_limiter(user) is True
    assert rate_limiter(user) is False

    # Waiting past a paid window must NOT help a free user: the cap is for life.
    time.sleep(WAIT)
    assert rate_limiter(user) is False, "free cap must not reset after waiting"
    assert rate_limiter(user) is False


# --- Core: paid users --------------------------------------------------------------------
def test_paid_user_allows_then_denies():
    user = "paid-user-1"
    user_tiers[user] = "paid"

    for i in range(PAID_LIMIT):
        assert rate_limiter(user) is True, f"paid request {i + 1} of {PAID_LIMIT} should be allowed"
    assert rate_limiter(user) is False, "request beyond PAID_LIMIT in the window should be denied"


def test_paid_user_allows_again_after_window():
    user = "paid-user-2"
    user_tiers[user] = "paid"

    for _ in range(PAID_LIMIT):
        assert rate_limiter(user) is True
    assert rate_limiter(user) is False

    time.sleep(WAIT)

    for i in range(PAID_LIMIT):
        assert rate_limiter(user) is True, f"after the window, request {i + 1} should be allowed"
    assert rate_limiter(user) is False


def test_paid_single_request_then_window_expiry():
    user = "paid-single-window"
    user_tiers[user] = "paid"

    assert rate_limiter(user) is True
    time.sleep(WAIT)
    for _ in range(PAID_LIMIT):
        assert rate_limiter(user) is True, "old request must have expired from the window"
    assert rate_limiter(user) is False


def test_paid_two_full_windows():
    user = "paid-two-windows"
    user_tiers[user] = "paid"

    for window in range(3):
        for i in range(PAID_LIMIT):
            assert rate_limiter(user) is True, f"window {window + 1}: request {i + 1} should be allowed"
        assert rate_limiter(user) is False, f"window {window + 1}: request beyond PAID_LIMIT should be denied"
        if window < 2:
            time.sleep(WAIT)


# --- Core: isolation between users -------------------------------------------------------
def test_isolation_between_free_users():
    user_a = "free-isolation-a"
    user_b = "free-isolation-b"
    user_tiers[user_a] = "free"
    user_tiers[user_b] = "free"

    for _ in range(FREE_LIMIT):
        assert rate_limiter(user_a) is True
    assert rate_limiter(user_a) is False

    for i in range(FREE_LIMIT):
        assert rate_limiter(user_b) is True, f"user B request {i + 1} must not be affected by user A"
    assert rate_limiter(user_b) is False


def test_isolation_between_paid_users():
    user_a = "paid-isolation-a"
    user_b = "paid-isolation-b"
    user_tiers[user_a] = "paid"
    user_tiers[user_b] = "paid"

    for _ in range(PAID_LIMIT):
        assert rate_limiter(user_a) is True
    assert rate_limiter(user_a) is False

    for i in range(PAID_LIMIT):
        assert rate_limiter(user_b) is True, f"user B request {i + 1} must not be affected by user A"
    assert rate_limiter(user_b) is False


# --- EXTRA CREDIT: free user upgrades to paid --------------------------------------------
# After the upgrade, paid rules apply AND requests made while free still count toward the
# current paid window, so the user does NOT get a fresh window just by upgrading.
def test_extra_credit_free_upgrades_to_paid():
    if PAID_LIMIT > FREE_LIMIT:
        pytest.skip("upgrade test assumes PAID_LIMIT <= FREE_LIMIT")

    user = "upgrade-user-1"
    user_tiers[user] = "free"

    for _ in range(PAID_LIMIT):
        assert rate_limiter(user) is True

    user_tiers[user] = "paid"

    # Already made PAID_LIMIT requests inside this window -> denied.
    assert rate_limiter(user) is False, "past free requests must count toward the paid window"

    time.sleep(WAIT)

    for _ in range(PAID_LIMIT):
        assert rate_limiter(user) is True
    assert rate_limiter(user) is False


# --- EXTRA CREDIT: constants are respected -----------------------------------------------
# Overrides FREE_LIMIT at runtime. Passes only if the implementation reads the module-level
# constant on every call instead of copying it at import time.
def test_extra_credit_constants_respected():
    user = "free-constants-check"
    user_tiers[user] = "free"
    new_limit = FREE_LIMIT + 3
    old_limit = rl.FREE_LIMIT
    try:
        rl.FREE_LIMIT = new_limit
        for i in range(new_limit):
            assert rate_limiter(user) is True, f"request {i + 1} of overridden limit {new_limit}"
        assert rate_limiter(user) is False
    finally:
        rl.FREE_LIMIT = old_limit
