// ==========================================================================
//                                                                          
//      SAMPLE SOLUTION  --  INTERVIEWER REFERENCE ONLY  --  DO NOT READ    
//                                                                          
//   If you are the CANDIDATE / INTERVIEWEE: STOP. Close this file now.     
//   This folder contains the reference solution to the exercise you are    
//   being asked to solve. Reading it defeats the purpose of the interview  
//   and will be obvious in the follow-up discussion.                       
//                                                                          
// ==========================================================================
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
// ==========================================================================
//   The reference solution begins below this line.                         
// ==========================================================================

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

// mu guards history and makes the whole check-then-record atomic, so
// concurrent callers admit exactly the limit (hard mode). A per-user lock
// would reduce contention; one global lock is the simplest correct choice.
var mu sync.Mutex

// history holds every allowed request's timestamp per user. Keeping the full
// history makes the free lifetime cap and the upgrade extra credit (past free
// requests count toward the paid window) fall out naturally. To bound memory,
// store only the lifetime count plus the most recent PAID_LIMIT timestamps.
var history = make(map[string][]time.Time)

// RateLimiter returns true if the request is allowed, false if rate limited.
func RateLimiter(userID string) bool {
	mu.Lock()
	defer mu.Unlock()

	now := time.Now()
	calls := history[userID]

	var allowed bool
	if UserTiers[userID] == Paid {
		cutoff := now.Add(-time.Duration(WINDOW_SECONDS) * time.Second)
		inWindow := 0
		for _, t := range calls {
			if !t.Before(cutoff) {
				inWindow++
			}
		}
		allowed = inWindow < PAID_LIMIT
	} else {
		// Unknown users default to free.
		allowed = len(calls) < FREE_LIMIT
	}

	if allowed {
		history[userID] = append(calls, now)
	}
	return allowed
}
