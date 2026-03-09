# API Rate Limiter — Problem Design and Requirements

This document is the single source of truth for the interview exercise. Use it when implementing a new language or when verifying behavior.

---

## 1. Problem statement (copy-paste for candidates)

Implement a simple API rate limiter.

- **Free users** get **5 total requests ever** (lifetime cap). After 5 requests, all future requests are denied.
- **Paid users** get **2 requests per 5-second window**. After 2 requests in a window, deny until the window passes, then allow again.

Use a **Map** (or equivalent) from user ID to tier (`'free'` | `'paid'`) to determine which limits apply. Expose a single function that takes a user ID and returns whether the request is allowed (`true`) or rate limited (`false`).

**Interview:** Aim for ~30–45 minutes; focus on core behavior first, then extra credit if time allows.

---

## 2. Requirements (authoritative spec)

- **API:** One function: `rateLimiter(userId)` returns `true` (allowed) or `false` (rate limited).
- **User tier:** Include a Map (or language equivalent) from user ID to tier (`'free'` | `'paid'`). The rate limiter consults this map to apply the correct limit. Tests must be able to set or change a user's tier (e.g. for the upgrade scenario).
- **Constants:** `FREE_LIMIT` (default 5, lifetime cap for free users), `PAID_LIMIT` (default 2), `WINDOW_SECONDS` (default 5, for paid users only). Defined in code and referenced in tests so values can be changed.
- **Behavior:** In-memory state per user. **Free:** lifetime request count; once it reaches `FREE_LIMIT`, always deny. **Paid:** at most `PAID_LIMIT` requests in any 5-second window (sliding or fixed); window resets. No thread-safety requirement for the interview.

---

## 3. Test requirements (what every language must verify)

- **Free user:** First `FREE_LIMIT` calls ever return `true`; every call after that returns `false` (no reset).
- **Paid user:** First `PAID_LIMIT` calls in a 5s window return `true`, then `false` until the window passes; after window, next requests are allowed again.
- Tests use the same constants as the implementation (or import them) so changing constants still passes/fails correctly.
- **Extra credit (must be clearly labeled in code and README):** Free user upgrades to paid — user makes some requests as free; tier is then changed to paid in the user map; subsequent requests must apply paid rules and must count **past requests** (made when the user was free) toward the paid 2-per-window limit, so the user does not get a fresh paid window on upgrade.

---

## 4. Adding a new language

Step-by-step instructions for agents and maintainers:

1. **Create a new folder** (e.g. `rust/`, `ruby/`) at repo root.

2. **Add the rate limiter function** with the same contract: one argument (user identifier), returns boolean. Use idiomatic naming for the language.

3. **Define the user tier Map** (userId → `'free'` | `'paid'`) and constants `FREE_LIMIT`, `PAID_LIMIT`, `WINDOW_SECONDS`; use them in both implementation and tests.

4. **Implement in-memory state per user:** **free** = lifetime count; **paid** = request timestamps (or equivalent) for a 5-second window. Rate limiter looks up tier from the map.

5. **Add tests** that satisfy the test requirements above (free = lifetime cap, paid = per-window with reset). Include the **extra-credit** upgrade test (free → paid; past requests count toward paid window) and label it clearly as extra credit in the test file and in the README.

6. **Add a README** with: (a) how to open in VS Code / GitHub Codespaces, (b) how to install dependencies and run the test suite.

7. **Add .gitignore for the new language:** Either add entries to the root [.gitignore](.gitignore) or create `<lang>/.gitignore`. Ignore: dependency/install dirs (e.g. `node_modules/`, `vendor/`, `.venv/`), build/output dirs (e.g. `dist/`, `target/`, `bin/`, `obj/`), caches (e.g. `.pytest_cache/`, `.turbo`), and IDE/project files if desired. Reference existing language sections in the root .gitignore or existing `<lang>/.gitignore` files for patterns.

8. **Update root [README.md](README.md)** to link the new folder. Optionally add the language to the "Languages" table in this SPEC for consistency.

9. **VS Code extensions:** Add the language's VS Code extension to both [.devcontainer/devcontainer.json](.devcontainer/devcontainer.json) (`customizations.vscode.extensions`) and [.vscode/extensions.json](.vscode/extensions.json) (`recommendations`) so the two lists stay identical. The dev container list auto-installs in Codespaces/Reopen in Container; the workspace recommendations prompt local users to install.

### Per-language outline (reference)

| Language   | Function signature (conceptual)          | Test runner / framework      |
| ---------- | ---------------------------------------- | ---------------------------- |
| TypeScript | `rateLimiter(userId: string): boolean`   | Jest or Vitest               |
| Go         | `func RateLimiter(userID string) bool`   | `go test`                    |
| Python     | `def rate_limiter(user_id: str) -> bool` | pytest                       |
| Java       | `boolean rateLimiter(String userId)`     | JUnit 5 (Maven or Gradle)     |
| C#         | `bool RateLimiter(string userId)`        | xUnit or NUnit (dotnet test) |
| Rust       | `fn rate_limiter(user_id: &str) -> bool`  | `cargo test`                 |
| Ruby       | `def rate_limiter(user_id)`              | Minitest (ruby / bundle exec) |
| Kotlin     | `fun rateLimiter(userId: String): Boolean` | JUnit 5 (Gradle)              |
