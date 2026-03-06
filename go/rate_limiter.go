package ratelimit

import (
	"sync"
	"time"
)

// UserTier is "free" or "paid".
type UserTier string

const (
	Free UserTier = "free"
	Paid UserTier = "paid"
)

// UserTiers maps user ID to tier. Tests and callers set this; rate limiter reads it.
var UserTiers = make(map[string]UserTier)

var mu sync.Mutex

// requestTimestamps stores per-user request timestamps for both free (lifetime) and paid (window) logic.
var requestTimestamps = make(map[string][]int64)

func getTier(userID string) UserTier {
	if t, ok := UserTiers[userID]; ok {
		return t
	}
	return Free
}

// RateLimiter returns true if the request is allowed, false if rate limited.
func RateLimiter(userID string) bool {
	mu.Lock()
	defer mu.Unlock()

	tier := getTier(userID)
	now := time.Now().UnixMilli()
	windowMs := int64(WINDOW_SECONDS) * 1000
	cutoff := now - windowMs

	ts := requestTimestamps[userID]
	if ts == nil {
		ts = []int64{}
	}

	if tier == Free {
		if len(ts) >= FREE_LIMIT {
			return false
		}
		requestTimestamps[userID] = append(ts, now)
		return true
	}

	// Paid: only count requests in current window
	inWindow := 0
	for _, t := range ts {
		if t > cutoff {
			inWindow++
		}
	}
	if inWindow >= PAID_LIMIT {
		return false
	}
	requestTimestamps[userID] = append(ts, now)
	return true
}
