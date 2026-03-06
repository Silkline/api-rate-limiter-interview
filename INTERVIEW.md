# Interview guide

Use this with candidates for the API rate limiter exercise.

## For interviewers

- **Before the interview:** Share the repo (or a Codespaces link). Optionally use **GitHub Codespaces** so the candidate gets a pre-built environment (Node, Go, Python, Java, C# and dependencies installed via the devcontainer).
- **Problem:** Point candidates to [SPEC.md](SPEC.md) (or the problem statement in §1 there). They implement in **one** language of their choice.
- **Time:** Suggest 30–45 minutes; adjust for your process. Aim for ~30–45 minutes; focus on core behavior first, then extra credit if time allows.
- **Running tests:** Candidates can run tests anytime to check their work. Core tests must pass; the free→paid upgrade test is **extra credit** and should be clearly labeled.
- **Verification:** After the session, run `./scripts/test.sh <lang>` (or use VS Code **Run Task → Tests: &lt;language&gt;** or **Tests: All**) to confirm tests pass.

## For candidates

1. **Read the problem** in [SPEC.md](SPEC.md) (§1 Problem statement).
2. **Pick one language** (TypeScript, Go, Python, Java, or C#) and open that folder.
3. **Set up** (if not using Codespaces):
   - From repo root: `./scripts/install.sh <language>`  
   - Or follow the README in that language’s folder.
4. **Implement** the rate limiter and user-tier Map so the **core** tests pass (free: 5 ever, paid: 2 per 5s window).
5. **Run tests** often:
   - From repo root: `./scripts/test.sh <language>`
   - Or from the language folder: `npm test` / `go test` / `pytest` / `mvn test` / `dotnet test`
   - Or in VS Code: **Terminal → Run Task → Tests: &lt;language&gt;**
6. **Extra credit:** Implement the free→paid upgrade behavior so the labeled extra-credit test passes (past requests count toward the paid window).

## Quick reference

| Language   | Install (from repo root)     | Run tests (from repo root)   |
| ---------- | ----------------------------- | ---------------------------- |
| TypeScript | `./scripts/install.sh typescript` | `./scripts/test.sh typescript` |
| Go         | `./scripts/install.sh go`    | `./scripts/test.sh go`       |
| Python     | `./scripts/install.sh python`| `./scripts/test.sh python`   |
| Java       | `./scripts/install.sh java`  | `./scripts/test.sh java`     |
| C#         | `./scripts/install.sh csharp`| `./scripts/test.sh csharp`   |
| All        | `./scripts/install.sh all`   | `./scripts/test.sh all`      |

**Windows:** If you don’t have Bash, run the equivalent commands from each language’s README (e.g. `cd typescript && npm install && npm test`).
