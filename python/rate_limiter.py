"""
API rate limiter: free = 5 requests ever, paid = 2 per 5s window.
"""

# Constants (used by implementation and tests)
FREE_LIMIT = 5
PAID_LIMIT = 2
WINDOW_SECONDS = 5

# Map from user ID to tier. Tests and callers set this; rate limiter reads it.
user_tiers: dict[str, str] = {}

# Per-user request timestamps (ms). Used for free=lifetime count, paid=sliding window.
_request_timestamps: dict[str, list[float]] = {}


def _get_tier(user_id: str) -> str:
    return user_tiers.get(user_id, "free")


def rate_limiter(user_id: str) -> bool:
    """
    Returns True if the request is allowed, False if rate limited.
    """
    import time

    tier = _get_tier(user_id)
    now = time.time() * 1000
    window_ms = WINDOW_SECONDS * 1000
    cutoff = now - window_ms

    timestamps = _request_timestamps.get(user_id, []).copy()

    if tier == "free":
        if len(timestamps) >= FREE_LIMIT:
            return False
        timestamps.append(now)
        _request_timestamps[user_id] = timestamps
        return True

    # paid: only count requests in current window
    in_window = [t for t in timestamps if t > cutoff]
    if len(in_window) >= PAID_LIMIT:
        return False
    timestamps.append(now)
    _request_timestamps[user_id] = timestamps
    return True
