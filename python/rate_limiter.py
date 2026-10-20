"""
API rate limiter: free = 5 requests ever, paid = at most 2 in any 5-second span.
"""

# Constants (used by implementation and tests)
FREE_LIMIT = 5
PAID_LIMIT = 2
WINDOW_SECONDS = 5

# Map from user ID to tier. Tests and callers set this; rate limiter reads it.
user_tiers: dict[str, str] = {}


def rate_limiter(user_id: str) -> bool:
    """
    Returns True if the request is allowed, False if rate limited.
    TODO: Implement per SPEC — free = lifetime cap of FREE_LIMIT, paid = at most PAID_LIMIT in any WINDOW_SECONDS-second span.
    """
    # Stub: replace with your implementation
    return False
