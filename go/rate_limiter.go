package ratelimit

// UserTier is "free" or "paid".
type UserTier string

const (
	Free UserTier = "free"
	Paid UserTier = "paid"
)

// UserTiers maps user ID to tier. Tests set this; the rate limiter reads it.
var UserTiers = make(map[string]UserTier)

// RateLimiter returns true if the request is allowed, false if it is rate limited.
//
// TODO: implement per SPEC.md.
//   - Look up the user's tier in UserTiers.
//   - Free: allow the first FREE_LIMIT requests ever, then always deny.
//   - Paid: allow at most PAID_LIMIT requests per WINDOW_SECONDS-second window.
//
// Keep per-user state in memory (e.g. package-level maps).
func RateLimiter(userID string) bool {
	// Stub: replace with your implementation.
	_ = userID
	return false
}
