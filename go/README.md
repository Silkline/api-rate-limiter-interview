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

With timeout (for the test that sleeps for the window):

```bash
go test -v -timeout=15s
```

## User tier map

Tests set a user's tier via the package-level `UserTiers` map (e.g. `UserTiers["user1"] = Free`). The rate limiter reads from this map to decide which limits apply.

## Extra credit

The test **`TestRateLimiter_ExtraCredit_FreeUpgradesToPaid`** is optional. It verifies that when a free user is upgraded to paid, past requests (made when free) still count toward the paid 2-per-window limit. It is clearly labeled as extra credit in the test name and comment.
