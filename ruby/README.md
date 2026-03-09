# API Rate Limiter — Ruby

Ruby implementation of the API rate limiter. See [SPEC.md](../SPEC.md) for the full problem and requirements.

## Setup (VS Code / GitHub Codespaces)

1. Open this folder in VS Code (or open the repo in [GitHub Codespaces](https://github.com/features/codespaces)).
2. Ensure Ruby 3.x is installed (`ruby --version`).
3. Install dependencies: `bundle install` (from the `ruby` directory).

## Run tests

From the `ruby` directory:

```bash
bundle exec ruby -Ilib:test test/rate_limiter_test.rb
```

Tests include one that sleeps for the paid window (5+ seconds).

## User tier map

Tests set a user's tier via `USER_TIERS` (e.g. `USER_TIERS["user1"] = "free"`). The rate limiter reads from this map to decide which limits apply. Each test clears `USER_TIERS` in `setup`.

## Extra credit

The test **`test_extra_credit_free_upgrades_to_paid`** is optional. It verifies that when a free user is upgraded to paid, past requests (made when free) still count toward the paid 2-per-window limit. It is clearly labeled as extra credit in the test name and comment.
