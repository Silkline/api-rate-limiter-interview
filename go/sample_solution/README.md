# ⚠️ Sample solution — interviewer reference only

**Candidates: do not open this folder.** Reading the solution defeats the purpose of the exercise.

Reference Go solution passing core, extra-credit, and hard-mode (concurrency) tests.

**To verify against the tests:** copy `rate_limiter.go` over `../rate_limiter.go` (leave `constants.go` alone — the copy here only exists so this folder compiles standalone), then from `go/`:

```bash
HARD_MODE=1 go test -v -race -timeout=120s
```
