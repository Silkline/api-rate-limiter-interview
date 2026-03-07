# API Rate Limiter — Rust

Rust implementation of the API rate limiter. See [SPEC.md](../SPEC.md) for the full problem and requirements.

## Setup (VS Code / GitHub Codespaces)

1. Open this folder in VS Code (or open the repo in [GitHub Codespaces](https://github.com/features/codespaces)).
2. Ensure Rust is installed (`rustc --version`, or install from https://rustup.rs/).
3. No external dependencies; the crate uses the standard library only.

## Run tests

From the `rust` directory:

```bash
cargo test
```

With verbose output:

```bash
cargo test -- --nocapture
```

Tests include one that sleeps for the paid window (5+ seconds); total test time is about 15–20 seconds.

## User tier map

Tests set a user's tier via `rate_limiter::with_user_tiers_mut()` (e.g. `with_user_tiers_mut(|m| { m.insert("user1".into(), "free".into()); })`). The rate limiter reads from this map to decide which limits apply. Call `rate_limiter::reset_for_tests()` at the start of each test to clear state.

## Extra credit

The test **`extra_credit_free_upgrades_to_paid`** is optional. It verifies that when a free user is upgraded to paid, past requests (made when free) still count toward the paid 2-per-window limit. It is clearly labeled as extra credit in the test name and comment.
