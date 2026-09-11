# Interview guide

Use this with candidates for the API rate limiter exercise.

## For interviewers

**Before the session (5 minutes)**

1. Ask the candidate which language they will use.
2. Pre-flight the environment they will use. On your machine, in a Codespace, or on theirs:

   ```bash
   ./scripts/verify.sh <language>
   ```

   It installs dependencies, compiles, and runs the always-passing `harness smoke` test. It prints
   `Verify OK` when the toolchain works. It does **not** require an implementation.
3. Share the repo (clone URL, a Codespaces link, or a VS Code Live Share session; `.vscode/settings.json`
   already allows guests to run tasks and debug).

**During the session**

- Point the candidate at [SPEC.md](SPEC.md) §1 and the file to implement (see the table in
  [README.md](README.md)). Time box: about 30–45 minutes.
- Core tests first, then extra credit if time allows. The extra-credit tests are named
  `extra credit` / `extraCredit_` / `extra_credit_` in every language.
- The candidate can run `./scripts/test.sh <language>` as often as they like. A full run takes about 40 seconds
  because the paid-window tests really wait for the window. Suggest running a single test while iterating
  (see the language README for the filter flag).

**After the session**

- Run `./scripts/test.sh <language>` once more to confirm which tests pass.
- Optional: change `PAID_LIMIT` or `FREE_LIMIT` in the constants file and re-run. The tests derive every
  expectation from the constants, so a correct implementation still passes and a hard-coded one fails.

## For candidates

1. **Read the problem** in [SPEC.md](SPEC.md) §1.
2. **Pick one language** from the table in [README.md](README.md) and note the file to implement.
3. **Check your environment:** `./scripts/verify.sh <language>` (or open the repo in GitHub Codespaces, where
   everything is pre-installed).
4. **Implement** `rateLimiter` so the core tests pass: free users get `FREE_LIMIT` requests ever; paid users get
   `PAID_LIMIT` per `WINDOW_SECONDS`-second window. Read the constants at call time rather than copying them.
5. **Run the tests often:** `./scripts/test.sh <language>`, or the native command below, or the VS Code task
   **Tests: \<Language\>**.
6. **Extra credit** if time allows: the free→paid upgrade test and the constants test, both clearly labelled.

Until you implement the function, every test except `harness smoke` fails. That is expected. If you see a
compile error or a "command not found" instead, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md).

## Quick reference

All script commands run from the repo root. Native commands run from the language folder.

| Language | Verify environment | Run tests (script) | Run tests (native, from the language folder) |
| --- | --- | --- | --- |
| TypeScript | `./scripts/verify.sh typescript` | `./scripts/test.sh typescript` | `npm install && npm test` |
| Go | `./scripts/verify.sh go` | `./scripts/test.sh go` | `go test -v` |
| Python | `./scripts/verify.sh python` | `./scripts/test.sh python` | `python3 -m venv .venv && .venv/bin/pip install -r requirements.txt && .venv/bin/pytest -v` |
| Java | `./scripts/verify.sh java` | `./scripts/test.sh java` | `./mvnw test` (Windows: `mvnw.cmd test`) |
| C# | `./scripts/verify.sh csharp` | `./scripts/test.sh csharp` | `dotnet test` |
| Rust | `./scripts/verify.sh rust` | `./scripts/test.sh rust` | `cargo test -- --test-threads=1` |
| Ruby | `./scripts/verify.sh ruby` | `./scripts/test.sh ruby` | `ruby -Ilib test/rate_limiter_test.rb` |
| Kotlin | `./scripts/verify.sh kotlin` | `./scripts/test.sh kotlin` | `./gradlew test` (Windows: `gradlew.bat test`) |
| All | `./scripts/verify.sh` (lists runtimes) | `./scripts/test.sh all` | |

**Windows:** run the scripts from Git Bash or WSL, or use the native column.
