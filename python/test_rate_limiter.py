import time

import pytest

from rate_limiter import (
    FREE_LIMIT,
    PAID_LIMIT,
    WINDOW_SECONDS,
    rate_limiter,
    user_tiers,
)


@pytest.fixture(autouse=True)
def clear_tiers():
    user_tiers.clear()
    yield


def test_free_user_allows_then_denies():
    user = "free-user-1"
    user_tiers[user] = "free"

    for i in range(FREE_LIMIT):
        assert rate_limiter(user) is True
    assert rate_limiter(user) is False
    assert rate_limiter(user) is False


def test_free_user_never_resets():
    user = "free-user-2"
    user_tiers[user] = "free"

    for _ in range(FREE_LIMIT):
        rate_limiter(user)
    assert rate_limiter(user) is False
    assert rate_limiter(user) is False


def test_paid_user_allows_then_denies():
    user = "paid-user-1"
    user_tiers[user] = "paid"

    assert rate_limiter(user) is True
    assert rate_limiter(user) is True
    assert rate_limiter(user) is False


def test_paid_user_allows_again_after_window():
    user = "paid-user-2"
    user_tiers[user] = "paid"

    rate_limiter(user)
    rate_limiter(user)
    assert rate_limiter(user) is False

    time.sleep(WINDOW_SECONDS + 0.5)

    assert rate_limiter(user) is True
    assert rate_limiter(user) is True
    assert rate_limiter(user) is False


def test_isolation_between_users():
    user_a = "free-isolation-a"
    user_b = "free-isolation-b"
    user_tiers[user_a] = "free"
    user_tiers[user_b] = "free"

    for _ in range(3):
        assert rate_limiter(user_a) is True
    for _ in range(FREE_LIMIT):
        assert rate_limiter(user_b) is True
    assert rate_limiter(user_b) is False


def test_paid_single_request_then_window_expiry():
    user = "paid-single-window"
    user_tiers[user] = "paid"

    assert rate_limiter(user) is True
    time.sleep(WINDOW_SECONDS + 0.5)
    assert rate_limiter(user) is True
    assert rate_limiter(user) is True
    assert rate_limiter(user) is False


def test_paid_two_full_windows():
    user = "paid-two-windows"
    user_tiers[user] = "paid"

    assert rate_limiter(user) is True
    assert rate_limiter(user) is True
    assert rate_limiter(user) is False
    time.sleep(WINDOW_SECONDS + 0.5)
    assert rate_limiter(user) is True
    assert rate_limiter(user) is True
    assert rate_limiter(user) is False
    time.sleep(WINDOW_SECONDS + 0.5)
    assert rate_limiter(user) is True
    assert rate_limiter(user) is True
    assert rate_limiter(user) is False


# --- EXTRA CREDIT: Free user upgrades to paid ---
# After upgrade, paid rules apply and past requests (made when free) must count
# toward the paid 2-per-window limit, so the user does NOT get a fresh paid window.
def test_extra_credit_free_upgrades_to_paid():
    user = "upgrade-user-1"
    user_tiers[user] = "free"

    assert rate_limiter(user) is True
    assert rate_limiter(user) is True

    user_tiers[user] = "paid"

    # Paid limit is 2 per window; we already have 2 in this window
    assert rate_limiter(user) is False

    time.sleep(WINDOW_SECONDS + 0.5)

    assert rate_limiter(user) is True
    assert rate_limiter(user) is True
    assert rate_limiter(user) is False


# --- EXTRA CREDIT: Constants respected (override FREE_LIMIT in test) ---
def test_extra_credit_constants_respected():
    import rate_limiter as rl

    user = "free-constants-check"
    user_tiers[user] = "free"
    old_limit = rl.FREE_LIMIT
    try:
        rl.FREE_LIMIT = 2
        assert rate_limiter(user) is True
        assert rate_limiter(user) is True
        assert rate_limiter(user) is False
    finally:
        rl.FREE_LIMIT = old_limit
