# API Rate Limiter — Go

Problem and requirements: [SPEC.md](../SPEC.md). Interview flow: [INTERVIEW.md](../INTERVIEW.md).

| | |
| --- | --- |
| **File to implement** | `rate_limiter.go` → `func RateLimiter(userID string) bool` |
| **Constants** | `constants.go` (`FREE_LIMIT`, `PAID_LIMIT`, `WINDOW_SECONDS`) |
| **User tier map** | `UserTiers` (package-level `map[string]UserTier`, values `Free` / `Paid`); tests set it, you read it |
| **Tests** | `rate_limiter_test.go` (`go test`) |
| **Requires** | Go 1.22+ (`go version`); standard library only |

## Setup

Nothing to install. Optionally, from the repo root: `./scripts/verify.sh go` (vets and runs the smoke test).

## Run tests

```bash
cd go
go test -v                       # full suite, about 40 seconds (paid-window tests really wait)
go test -v -run 'TestFreeUser'   # only tests whose name matches
```

Or from the repo root: `./scripts/test.sh go`, or VS Code **Terminal → Run Task → Tests: Go**.

On a fresh clone every test except `TestHarnessSmoke` fails: the function is a stub. That is expected.

## Extra credit (clearly labelled in the test file)

- **`TestExtraCredit_FreeUpgradesToPaid`** — after a free user is switched to `Paid` in `UserTiers`, requests made
  while free still count toward the current paid window.
- **`TestExtraCredit_ConstantsRespected`** — read `FREE_LIMIT` from `constants.go`; change it and re-run to check.
