# API Rate Limiter — Go

Go implementation of the API rate limiter. See [SPEC.md](../SPEC.md) for the full problem and requirements.

## Setup (VS Code / GitHub Codespaces)

1. Open this folder in VS Code (or open the repo in [GitHub Codespaces](https://github.com/features/codespaces)).
2. Ensure Go 1.21+ is installed (`go version`).
3. Dependencies are in the standard library; no `go get` required.

## Run tests

From the `go` directory:

```bash
go test -v
```

With timeout (the suite sleeps through several 5-second windows once implemented):

```bash
go test -v -timeout=120s
```

## User tier map

Tests set a user's tier via the package-level `UserTiers` map (e.g. `UserTiers["user1"] = Free`). The rate limiter reads from this map to decide which limits apply.

## Extra credit

The test **`TestRateLimiter_ExtraCredit_FreeUpgradesToPaid`** is optional. It verifies that when a free user is upgraded to paid, past requests (made when free) still count toward the paid 2-per-window limit. It is clearly labeled as extra credit in the test name and comment.

## Hard mode: concurrency

The tests **`TestRateLimiter_HardMode_*`** are skipped by default. They fire 100 concurrent requests for one user and require that **exactly** the limit is allowed — the naive check-then-record pattern races and over-admits (or crashes on concurrent map writes). Enable them with the race detector:

```bash
HARD_MODE=1 go test -v -race -timeout=120s
```

or from the repo root: `HARD_MODE=1 ./scripts/test.sh go`.
