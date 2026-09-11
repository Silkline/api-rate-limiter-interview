# API Rate Limiter — Kotlin

Problem and requirements: [SPEC.md](../SPEC.md). Interview flow: [INTERVIEW.md](../INTERVIEW.md).

| | |
| --- | --- |
| **File to implement** | `src/main/kotlin/com/silkline/ratelimit/RateLimiter.kt` → `fun rateLimiter(userId: String): Boolean` |
| **Constants** | `src/main/kotlin/com/silkline/ratelimit/Constants.kt` (`FREE_LIMIT`, `PAID_LIMIT`, `WINDOW_SECONDS`) |
| **User tier map** | `RateLimiter.userTiers` (`ConcurrentHashMap<String, UserTier>`, values `FREE` / `PAID`); tests set it, you read it |
| **Tests** | `src/test/kotlin/com/silkline/ratelimit/RateLimiterTest.kt` (JUnit 5) |
| **Requires** | JDK 17+ (`java -version`). Gradle is **not** required: `./gradlew` downloads it on first use |

## Setup

```bash
cd kotlin
./gradlew compileTestKotlin      # Windows: gradlew.bat compileTestKotlin
```

The first run downloads Gradle and the Kotlin compiler (a few minutes); later runs are fast.
Or from the repo root: `./scripts/verify.sh kotlin` (compiles and runs the smoke test).

## Run tests

```bash
./gradlew test                                              # full suite, about 40 seconds (paid-window tests really wait)
./gradlew test --tests '*RateLimiterTest.freeUser*'         # only tests whose name matches
```

Each test's pass/fail is printed. Or from the repo root: `./scripts/test.sh kotlin`, or VS Code
**Terminal → Run Task → Tests: Kotlin**.

On a fresh clone every test except `harnessSmoke` fails: the function is a stub. That is expected.

## Extra credit (clearly labelled in the test file)

- **`extraCredit_freeUpgradesToPaid`** — after a free user is switched to `PAID` in `userTiers`, requests made while
  free still count toward the current paid window.
- **`extraCredit_constantsRespected`** — read `Constants.FREE_LIMIT`; change it and re-run to check.
