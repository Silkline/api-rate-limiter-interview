# API Rate Limiter — Ruby

Problem and requirements: [SPEC.md](../SPEC.md). Interview flow: [INTERVIEW.md](../INTERVIEW.md).

| | |
| --- | --- |
| **File to implement** | `lib/rate_limiter.rb` → `def rate_limiter(user_id)` |
| **Constants** | same file (`FREE_LIMIT`, `PAID_LIMIT`, `WINDOW_SECONDS`) |
| **User tier map** | `USER_TIERS` (top-level `Hash`, values `"free"` / `"paid"`); tests set it, you read it |
| **Tests** | `test/rate_limiter_test.rb` (Minitest, which ships with Ruby) |
| **Requires** | Ruby 2.6+ (`ruby --version`); no gems to install |

## Setup

Nothing to install. Optionally, from the repo root: `./scripts/verify.sh ruby`.

`Gemfile` / `Gemfile.lock` are provided only for editors that expect them; Bundler is not needed
(and `bundle install` may fail on macOS system Ruby with a permissions error; ignore it).

## Run tests

```bash
cd ruby
ruby -Ilib test/rate_limiter_test.rb                       # full suite, about 40 seconds (paid-window tests really wait)
ruby -Ilib test/rate_limiter_test.rb -n /free_user/        # only tests whose name matches
```

Or from the repo root: `./scripts/test.sh ruby`, or VS Code **Terminal → Run Task → Tests: Ruby**.

On a fresh clone every test except `test_harness_smoke` fails: the function is a stub. That is expected.

## Extra credit (clearly labelled in the test file)

- **`test_extra_credit_free_upgrades_to_paid`** — after a free user is switched to `"paid"` in `USER_TIERS`, requests
  made while free still count toward the current paid window.
- **`test_extra_credit_constants_respected`** — the test redefines `FREE_LIMIT` at runtime, so read the constant on
  every call instead of copying it.
