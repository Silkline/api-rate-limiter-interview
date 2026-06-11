# API Rate Limiter — Kotlin

Kotlin implementation of the API rate limiter. See [SPEC.md](../SPEC.md) for the full problem and requirements.

## Setup (VS Code / GitHub Codespaces)

1. Open this folder in VS Code (or open the repo in [GitHub Codespaces](https://github.com/features/codespaces)).
2. Ensure JDK 17+ is installed (`java -version`).
3. The project uses the Gradle wrapper; no need to install Gradle. From the `kotlin` directory run `./gradlew build` to resolve dependencies (optional; `./gradlew test` will do it as well).

## Run tests

From the `kotlin` directory:

```bash
./gradlew test
```

With more output:

```bash
./gradlew test --info
```

Tests include one that sleeps for the paid window (5+ seconds); total test time is about 15–20 seconds.

## User tier map

Tests set a user's tier via `RateLimiter.userTiers` (e.g. `RateLimiter.userTiers["user1"] = UserTier.FREE`). The rate limiter reads from this map to decide which limits apply.

## Extra credit

The test **`extraCredit_freeUpgradesToPaid`** is optional. It verifies that when a free user is upgraded to paid, past requests (made when free) still count toward the paid 2-per-window limit. It is clearly labeled as extra credit in the test name and comment.

## Hard mode: concurrency

The tests **`hardMode_*`** are skipped by default. They release 100 threads at once against one user and require that **exactly** the limit is allowed — the naive check-then-record pattern races between reading the count and recording the request. Enable with:

```bash
HARD_MODE=1 ./gradlew test --no-daemon
```

(`--no-daemon` ensures the environment variable reaches the test JVM even if a Gradle daemon is already running.)
