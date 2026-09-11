# API Rate Limiter — Java

Problem and requirements: [SPEC.md](../SPEC.md). Interview flow: [INTERVIEW.md](../INTERVIEW.md).

| | |
| --- | --- |
| **File to implement** | `src/main/java/com/silkline/ratelimit/RateLimiter.java` → `static boolean rateLimiter(String userId)` |
| **Constants** | `src/main/java/com/silkline/ratelimit/Constants.java` (`FREE_LIMIT`, `PAID_LIMIT`, `WINDOW_SECONDS`) |
| **User tier map** | `RateLimiter.USER_TIERS` (`Map<String, UserTier>`, values `FREE` / `PAID`); tests set it, you read it |
| **Tests** | `src/test/java/com/silkline/ratelimit/RateLimiterTest.java` (JUnit 5) |
| **Requires** | JDK 17+ (`java -version`). Maven is **not** required: `./mvnw` downloads it on first use |

## Setup

```bash
cd java
./mvnw -q dependency:resolve     # Windows: mvnw.cmd -q dependency:resolve
```

Or from the repo root: `./scripts/verify.sh java` (resolves, compiles, and runs the smoke test).

## Run tests

```bash
./mvnw test                                        # full suite, about 40 seconds (paid-window tests really wait)
./mvnw test -Dtest='RateLimiterTest#freeUser*'     # only tests whose name matches
```

Or from the repo root: `./scripts/test.sh java`, or VS Code **Terminal → Run Task → Tests: Java**.
If you already have Maven installed, `mvn test` works too.

On a fresh clone every test except `harnessSmoke` fails: the function is a stub. That is expected.

## Extra credit (clearly labelled in the test file)

- **`extraCredit_freeUpgradesToPaid`** — after a free user is switched to `PAID` in `USER_TIERS`, requests made while
  free still count toward the current paid window.
- **`extraCredit_constantsRespected`** — read `Constants.FREE_LIMIT`; change it and re-run to check.
