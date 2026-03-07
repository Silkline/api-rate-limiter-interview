# API rate limiter: free = 5 requests ever, paid = 2 per 5s window.

# Constants (used by implementation and tests)
FREE_LIMIT = 5
PAID_LIMIT = 2
WINDOW_SECONDS = 5

# Map from user ID to tier. Tests and callers set this; rate limiter reads it.
USER_TIERS = {}

# Per-user request timestamps (ms). Used for free=lifetime count, paid=sliding window.
REQUEST_TIMESTAMPS = {}

def get_tier(user_id)
  USER_TIERS[user_id] || "free"
end

# Returns true if the request is allowed, false if rate limited.
def rate_limiter(user_id)
  tier = get_tier(user_id)
  now_ms = (Time.now.to_f * 1000).to_i
  window_ms = WINDOW_SECONDS * 1000
  cutoff = now_ms - window_ms

  timestamps = (REQUEST_TIMESTAMPS[user_id] || []).dup

  if tier == "free"
    return false if timestamps.size >= FREE_LIMIT
    timestamps << now_ms
    REQUEST_TIMESTAMPS[user_id] = timestamps
    return true
  end

  # paid: only count requests in current window
  in_window = timestamps.count { |t| t > cutoff }
  return false if in_window >= PAID_LIMIT
  timestamps << now_ms
  REQUEST_TIMESTAMPS[user_id] = timestamps
  true
end
