// Tests for the API rate limiter. See ../SPEC.md section 3 for what each test verifies.
//
// All expectations are derived from FREE_LIMIT / PAID_LIMIT / WINDOW_SECONDS, so changing
// those constants in constants.go changes what the tests expect.
//
// Tests that wait for the paid window sleep for real, so the full suite takes roughly
// 6 * (WINDOW_SECONDS + 1) seconds (about 36s with the defaults).
package ratelimit

import (
	"fmt"
	"testing"
	"time"
)

// wait is a little longer than the window so we are safely on the other side of it.
const wait = time.Duration(WINDOW_SECONDS+1) * time.Second

func clearUserTiers() {
	UserTiers = make(map[string]UserTier)
}

func expectAllowed(t *testing.T, user string, count int, label string) {
	t.Helper()
	for i := 0; i < count; i++ {
		if !RateLimiter(user) {
			t.Fatalf("%s: request %d of %d should be allowed", label, i+1, count)
		}
	}
}

func expectDenied(t *testing.T, user string, label string) {
	t.Helper()
	if RateLimiter(user) {
		t.Fatalf("%s: should be denied", label)
	}
}

// TestHarnessSmoke passes even with the unimplemented stub.
// If this test fails, your environment is broken (not your implementation).
func TestHarnessSmoke(t *testing.T) {
	clearUserTiers()
	if FREE_LIMIT <= 0 || PAID_LIMIT <= 0 || WINDOW_SECONDS <= 0 {
		t.Fatal("constants must be positive")
	}
	if len(UserTiers) != 0 {
		t.Fatal("UserTiers should start empty")
	}
	_ = RateLimiter("smoke-user")
}

func TestFreeUser_AllowsThenDenies(t *testing.T) {
	clearUserTiers()
	user := "free-user-1"
	UserTiers[user] = Free

	expectAllowed(t, user, FREE_LIMIT, "free")
	expectDenied(t, user, "request beyond FREE_LIMIT")
	expectDenied(t, user, "request beyond FREE_LIMIT")
}

func TestFreeUser_NeverResets(t *testing.T) {
	clearUserTiers()
	user := "free-user-2"
	UserTiers[user] = Free

	expectAllowed(t, user, FREE_LIMIT, "free")
	expectDenied(t, user, "request beyond FREE_LIMIT")

	// Waiting past a paid window must NOT help a free user: the cap is for life.
	time.Sleep(wait)
	expectDenied(t, user, "free cap must not reset after waiting")
	expectDenied(t, user, "free cap must not reset after waiting")
}

func TestPaidUser_AllowsThenDenies(t *testing.T) {
	clearUserTiers()
	user := "paid-user-1"
	UserTiers[user] = Paid

	expectAllowed(t, user, PAID_LIMIT, "paid")
	expectDenied(t, user, "request beyond PAID_LIMIT in the window")
}

func TestPaidUser_AllowsAgainAfterWindow(t *testing.T) {
	clearUserTiers()
	user := "paid-user-2"
	UserTiers[user] = Paid

	expectAllowed(t, user, PAID_LIMIT, "window 1")
	expectDenied(t, user, "window 1: beyond PAID_LIMIT")

	time.Sleep(wait)

	expectAllowed(t, user, PAID_LIMIT, "after window")
	expectDenied(t, user, "after window: beyond PAID_LIMIT")
}

func TestPaidUser_SingleRequestThenWindowExpiry(t *testing.T) {
	clearUserTiers()
	user := "paid-single-window"
	UserTiers[user] = Paid

	expectAllowed(t, user, 1, "first request")
	time.Sleep(wait)
	expectAllowed(t, user, PAID_LIMIT, "old request must have expired")
	expectDenied(t, user, "after window: beyond PAID_LIMIT")
}

func TestPaidUser_TwoFullWindows(t *testing.T) {
	clearUserTiers()
	user := "paid-two-windows"
	UserTiers[user] = Paid

	for w := 1; w <= 3; w++ {
		expectAllowed(t, user, PAID_LIMIT, fmt.Sprintf("window %d", w))
		expectDenied(t, user, fmt.Sprintf("window %d: beyond PAID_LIMIT", w))
		if w < 3 {
			time.Sleep(wait)
		}
	}
}

func TestIsolation_BetweenFreeUsers(t *testing.T) {
	clearUserTiers()
	userA := "free-isolation-a"
	userB := "free-isolation-b"
	UserTiers[userA] = Free
	UserTiers[userB] = Free

	expectAllowed(t, userA, FREE_LIMIT, "user A")
	expectDenied(t, userA, "user A beyond FREE_LIMIT")

	expectAllowed(t, userB, FREE_LIMIT, "user B must not be affected by user A")
	expectDenied(t, userB, "user B beyond FREE_LIMIT")
}

func TestIsolation_BetweenPaidUsers(t *testing.T) {
	clearUserTiers()
	userA := "paid-isolation-a"
	userB := "paid-isolation-b"
	UserTiers[userA] = Paid
	UserTiers[userB] = Paid

	expectAllowed(t, userA, PAID_LIMIT, "user A")
	expectDenied(t, userA, "user A beyond PAID_LIMIT")

	expectAllowed(t, userB, PAID_LIMIT, "user B must not be affected by user A")
	expectDenied(t, userB, "user B beyond PAID_LIMIT")
}

// TestExtraCredit_FreeUpgradesToPaid is EXTRA CREDIT.
// After the upgrade, paid rules apply AND requests made while free still count toward the
// current paid window, so the user does NOT get a fresh window just by upgrading.
func TestExtraCredit_FreeUpgradesToPaid(t *testing.T) {
	if PAID_LIMIT > FREE_LIMIT {
		t.Skip("upgrade test assumes PAID_LIMIT <= FREE_LIMIT")
	}
	clearUserTiers()
	user := "upgrade-user-1"
	UserTiers[user] = Free

	expectAllowed(t, user, PAID_LIMIT, "as free")

	UserTiers[user] = Paid

	// Already made PAID_LIMIT requests inside this window -> denied.
	expectDenied(t, user, "past free requests must count toward the paid window")

	time.Sleep(wait)

	expectAllowed(t, user, PAID_LIMIT, "after window")
	expectDenied(t, user, "after window: beyond PAID_LIMIT")
}

// TestExtraCredit_ConstantsRespected is EXTRA CREDIT (constants).
// The implementation must read FREE_LIMIT from constants.go rather than hard-coding 5.
// Go constants cannot be overridden at runtime, so this test only counts; to really check,
// change FREE_LIMIT in constants.go and re-run.
func TestExtraCredit_ConstantsRespected(t *testing.T) {
	clearUserTiers()
	user := "free-constants-check"
	UserTiers[user] = Free

	allowed := 0
	for i := 0; i < FREE_LIMIT+2; i++ {
		if RateLimiter(user) {
			allowed++
		}
	}
	if allowed != FREE_LIMIT {
		t.Fatalf("expected exactly %d allowed, got %d", FREE_LIMIT, allowed)
	}
}
