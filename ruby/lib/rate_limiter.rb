# frozen_string_literal: true

# API rate limiter.
#
# - Free users: FREE_LIMIT requests total, ever (lifetime cap; never resets).
# - Paid users: PAID_LIMIT requests per WINDOW_SECONDS-second window; the window resets.
#
# Implement `rate_limiter` below. Everything else in this file is scaffolding used by the tests.

# Constants (used by the implementation AND the tests; change them here to change the rules).
FREE_LIMIT = 5
PAID_LIMIT = 2
WINDOW_SECONDS = 5

# Map from user ID to tier ("free" | "paid"). Tests set this; the rate limiter reads it.
USER_TIERS = {}

# Returns true if the request is allowed, false if it is rate limited.
#
# TODO: implement per SPEC.md.
#   - Look up the user's tier in USER_TIERS.
#   - "free": allow the first FREE_LIMIT requests ever, then always deny.
#   - "paid": allow at most PAID_LIMIT requests per WINDOW_SECONDS-second window.
# Keep per-user state in memory (e.g. top-level Hash constants).
# Read FREE_LIMIT / PAID_LIMIT / WINDOW_SECONDS at call time so the tests can change them.
def rate_limiter(user_id)
  # Stub: replace with your implementation.
  false
end
