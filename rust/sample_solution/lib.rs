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

//! API rate limiter: free = 5 requests ever, paid = 2 per 5s window.

use std::collections::HashMap;
use std::sync::{Mutex, OnceLock};
use std::time::{Duration, Instant};

// Constants (used by implementation and tests)
pub const FREE_LIMIT: usize = 5;
pub const PAID_LIMIT: usize = 2;
pub const WINDOW_SECONDS: u64 = 5;

static USER_TIERS: OnceLock<Mutex<HashMap<String, String>>> = OnceLock::new();

// Every allowed request's timestamp per user. Keeping the full history makes
// the free lifetime cap and the upgrade extra credit (past free requests count
// toward the paid window) fall out naturally. To bound memory, store only the
// lifetime count plus the most recent PAID_LIMIT timestamps.
//
// The Mutex makes the whole check-then-record atomic, so concurrent callers
// admit exactly the limit (hard mode). A per-user lock would reduce
// contention; one global lock is the simplest correct choice.
static HISTORY: OnceLock<Mutex<HashMap<String, Vec<Instant>>>> = OnceLock::new();

fn user_tiers() -> &'static Mutex<HashMap<String, String>> {
    USER_TIERS.get_or_init(|| Mutex::new(HashMap::new()))
}

fn history() -> &'static Mutex<HashMap<String, Vec<Instant>>> {
    HISTORY.get_or_init(|| Mutex::new(HashMap::new()))
}

/// Clears user tiers and request timestamps. For use in tests only.
pub fn reset_for_tests() {
    user_tiers().lock().unwrap().clear();
    history().lock().unwrap().clear();
}

/// Allows tests to set or change a user's tier. Call with a closure that mutates the map.
pub fn with_user_tiers_mut<F, R>(f: F) -> R
where
    F: FnOnce(&mut HashMap<String, String>) -> R,
{
    f(&mut *user_tiers().lock().unwrap())
}

/// Returns true if the request is allowed, false if rate limited.
pub fn rate_limiter(user_id: &str) -> bool {
    // Read the tier first and drop that lock before taking the history lock
    // (consistent ordering, never held together — no deadlock).
    let is_paid = user_tiers()
        .lock()
        .unwrap()
        .get(user_id)
        .map(|t| t == "paid")
        .unwrap_or(false); // Unknown users default to free.

    let mut history = history().lock().unwrap();
    let calls = history.entry(user_id.to_string()).or_default();

    let allowed = if is_paid {
        let window = Duration::from_secs(WINDOW_SECONDS);
        calls.iter().filter(|t| t.elapsed() < window).count() < PAID_LIMIT
    } else {
        calls.len() < FREE_LIMIT
    };

    if allowed {
        calls.push(Instant::now());
    }
    allowed
}
