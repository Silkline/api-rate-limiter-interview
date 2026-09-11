# API Rate Limiter Interview

A small rate-limiter exercise used for interviews. Each subfolder is a self-contained
starter in a different language: a stub function to implement plus a ready-made test suite.

## Quick start (candidates)

1. **Read the problem:** [SPEC.md](SPEC.md) §1 (2 minutes).
2. **Pick one language** and check your environment works (installs dependencies, compiles, runs one always-passing test):

   ```bash
   ./scripts/verify.sh <language>      # typescript | go | python | java | csharp | rust | ruby | kotlin
   ```

3. **Implement** the stub function in the file listed below.
4. **Run the tests** as often as you like:

   ```bash
   ./scripts/test.sh <language>
   ```

> **Expected on a fresh clone:** every test except `harness smoke` fails, because the function is a stub.
> Assertion failures are normal. Compile or tooling errors are not: see [TROUBLESHOOTING.md](TROUBLESHOOTING.md).

| Language | File to implement | Tests | Requires |
| --- | --- | --- | --- |
| [TypeScript](typescript/README.md) | `typescript/src/rateLimiter.ts` | `typescript/tests/rateLimiter.test.ts` | Node 18+ |
| [Go](go/README.md) | `go/rate_limiter.go` | `go/rate_limiter_test.go` | Go 1.22+ |
| [Python](python/README.md) | `python/rate_limiter.py` | `python/test_rate_limiter.py` | Python 3.10+ |
| [Java](java/README.md) | `java/src/main/java/com/silkline/ratelimit/RateLimiter.java` | `java/src/test/java/.../RateLimiterTest.java` | JDK 17+ (Maven is bundled) |
| [C#](csharp/README.md) | `csharp/RateLimiter.cs` | `csharp/RateLimiterTests.cs` | .NET SDK 8+ |
| [Rust](rust/README.md) | `rust/src/lib.rs` | `rust/tests/rate_limiter_test.rs` | Rust (rustup) |
| [Ruby](ruby/README.md) | `ruby/lib/rate_limiter.rb` | `ruby/test/rate_limiter_test.rb` | Ruby 2.6+ (no gems needed) |
| [Kotlin](kotlin/README.md) | `kotlin/src/main/kotlin/com/silkline/ratelimit/RateLimiter.kt` | `kotlin/src/test/kotlin/.../RateLimiterTest.kt` | JDK 17+ (Gradle is bundled) |

Constants (`FREE_LIMIT`, `PAID_LIMIT`, `WINDOW_SECONDS`) live next to the stub in each language. The tests
derive every expectation from them, so you can change them and the suite still checks the right thing.

## The problem in one paragraph

Implement `rateLimiter(userId) -> boolean`. **Free users** get `FREE_LIMIT` requests total, ever (default 5).
**Paid users** get `PAID_LIMIT` requests per `WINDOW_SECONDS`-second window (default 2 per 5s), and the window
resets. A map from user ID to tier (`'free'` | `'paid'`) tells you which rule applies. Full details, test list
and extra credit: [SPEC.md](SPEC.md). Interview flow and tips: [INTERVIEW.md](INTERVIEW.md).

## Ways to run

| How | Command / action |
| --- | --- |
| Terminal (macOS, Linux, WSL, Git Bash) | `./scripts/test.sh <language>` |
| VS Code task | **Terminal → Run Task… → Tests: \<Language\>** (or **Smoke: \<Language\>** for the environment check) |
| VS Code Run and Debug | pick **Run \<Language\> tests**, press F5 |
| Per-language native command | see the table in [INTERVIEW.md](INTERVIEW.md#quick-reference) or the language README |
| GitHub Codespaces / Dev Containers | open the repo; [.devcontainer](.devcontainer/devcontainer.json) installs every runtime and runs `scripts/install.sh all` |

Each suite takes about **40 seconds**: the paid-window tests really wait for the window to pass.

## Scripts (repo root)

| Script | Purpose |
| --- | --- |
| `./scripts/verify.sh [lang]` | No argument: list installed runtimes. With a language: install + compile + smoke test. |
| `./scripts/install.sh <lang\|all>` | Install dependencies for one language or all. |
| `./scripts/test.sh <lang\|all> [smoke]` | Run the tests for one language or all. `smoke` runs only the always-passing harness test. |

**Windows:** run the scripts from Git Bash or WSL, or use the native commands in each language's README
(Kotlin ships `gradlew.bat`, Java ships `mvnw.cmd`).

## Repo layout

- `SPEC.md`: authoritative problem statement, requirements, test list, and how to add a language.
- `INTERVIEW.md`: interviewer and candidate flow.
- `TROUBLESHOOTING.md`: known environment problems and fixes.
- `.github/workflows/test.yml`: CI verifies every harness on a fresh clone (install, compile, smoke test).
