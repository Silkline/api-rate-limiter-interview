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
func RateLimiter(userID string) bool {

}
