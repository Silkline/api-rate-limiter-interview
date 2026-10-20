//! Tests for the API rate limiter. See ../../SPEC.md section 3 for what each test verifies.
//!
//! All expectations are derived from FREE_LIMIT / PAID_LIMIT / WINDOW_SECONDS, so changing
//! those constants in src/lib.rs changes what the tests expect.
//!
//! Tests that wait for the paid window sleep for real, so the full suite takes roughly
//! 7 * (WINDOW_SECONDS + 1) seconds (about 42s with the defaults).
//!
//! The tests share one global tier map, so run them with `cargo test -- --test-threads=1`
//! (the scripts and README do this for you).

use rate_limiter::{
    rate_limiter, reset_for_tests, with_user_tiers_mut, FREE_LIMIT, PAID_LIMIT, WINDOW_SECONDS,
};
use std::thread;
use std::time::Duration;

/// Wait a little longer than the window so we are safely on the other side of it.
const WAIT: Duration = Duration::from_secs(WINDOW_SECONDS + 1);

/// Half a window: long enough to catch a window that is too short (e.g. ms vs s), short enough
/// to still be inside it.
const HALF_WINDOW: Duration = Duration::from_millis(WINDOW_SECONDS * 1000 / 2);

fn set_tier(user: &str, tier: &str) {
    with_user_tiers_mut(|m| {
        m.insert(user.to_string(), tier.to_string());
    });
}

fn expect_allowed(user: &str, count: usize, label: &str) {
    for i in 0..count {
        assert!(
            rate_limiter(user),
            "{label}: request {} of {count} should be allowed",
            i + 1
        );
    }
}

fn expect_denied(user: &str, label: &str) {
    assert!(!rate_limiter(user), "{label}: should be denied");
}

/// Passes even with the unimplemented stub. If this fails, your environment is broken.
#[test]
fn harness_smoke() {
    reset_for_tests();
    assert!(FREE_LIMIT > 0);
    assert!(PAID_LIMIT > 0);
    assert!(WINDOW_SECONDS > 0);
    assert!(with_user_tiers_mut(|m| m.is_empty()));
    let _ = rate_limiter("smoke-user");
}

#[test]
fn free_user_allows_then_denies() {
    reset_for_tests();
    let user = "free-user-1";
    set_tier(user, "free");

    expect_allowed(user, FREE_LIMIT, "free");
    expect_denied(user, "request beyond FREE_LIMIT");
    expect_denied(user, "request beyond FREE_LIMIT");
}

#[test]
fn free_user_never_resets() {
    reset_for_tests();
    let user = "free-user-2";
    set_tier(user, "free");

    expect_allowed(user, FREE_LIMIT, "free");
    expect_denied(user, "request beyond FREE_LIMIT");

    // Waiting past a paid window must NOT help a free user: the cap is for life.
    thread::sleep(WAIT);
    expect_denied(user, "free cap must not reset after waiting");
    expect_denied(user, "free cap must not reset after waiting");
}

#[test]
fn paid_user_allows_then_denies() {
    reset_for_tests();
    let user = "paid-user-1";
    set_tier(user, "paid");

    expect_allowed(user, PAID_LIMIT, "paid");
    expect_denied(user, "request beyond PAID_LIMIT in the window");
}

#[test]
fn paid_user_allows_again_after_window() {
    reset_for_tests();
    let user = "paid-user-2";
    set_tier(user, "paid");

    expect_allowed(user, PAID_LIMIT, "window 1");
    expect_denied(user, "window 1: beyond PAID_LIMIT");

    thread::sleep(HALF_WINDOW);
    expect_denied(user, "still inside the window: must remain denied (is the window measured in the right unit?)");

    thread::sleep(WAIT - HALF_WINDOW);

    expect_allowed(user, PAID_LIMIT, "after window");
    expect_denied(user, "after window: beyond PAID_LIMIT");
}

#[test]
fn paid_single_request_then_window_expiry() {
    reset_for_tests();
    let user = "paid-single-window";
    set_tier(user, "paid");

    expect_allowed(user, 1, "first request");
    thread::sleep(WAIT);
    expect_allowed(user, PAID_LIMIT, "old request must have expired");
    expect_denied(user, "after window: beyond PAID_LIMIT");
}

