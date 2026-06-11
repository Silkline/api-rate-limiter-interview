use rate_limiter::{
    rate_limiter, reset_for_tests, with_user_tiers_mut, FREE_LIMIT, PAID_LIMIT, WINDOW_SECONDS,
};
use std::sync::{Arc, Barrier};
use std::thread;
use std::time::Duration;

#[test]
fn free_user_allows_then_denies() {
    reset_for_tests();
    let user = "free-user-1";
    with_user_tiers_mut(|m| {
        m.clear();
        m.insert(user.to_string(), "free".to_string());
    });

    for _ in 0..FREE_LIMIT {
        assert!(rate_limiter(user), "expected true within limit");
    }
    assert!(!rate_limiter(user), "expected false after limit");
    assert!(!rate_limiter(user), "expected false (no reset)");
}

#[test]
fn free_user_never_resets() {
    reset_for_tests();
    let user = "free-user-2";
    with_user_tiers_mut(|m| {
        m.clear();
        m.insert(user.to_string(), "free".to_string());
    });

    for _ in 0..FREE_LIMIT {
        rate_limiter(user);
    }
    assert!(!rate_limiter(user));
    assert!(!rate_limiter(user));
}

#[test]
fn paid_user_allows_then_denies() {
    reset_for_tests();
    let user = "paid-user-1";
    with_user_tiers_mut(|m| {
        m.clear();
        m.insert(user.to_string(), "paid".to_string());
    });

    assert!(rate_limiter(user), "first request expected true");
    assert!(rate_limiter(user), "second request expected true");
    assert!(!rate_limiter(user), "third request expected false");
}

#[test]
fn paid_user_allows_again_after_window() {
    reset_for_tests();
    let user = "paid-user-2";
    with_user_tiers_mut(|m| {
        m.clear();
        m.insert(user.to_string(), "paid".to_string());
    });

    rate_limiter(user);
    rate_limiter(user);
    assert!(!rate_limiter(user), "expected false within window");

    thread::sleep(Duration::from_secs(WINDOW_SECONDS + 1));

    assert!(rate_limiter(user), "expected true after window");
    assert!(rate_limiter(user), "expected true");
    assert!(!rate_limiter(user), "expected false");
}

#[test]
fn isolation_between_users() {
    reset_for_tests();
    let user_a = "free-isolation-a";
    let user_b = "free-isolation-b";
    with_user_tiers_mut(|m| {
        m.clear();
        m.insert(user_a.to_string(), "free".to_string());
        m.insert(user_b.to_string(), "free".to_string());
    });

    for _ in 0..3 {
        assert!(rate_limiter(user_a));
    }
    for _ in 0..FREE_LIMIT {
        assert!(rate_limiter(user_b));
    }
    assert!(!rate_limiter(user_b));
}

#[test]
fn paid_single_request_then_window_expiry() {
    reset_for_tests();
    let user = "paid-single-window";
    with_user_tiers_mut(|m| {
        m.clear();
        m.insert(user.to_string(), "paid".to_string());
    });

    assert!(rate_limiter(user));
    thread::sleep(Duration::from_secs(WINDOW_SECONDS + 1));
    assert!(rate_limiter(user));
    assert!(rate_limiter(user));
    assert!(!rate_limiter(user));
}

#[test]
fn paid_two_full_windows() {
    reset_for_tests();
    let user = "paid-two-windows";
    with_user_tiers_mut(|m| {
        m.clear();
        m.insert(user.to_string(), "paid".to_string());
    });

    assert!(rate_limiter(user));
    assert!(rate_limiter(user));
    assert!(!rate_limiter(user));
    thread::sleep(Duration::from_secs(WINDOW_SECONDS + 1));
    assert!(rate_limiter(user));
    assert!(rate_limiter(user));
    assert!(!rate_limiter(user));
    thread::sleep(Duration::from_secs(WINDOW_SECONDS + 1));
    assert!(rate_limiter(user));
    assert!(rate_limiter(user));
    assert!(!rate_limiter(user));
}

