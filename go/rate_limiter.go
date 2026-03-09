package ratelimit

// UserTier is "free" or "paid".
type UserTier string

const (
	Free UserTier = "free"
	Paid UserTier = "paid"
)

// UserTiers maps user ID to tier. Tests and callers set this; rate limiter reads it.
var UserTiers = make(map[string]UserTier)

// RateLimiter returns true if the request is allowed, false if rate limited.
// TODO: Implement per SPEC — free = lifetime cap of FREE_LIMIT, paid = PAID_LIMIT per WINDOW_SECONDS window.
func RateLimiter(userID string) bool {
	// Stub: replace with your implementation
	return false
}
