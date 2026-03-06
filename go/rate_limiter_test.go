package ratelimit

import (
	"testing"
	"time"
)

func init() {
	// Clear state before each test file run
	UserTiers = make(map[string]UserTier)
}

func clearUserTiers() {
	UserTiers = make(map[string]UserTier)
}

func TestRateLimiter_FreeUser_AllowsThenDenies(t *testing.T) {
	clearUserTiers()
	user := "free-user-1"
	UserTiers[user] = Free

	for i := 0; i < FREE_LIMIT; i++ {
		if !RateLimiter(user) {
			t.Fatalf("request %d: expected true", i+1)
		}
	}
	if RateLimiter(user) {
		t.Fatal("expected false after limit")
	}
	if RateLimiter(user) {
		t.Fatal("expected false (no reset)")
	}
}

func TestRateLimiter_FreeUser_NeverResets(t *testing.T) {
	clearUserTiers()
	user := "free-user-2"
	UserTiers[user] = Free

	for i := 0; i < FREE_LIMIT; i++ {
		RateLimiter(user)
	}
	if RateLimiter(user) {
		t.Fatal("expected false")
	}
	// Waiting would not help free users
	if RateLimiter(user) {
		t.Fatal("expected false")
	}
}

func TestRateLimiter_PaidUser_AllowsThenDenies(t *testing.T) {
	clearUserTiers()
	user := "paid-user-1"
	UserTiers[user] = Paid

	if !RateLimiter(user) {
		t.Fatal("first request expected true")
	}
	if !RateLimiter(user) {
		t.Fatal("second request expected true")
	}
	if RateLimiter(user) {
		t.Fatal("third request expected false")
	}
}

func TestRateLimiter_PaidUser_AllowsAgainAfterWindow(t *testing.T) {
	clearUserTiers()
	user := "paid-user-2"
	UserTiers[user] = Paid

	RateLimiter(user)
	RateLimiter(user)
	if RateLimiter(user) {
		t.Fatal("expected false within window")
	}

	time.Sleep(time.Duration(WINDOW_SECONDS+1) * time.Second)

	if !RateLimiter(user) {
		t.Fatal("expected true after window")
	}
	if !RateLimiter(user) {
		t.Fatal("expected true")
	}
	if RateLimiter(user) {
		t.Fatal("expected false")
	}
}

// TestRateLimiter_ExtraCredit_FreeUpgradesToPaid is EXTRA CREDIT.
// After upgrade, paid rules apply and past requests (made when free) must count
// toward the paid 2-per-window limit, so the user does NOT get a fresh paid window.
func TestRateLimiter_ExtraCredit_FreeUpgradesToPaid(t *testing.T) {
	clearUserTiers()
	user := "upgrade-user-1"
	UserTiers[user] = Free

	if !RateLimiter(user) {
		t.Fatal("first free request expected true")
	}
	if !RateLimiter(user) {
		t.Fatal("second free request expected true")
	}

	UserTiers[user] = Paid

	// Paid limit is 2 per window; we already have 2 in this window
	if RateLimiter(user) {
		t.Fatal("expected false after upgrade (past requests count)")
	}

	time.Sleep(time.Duration(WINDOW_SECONDS+1) * time.Second)

	if !RateLimiter(user) {
		t.Fatal("expected true after window")
	}
	if !RateLimiter(user) {
		t.Fatal("expected true")
	}
	if RateLimiter(user) {
		t.Fatal("expected false")
	}
}
