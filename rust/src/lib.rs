//! API rate limiter: free = 5 requests ever, paid = 2 per 5s window.

use std::cell::RefCell;
use std::collections::HashMap;
use std::sync::OnceLock;

// Constants (used by implementation and tests)
pub const FREE_LIMIT: usize = 5;
pub const PAID_LIMIT: usize = 2;
pub const WINDOW_SECONDS: u64 = 5;

static USER_TIERS: OnceLock<RefCell<HashMap<String, String>>> = OnceLock::new();

fn user_tiers() -> &'static RefCell<HashMap<String, String>> {
    USER_TIERS.get_or_init(|| RefCell::new(HashMap::new()))
}

/// Clears user tiers and request timestamps. For use in tests only.
pub fn reset_for_tests() {
    user_tiers().borrow_mut().clear();
}

/// Allows tests to set or change a user's tier. Call with a closure that mutates the map.
pub fn with_user_tiers_mut<F, R>(f: F) -> R
where
    F: FnOnce(&mut HashMap<String, String>) -> R,
{
    f(&mut *user_tiers().borrow_mut())
}

/// Returns true if the request is allowed, false if rate limited.
/// TODO: Implement per SPEC — free = lifetime cap of FREE_LIMIT, paid = PAID_LIMIT per WINDOW_SECONDS window.
pub fn rate_limiter(user_id: &str) -> bool {
    // Stub: replace with your implementation
    let _ = user_id;
    false
}