#[test]
fn paid_two_full_windows() {
    reset_for_tests();
    let user = "paid-two-windows";
    set_tier(user, "paid");

    for w in 1..=3 {
        expect_allowed(user, PAID_LIMIT, &format!("window {w}"));
        expect_denied(user, &format!("window {w}: beyond PAID_LIMIT"));
        if w < 3 {
            thread::sleep(WAIT);
        }
    }
}

/// The limit applies to every WINDOW_SECONDS-long span, not just windows that start at the
/// user's first request. Requests land at t=0, t=W/2 and t=5W/4; the span from W/2 to 3W/2
/// must still hold at most PAID_LIMIT allowed requests. A fixed window that resets at t=W
/// lets PAID_LIMIT more through at t=5W/4 and fails here.
#[test]
fn paid_at_most_limit_in_any_window() {
    reset_for_tests();
    let user = "paid-any-window";
    set_tier(user, "paid");
    let quarter = Duration::from_millis(WINDOW_SECONDS * 1000 / 4);

    expect_allowed(user, 1, "t=0");
    thread::sleep(quarter * 2);

    expect_allowed(user, PAID_LIMIT - 1, "t=W/2");
    thread::sleep(quarter * 3);

    expect_allowed(user, 1, "t=5W/4: the t=0 request has expired");
    expect_denied(user, "t=5W/4: the t=W/2 requests are still within the last WINDOW_SECONDS");
}

#[test]
fn isolation_between_free_users() {
    reset_for_tests();
    let user_a = "free-isolation-a";
    let user_b = "free-isolation-b";
    set_tier(user_a, "free");
    set_tier(user_b, "free");

    expect_allowed(user_a, FREE_LIMIT, "user A");
    expect_denied(user_a, "user A beyond FREE_LIMIT");

    expect_allowed(user_b, FREE_LIMIT, "user B must not be affected by user A");
    expect_denied(user_b, "user B beyond FREE_LIMIT");
}

#[test]
fn isolation_between_paid_users() {
    reset_for_tests();
    let user_a = "paid-isolation-a";
    let user_b = "paid-isolation-b";
    set_tier(user_a, "paid");
    set_tier(user_b, "paid");

    expect_allowed(user_a, PAID_LIMIT, "user A");
    expect_denied(user_a, "user A beyond PAID_LIMIT");

    expect_allowed(user_b, PAID_LIMIT, "user B must not be affected by user A");
    expect_denied(user_b, "user B beyond PAID_LIMIT");
}

// --- EXTRA CREDIT: free user upgrades to paid ---
// After the upgrade, paid rules apply AND requests made while free still count toward the
// current paid window, so the user does NOT get a fresh window just by upgrading.
#[test]
fn extra_credit_free_upgrades_to_paid() {
    if PAID_LIMIT > FREE_LIMIT {
        eprintln!("skipped: upgrade test assumes PAID_LIMIT <= FREE_LIMIT");
        return;
    }
    reset_for_tests();
    let user = "upgrade-user-1";
    set_tier(user, "free");

    expect_allowed(user, PAID_LIMIT, "as free");

    set_tier(user, "paid");

    // Already made PAID_LIMIT requests inside this window -> denied.
    expect_denied(user, "past free requests must count toward the paid window");

    thread::sleep(WAIT);

    expect_allowed(user, PAID_LIMIT, "after window");
    expect_denied(user, "after window: beyond PAID_LIMIT");
}

// --- EXTRA CREDIT (constants) ---
// The implementation must read FREE_LIMIT from lib.rs rather than hard-coding 5. Rust consts
// cannot be overridden at runtime, so this test only counts; to really check, change
// FREE_LIMIT in src/lib.rs and re-run.
#[test]
fn extra_credit_constants_respected() {
    reset_for_tests();
    let user = "free-constants-check";
    set_tier(user, "free");

    let allowed = (0..FREE_LIMIT + 2).filter(|_| rate_limiter(user)).count();
    assert_eq!(FREE_LIMIT, allowed, "expected exactly FREE_LIMIT allowed");
}
