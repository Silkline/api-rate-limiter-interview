# API Rate Limiter — Java

Java implementation of the API rate limiter. See [SPEC.md](../SPEC.md) for the full problem and requirements.

## Setup (VS Code / GitHub Codespaces)

1. Open this folder in VS Code (or open the repo in [GitHub Codespaces](https://github.com/features/codespaces)).
2. Ensure Java 17+ and Maven are installed (`java -version`, `mvn -v`).
3. No extra dependencies beyond JUnit (in `pom.xml`).

## Run tests

From the `java` directory:

```bash
mvn test
```

## User tier map

Tests set a user's tier via `RateLimiter.USER_TIERS` (e.g. `RateLimiter.USER_TIERS.put("user1", UserTier.FREE)`). The rate limiter reads from this map to decide which limits apply.

## Extra credit

The test **`extraCredit_freeUpgradesToPaid`** is optional. It verifies that when a free user is upgraded to paid, past requests (made when free) still count toward the paid 2-per-window limit. It is clearly labeled as extra credit in the test name and Javadoc.

## Hard mode: concurrency

The tests **`hardMode_*`** are skipped by default. They release 100 threads at once against one user and require that **exactly** the limit is allowed — the naive check-then-record pattern races between reading the count and recording the request. Enable with:

```bash
HARD_MODE=1 mvn test
```
