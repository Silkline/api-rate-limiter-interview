package ratelimit

import (
	"os"
	"sync"
	"sync/atomic"
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

func TestRateLimiter_IsolationBetweenUsers(t *testing.T) {
	clearUserTiers()
	userA := "free-isolation-a"
	userB := "free-isolation-b"
	UserTiers[userA] = Free
	UserTiers[userB] = Free

	// User A uses 3 requests; user B should still get FREE_LIMIT allowed
	for i := 0; i < 3; i++ {
		if !RateLimiter(userA) {
			t.Fatalf("user A request %d: expected true", i+1)
		}
	}
	for i := 0; i < FREE_LIMIT; i++ {
		if !RateLimiter(userB) {
			t.Fatalf("user B request %d: expected true", i+1)
		}
	}
	if RateLimiter(userB) {
		t.Fatal("user B: expected false after FREE_LIMIT")
	}
}

func TestRateLimiter_PaidUser_SingleRequestThenWindowExpiry(t *testing.T) {
	clearUserTiers()
	user := "paid-single-window"
	UserTiers[user] = Paid

	if !RateLimiter(user) {
		t.Fatal("first request expected true")
	}
	time.Sleep(time.Duration(WINDOW_SECONDS+1) * time.Second)
	if !RateLimiter(user) {
		t.Fatal("after window: first request expected true")
	}
	if !RateLimiter(user) {
		t.Fatal("after window: second request expected true")
	}
	if RateLimiter(user) {
		t.Fatal("after window: third request expected false")
	}
}

func TestRateLimiter_PaidUser_TwoFullWindows(t *testing.T) {
	clearUserTiers()
	user := "paid-two-windows"
	UserTiers[user] = Paid

	// Window 1: 2 allowed, then denied
	if !RateLimiter(user) {
		t.Fatal("w1: first expected true")
	}
	if !RateLimiter(user) {
		t.Fatal("w1: second expected true")
	}
	if RateLimiter(user) {
		t.Fatal("w1: third expected false")
	}
	time.Sleep(time.Duration(WINDOW_SECONDS+1) * time.Second)
	// Window 2: 2 allowed, then denied
	if !RateLimiter(user) {
		t.Fatal("w2: first expected true")
	}
	if !RateLimiter(user) {
		t.Fatal("w2: second expected true")
	}
	if RateLimiter(user) {
		t.Fatal("w2: third expected false")
	}
	time.Sleep(time.Duration(WINDOW_SECONDS+1) * time.Second)
	// Window 3: 2 allowed, then denied
	if !RateLimiter(user) {
		t.Fatal("w3: first expected true")
	}
	if !RateLimiter(user) {
		t.Fatal("w3: second expected true")
	}
	if RateLimiter(user) {
		t.Fatal("w3: third expected false")
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

// --- HARD MODE: Concurrency safety ---
// Skipped by default: a non-thread-safe implementation can crash the whole test
// binary (e.g. "fatal error: concurrent map writes"). Enable with:
//
//	HARD_MODE=1 go test -race ./...
//
// or HARD_MODE=1 ./scripts/test.sh go (adds -race automatically).

func requireHardMode(t *testing.T) {
	t.Helper()
	if os.Getenv("HARD_MODE") == "" {
		t.Skip("HARD MODE: set HARD_MODE=1 (and use -race) to enable concurrency tests")
	}
}

// countConcurrentAllowed releases n goroutines at once against the same user
// and returns how many requests were allowed.
func countConcurrentAllowed(user string, n int) int64 {
	var (
		start   = make(chan struct{})
		wg      sync.WaitGroup
		allowed atomic.Int64
	)
	for i := 0; i < n; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			<-start
			if RateLimiter(user) {
				allowed.Add(1)
			}
		}()
	}
	close(start)
	wg.Wait()
	return allowed.Load()
}

// HARD MODE: 100 concurrent requests for one paid user — exactly PAID_LIMIT
// may succeed. The naive check-then-record pattern races between reading the
// count and recording the request, allowing more than the limit through.
func TestRateLimiter_HardMode_PaidUser_ConcurrentRequests(t *testing.T) {
	requireHardMode(t)
	clearUserTiers()
	user := "paid-concurrent-1"
	UserTiers[user] = Paid

	if got := countConcurrentAllowed(user, 100); got != int64(PAID_LIMIT) {
		t.Fatalf("expected exactly %d allowed under concurrency, got %d", PAID_LIMIT, got)
	}
}

// HARD MODE: same for a free user — exactly FREE_LIMIT of 100 concurrent
// requests may succeed.
func TestRateLimiter_HardMode_FreeUser_ConcurrentRequests(t *testing.T) {
	requireHardMode(t)
	clearUserTiers()
	user := "free-concurrent-1"
	UserTiers[user] = Free

	if got := countConcurrentAllowed(user, 100); got != int64(FREE_LIMIT) {
		t.Fatalf("expected exactly %d allowed under concurrency, got %d", FREE_LIMIT, got)
	}
}
