# API Rate Limiter — Rust

Problem and requirements: [SPEC.md](../SPEC.md). Interview flow: [INTERVIEW.md](../INTERVIEW.md).

| | |
| --- | --- |
| **File to implement** | `src/lib.rs` → `pub fn rate_limiter(user_id: &str) -> bool` |
| **Constants** | same file (`FREE_LIMIT`, `PAID_LIMIT`, `WINDOW_SECONDS`) |
| **User tier map** | `user_tiers()` returns a locked `HashMap<String, String>` (values `"free"` / `"paid"`); tests set it via `with_user_tiers_mut`, you read it with `user_tiers().get(user_id)` |
| **Tests** | `tests/rate_limiter_test.rs` (`cargo test`) |
| **Requires** | Rust via [rustup](https://rustup.rs/) (`cargo --version`); standard library only |

## Setup

Nothing to install beyond Rust. Optionally, from the repo root: `./scripts/verify.sh rust`.

## Run tests

```bash
cd rust
cargo test -- --test-threads=1                 # full suite, about 40 seconds (paid-window tests really wait)
cargo test free_user -- --test-threads=1       # only tests whose name matches
cargo test -- --test-threads=1 --nocapture     # show println! output
```

`--test-threads=1` is required: the tests share one global tier map.

Or from the repo root: `./scripts/test.sh rust`, or VS Code **Terminal → Run Task → Tests: Rust**.

On a fresh clone every test except `harness_smoke` fails: the function is a stub. That is expected.

## Extra credit (clearly labelled in the test file)

- **`extra_credit_free_upgrades_to_paid`** — after a free user is switched to `"paid"`, requests made while free still
  count toward the current paid window.
- **`extra_credit_constants_respected`** — read `FREE_LIMIT` from `lib.rs`; change it and re-run to check.
