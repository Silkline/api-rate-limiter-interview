# API Rate Limiter — Problem Design and Requirements

This document is the single source of truth for the interview exercise. Use it when implementing a new language or when verifying behavior.

---

## 1. Problem statement (copy-paste for candidates)

Implement a simple API rate limiter.

- **Free users** get **5 total requests ever** (lifetime cap). After 5 requests, all future requests are denied.
- **Paid users** get **2 requests per 5-second window**. After 2 requests in a window, deny until the window passes, then allow again.
- The numbers 5, 2 and 5 seconds are the constants `FREE_LIMIT`, `PAID_LIMIT` and `WINDOW_SECONDS`; read them rather than hard-coding.

Use a **Map** (or equivalent) from user ID to tier (`'free'` | `'paid'`) to determine which limits apply. Expose a single function that takes a user ID and returns whether the request is allowed (`true`) or rate limited (`false`).

**Interview:** Aim for ~30–45 minutes; focus on core behavior first, then extra credit if time allows.

---

## 2. Requirements (authoritative spec)

- **API:** One function: `rateLimiter(userId)` returns `true` (allowed) or `false` (rate limited).
- **User tier:** Include a Map (or language equivalent) from user ID to tier (`'free'` | `'paid'`). The rate limiter consults this map to apply the correct limit. Tests must be able to set or change a user's tier (e.g. for the upgrade scenario).
- **Constants:** `FREE_LIMIT` (default 5, lifetime cap for free users), `PAID_LIMIT` (default 2), `WINDOW_SECONDS` (default 5, for paid users only). Defined in code and referenced in tests so values can be changed.
- **Behavior:** In-memory state per user. **Free:** lifetime request count; once it reaches `FREE_LIMIT`, always deny. **Paid:** at most `PAID_LIMIT` requests in any `WINDOW_SECONDS`-second window; window resets. No thread-safety requirement for the interview.
- **Unknown user:** a user ID with no entry in the tier map may be denied (`false`); tests never rely on it.
- **Window semantics:** a sliding window (keep the timestamps of recent requests) or a per-user fixed window that starts at the user's first request are both acceptable. Avoid fixed windows aligned to the wall clock (e.g. `floor(now / WINDOW_SECONDS)`): a burst of requests can straddle a boundary and make the tests flaky.
- **Read constants at call time.** Do not copy `FREE_LIMIT` into another variable at import time; the Python and Ruby suites override it at runtime.

---

## 3. Test requirements (what every language must verify)