// --- EXTRA CREDIT: Free user upgrades to paid ---
// After upgrade, paid rules apply and past requests (made when free) must count
// toward the paid 2-per-window limit, so the user does NOT get a fresh paid window.
#[test]
fn extra_credit_free_upgrades_to_paid() {
    reset_for_tests();
    let user = "upgrade-user-1";
    with_user_tiers_mut(|m| {
        m.clear();
        m.insert(user.to_string(), "free".to_string());
    });

    assert!(rate_limiter(user), "first free request expected true");
    assert!(rate_limiter(user), "second free request expected true");

    with_user_tiers_mut(|m| {
        m.insert(user.to_string(), "paid".to_string());
    });

    // Paid limit is 2 per window; we already have 2 in this window
    assert!(
        !rate_limiter(user),
        "expected false after upgrade (past requests count)"
    );

    thread::sleep(Duration::from_secs(WINDOW_SECONDS + 1));

    assert!(rate_limiter(user), "expected true after window");
    assert!(rate_limiter(user), "expected true");
    assert!(!rate_limiter(user), "expected false");
}

// --- EXTRA CREDIT: Constants respected (implementation must use FREE_LIMIT) ---
#[test]
fn extra_credit_constants_respected() {
    reset_for_tests();
    let user = "free-constants-check";
    with_user_tiers_mut(|m| {
        m.clear();
        m.insert(user.to_string(), "free".to_string());
    });
    let mut allowed = 0;
    for _ in 0..FREE_LIMIT + 2 {
        if rate_limiter(user) {
            allowed += 1;
        }
    }
    assert_eq!(FREE_LIMIT, allowed, "expected exactly FREE_LIMIT allowed");
}

// --- HARD MODE: Concurrency safety ---
// Ignored by default; run with `cargo test -- --include-ignored` (or
// HARD_MODE=1 ./scripts/test.sh rust). In Rust the compiler forces shared state
// to be Sync, so the lesson is choosing and scoping the lock (Mutex/RwLock,
// per-user vs. global) — and the naive check-then-record still over-admits if
// the lock is released between the check and the record.

/// Releases `n` threads at once against the same user; returns how many were allowed.
fn count_concurrent_allowed(user: &'static str, n: usize) -> usize {
    let barrier = Arc::new(Barrier::new(n));
    let handles: Vec<_> = (0..n)
        .map(|_| {
            let barrier = Arc::clone(&barrier);
            thread::spawn(move || {
                barrier.wait();
                rate_limiter(user)
            })
        })
        .collect();
    handles
        .into_iter()
        .map(|h| h.join().expect("rate limiter panicked under concurrency"))
        .filter(|&allowed| allowed)
        .count()
}

// HARD MODE: 100 concurrent requests for one paid user — exactly PAID_LIMIT may succeed.
#[test]
#[ignore = "HARD MODE: run with `cargo test -- --include-ignored`"]
fn hard_mode_paid_user_concurrent_requests() {
    reset_for_tests();
    let user = "paid-concurrent-1";
    with_user_tiers_mut(|m| {
        m.clear();
        m.insert(user.to_string(), "paid".to_string());
    });

    assert_eq!(
        PAID_LIMIT,
        count_concurrent_allowed(user, 100),
        "expected exactly PAID_LIMIT allowed under concurrency"
    );
}

// HARD MODE: 100 concurrent requests for one free user — exactly FREE_LIMIT may succeed.
#[test]
#[ignore = "HARD MODE: run with `cargo test -- --include-ignored`"]
fn hard_mode_free_user_concurrent_requests() {
    reset_for_tests();
    let user = "free-concurrent-1";
    with_user_tiers_mut(|m| {
        m.clear();
        m.insert(user.to_string(), "free".to_string());
    });

    assert_eq!(
        FREE_LIMIT,
        count_concurrent_allowed(user, 100),
        "expected exactly FREE_LIMIT allowed under concurrency"
    );
}
