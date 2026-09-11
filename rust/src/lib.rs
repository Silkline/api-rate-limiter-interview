//! API rate limiter.
//!
//! - Free users: `FREE_LIMIT` requests total, ever (lifetime cap; never resets).
//! - Paid users: `PAID_LIMIT` requests per `WINDOW_SECONDS`-second window; the window resets.
//!
//! Implement [`rate_limiter`] below. Everything else in this file is scaffolding used by the tests.

use std::collections::HashMap;
use std::sync::{Mutex, MutexGuard, OnceLock};

// Constants (used by the implementation AND the tests; change them here to change the rules).
pub const FREE_LIMIT: usize = 5;
pub const PAID_LIMIT: usize = 2;
pub const WINDOW_SECONDS: u64 = 5;

/// Map from user ID to tier ("free" | "paid"). Tests set this; the rate limiter reads it.
static USER_TIERS: OnceLock<Mutex<HashMap<String, String>>> = OnceLock::new();

/// Locks and returns the user-tier map. Use this from your implementation to look up a tier:
/// `let tier = user_tiers().get(user_id).cloned();`
pub fn user_tiers() -> MutexGuard<'static, HashMap<String, String>> {
    USER_TIERS
        .get_or_init(|| Mutex::new(HashMap::new()))
        .lock()
        .unwrap_or_else(|poisoned| poisoned.into_inner())
}

/// Lets tests set or change a user's tier. Call with a closure that mutates the map.
pub fn with_user_tiers_mut<F, R>(f: F) -> R
where
    F: FnOnce(&mut HashMap<String, String>) -> R,
{
    f(&mut user_tiers())
}

/// Clears the user-tier map. Called at the start of every test.
/// (It cannot clear your own per-user state; the tests use a unique user ID per test instead.)
pub fn reset_for_tests() {
    user_tiers().clear();
}

/// Returns true if the request is allowed, false if it is rate limited.
///
/// TODO: implement per SPEC.md.
///   - Look up the user's tier with `user_tiers().get(user_id)`.
///   - "free": allow the first FREE_LIMIT requests ever, then always deny.
///   - "paid": allow at most PAID_LIMIT requests per WINDOW_SECONDS-second window.
/// Keep per-user state in memory (e.g. another `static OnceLock<Mutex<HashMap<..>>>`).
pub fn rate_limiter(user_id: &str) -> bool {
    // Stub: replace with your implementation.
    let _ = user_id;
    false
}