Every language ships the same eleven tests, in the same order, with the same names (adapted to the language's naming convention). **Every expectation is derived from the constants**: no test hard-codes 5, 2 or 5 seconds, so changing a constant changes what the suite expects. Tests that wait for the window sleep `WINDOW_SECONDS + 1` seconds for real; a full run takes about `6 × (WINDOW_SECONDS + 1)` seconds.

| # | Test | What it verifies |
| --- | --- | --- |
| 1 | **harness smoke** | Always passes, even with the stub: constants are positive, the tier map starts empty, the function returns a boolean. Distinguishes "environment broken" from "not implemented". `./scripts/test.sh <lang> smoke` runs only this test. |
| 2 | **free: allows then denies** | First `FREE_LIMIT` calls return `true`; the next two return `false`. |
| 3 | **free: never resets** | After exhausting `FREE_LIMIT`, waiting past a window does **not** help: still `false`. |
| 4 | **paid: allows then denies** | First `PAID_LIMIT` calls in a window return `true`; the next returns `false`. |
| 5 | **paid: allows again after window** | `PAID_LIMIT` allowed, denied, wait, `PAID_LIMIT` allowed again, denied. |
| 6 | **paid: single request then window expiry** | One request, wait, then a full `PAID_LIMIT` allowed then denied (the old request expired). |
| 7 | **paid: two full windows** | Three consecutive windows of `PAID_LIMIT` allowed then denied, with a wait between windows. |
| 8 | **isolation: free users** | Free user A exhausts `FREE_LIMIT`; free user B still gets `FREE_LIMIT`. |
| 9 | **isolation: paid users** | Paid user A exhausts `PAID_LIMIT`; paid user B still gets `PAID_LIMIT` in the same window. |
| 10 | **EXTRA CREDIT: free upgrades to paid** | User makes `PAID_LIMIT` requests as free; tier is changed to paid in the map; the next request is denied because the requests made while free count toward the current paid window; after the window, `PAID_LIMIT` allowed again. Skipped if `PAID_LIMIT > FREE_LIMIT`. |
| 11 | **EXTRA CREDIT: constants respected** | Python and Ruby override `FREE_LIMIT` at runtime and assert the new limit is honoured. In languages whose constants cannot be overridden (TypeScript, Go, Java, C#, Rust, Kotlin) the test counts exactly `FREE_LIMIT` allowed and the README tells you to change the constant in source and re-run. |

Extra-credit tests must be clearly labelled as such in the test file (name and comment) and in the language README.

---

## 4. Adding a new language

Step-by-step instructions for agents and maintainers:

1. **Create a new folder** (e.g. `rust/`, `ruby/`) at repo root.

2. **Add the rate limiter function** with the same contract: one argument (user identifier), returns boolean. Use idiomatic naming for the language.

3. **Define the user tier Map** (userId → `'free'` | `'paid'`) and constants `FREE_LIMIT`, `PAID_LIMIT`, `WINDOW_SECONDS`; use them in both implementation and tests.

4. **Implement in-memory state per user:** **free** = lifetime count; **paid** = request timestamps (or equivalent) for a 5-second window. Rate limiter looks up tier from the map.

5. **Add tests** that mirror the eleven tests in §3, in the same order, with helper functions `expectAllowed(user, count, label)` / `expectDenied(user, label)` so every expectation loops over the constants. Include the always-passing **harness smoke** test and both **extra-credit** tests, labelled as such. Use `python/test_rate_limiter.py` as the reference shape.

6. **Add a README** following the existing ones: a table with the file to implement, constants file, tier map, test file and runtime requirement; setup; run commands (full suite and single-test filter); the "every test except smoke fails on a fresh clone" note; the extra-credit test names.

6a. **Wire the scripts:** add `install_<lang>`, `run_<lang>` (with a `smoke` branch that runs only the smoke test) to `scripts/install.sh` / `scripts/test.sh`, a `compile` case to `scripts/verify.sh`, a `scripts/run-<lang>-tests.sh` for VS Code, tasks in `.vscode/tasks.json`, a launch config, and a matrix entry in `.github/workflows/test.yml`. Then run `./scripts/verify.sh <lang>` on a fresh clone.

7. **Add .gitignore for the new language:** Either add entries to the root [.gitignore](.gitignore) or create `<lang>/.gitignore`. Ignore: dependency/install dirs (e.g. `node_modules/`, `vendor/`, `.venv/`), build/output dirs (e.g. `dist/`, `target/`, `bin/`, `obj/`), caches (e.g. `.pytest_cache/`, `.turbo`), and IDE/project files if desired. Reference existing language sections in the root .gitignore or existing `<lang>/.gitignore` files for patterns.

8. **Update root [README.md](README.md)** to link the new folder. Optionally add the language to the "Languages" table in this SPEC for consistency.

9. **VS Code extensions:** Add the language's VS Code extension to both [.devcontainer/devcontainer.json](.devcontainer/devcontainer.json) (`customizations.vscode.extensions`) and [.vscode/extensions.json](.vscode/extensions.json) (`recommendations`) so the two lists stay identical. The dev container list auto-installs in Codespaces/Reopen in Container; the workspace recommendations prompt local users to install.

### Per-language outline (reference)

| Language   | Function signature (conceptual)          | Test runner / framework      |
| ---------- | ---------------------------------------- | ---------------------------- |
| TypeScript | `rateLimiter(userId: string): boolean`   | Jest or Vitest               |
| Go         | `func RateLimiter(userID string) bool`   | `go test`                    |
| Python     | `def rate_limiter(user_id: str) -> bool` | pytest                       |
| Java       | `boolean rateLimiter(String userId)`     | JUnit 5 (Maven wrapper)       |
| C#         | `bool AllowRequest(string userId)` (a member cannot share its class's name) | xUnit (dotnet test) |
| Rust       | `fn rate_limiter(user_id: &str) -> bool`  | `cargo test`                 |
| Ruby       | `def rate_limiter(user_id)`              | Minitest (bundled with Ruby)  |
| Kotlin     | `fun rateLimiter(userId: String): Boolean` | JUnit 5 (Gradle wrapper)      |
