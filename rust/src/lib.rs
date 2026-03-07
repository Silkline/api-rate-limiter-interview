//! API rate limiter: free = 5 requests ever, paid = 2 per 5s window.

use std::cell::RefCell;
use std::collections::HashMap;
use std::sync::OnceLock;
use std::time::{SystemTime, UNIX_EPOCH};

// Constants (used by implementation and tests)
pub const FREE_LIMIT: usize = 5;
pub const PAID_LIMIT: usize = 2;
pub const WINDOW_SECONDS: u64 = 5;

static USER_TIERS: OnceLock<RefCell<HashMap<String, String>>> = OnceLock::new();
static REQUEST_TIMESTAMPS: OnceLock<RefCell<HashMap<String, Vec<i64>>>> = OnceLock::new();

fn user_tiers() -> &'static RefCell<HashMap<String, String>> {
    USER_TIERS.get_or_init(|| RefCell::new(HashMap::new()))
}

fn request_timestamps() -> &'static RefCell<HashMap<String, Vec<i64>>> {
    REQUEST_TIMESTAMPS.get_or_init(|| RefCell::new(HashMap::new()))
}

fn get_tier(user_id: &str) -> String {
    user_tiers()
        .borrow()
        .get(user_id)
        .cloned()
        .unwrap_or_else(|| "free".to_string())
}

fn now_ms() -> i64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .expect("time")
        .as_millis() as i64
}

/// Clears user tiers and request timestamps. For use in tests only.
pub fn reset_for_tests() {
    user_tiers().borrow_mut().clear();
    request_timestamps().borrow_mut().clear();
}

/// Allows tests to set or change a user's tier. Call with a closure that mutates the map.
pub fn with_user_tiers_mut<F, R>(f: F) -> R
where
    F: FnOnce(&mut HashMap<String, String>) -> R,
{
    f(&mut *user_tiers().borrow_mut())
}

/// Returns true if the request is allowed, false if rate limited.
pub fn rate_limiter(user_id: &str) -> bool {
    let tier = get_tier(user_id);
    let now = now_ms();
    let window_ms = (WINDOW_SECONDS as i64) * 1000;
    let cutoff = now - window_ms;

    let mut ts = request_timestamps()
        .borrow_mut()
        .entry(user_id.to_string())
        .or_default()
        .clone();

    if tier == "free" {
        if ts.len() >= FREE_LIMIT {
            return false;
        }
        ts.push(now);
        request_timestamps()
            .borrow_mut()
            .insert(user_id.to_string(), ts);
        return true;
    }

    // paid: only count requests in current window
    let in_window = ts.iter().filter(|&&t| t > cutoff).count();
    if in_window >= PAID_LIMIT {
        return false;
    }
    ts.push(now);
    request_timestamps()
        .borrow_mut()
        .insert(user_id.to_string(), ts);
    true
}
