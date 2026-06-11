import os
import sys
import threading
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


# --- HARD MODE: Concurrency safety ---
# Skipped unless HARD_MODE is set (e.g. HARD_MODE=1 pytest). The naive
# check-then-record pattern races between reading the count and recording the
# request. CPython's GIL makes the race rare by default, so we shrink the
# interpreter's thread switch interval to force frequent interleaving; even so,
# an unsafe implementation may occasionally pass — treat this test as a floor
# and discuss the locking strategy.

hard_mode = pytest.mark.skipif(
    not os.environ.get("HARD_MODE"),
    reason="HARD MODE: set HARD_MODE=1 to enable concurrency tests",
)


def _count_concurrent_allowed(user, count, calls_per_thread=5):
    """Release `count` threads at once against the same user; return how many calls were allowed.

    Each thread makes several calls: with the GIL, a single check-then-record
    rarely gets interrupted, but repeated overlapping calls make the race fire
    reliably for unsafe implementations.
    """
    barrier = threading.Barrier(count)
    results = [0] * count
    errors = []

    def worker(i):
        try:
            barrier.wait()
            for _ in range(calls_per_thread):
                if rate_limiter(user):
                    results[i] += 1
        except Exception as exc:  # noqa: BLE001 — report any failure under concurrency
            errors.append(exc)

    old_interval = sys.getswitchinterval()
    sys.setswitchinterval(1e-6)
    try:
        threads = [threading.Thread(target=worker, args=(i,)) for i in range(count)]
        for t in threads:
            t.start()
        for t in threads:
            t.join()
    finally:
        sys.setswitchinterval(old_interval)

    assert not errors, f"rate limiter raised under concurrency: {errors[:3]}"
    return sum(results)


# Several rounds with a fresh user each: under the GIL the race only fires
# sometimes, so one round can miss an unsafe implementation that repeated
# rounds reliably catch. A thread-safe implementation passes every round.
@hard_mode
def test_hard_mode_paid_user_concurrent_requests():
    for round_idx in range(10):
        user = f"paid-concurrent-{round_idx}"
        user_tiers[user] = "paid"

        allowed = _count_concurrent_allowed(user, 100)
        assert allowed == PAID_LIMIT, (
            f"round {round_idx}: expected exactly {PAID_LIMIT} allowed under concurrency, got {allowed}"
        )


@hard_mode
def test_hard_mode_free_user_concurrent_requests():
    for round_idx in range(10):
        user = f"free-concurrent-{round_idx}"
        user_tiers[user] = "free"

        allowed = _count_concurrent_allowed(user, 100)
        assert allowed == FREE_LIMIT, (
            f"round {round_idx}: expected exactly {FREE_LIMIT} allowed under concurrency, got {allowed}"
        )
