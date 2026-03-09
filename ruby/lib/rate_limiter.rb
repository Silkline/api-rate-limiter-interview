# API rate limiter: free = 5 requests ever, paid = 2 per 5s window.

# Constants (used by implementation and tests)
FREE_LIMIT = 5
PAID_LIMIT = 2
WINDOW_SECONDS = 5

# Map from user ID to tier. Tests and callers set this; rate limiter reads it.
USER_TIERS = {}

def rate_limiter(user_id)
  # TODO: Implement per SPEC — free = lifetime cap of FREE_LIMIT, paid = PAID_LIMIT per WINDOW_SECONDS window.
  # Stub: replace with your implementation
  false
end
