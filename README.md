# API Rate Limiter Interview

A small API rate limiter exercise used for interviews. This repo is a monorepo: each subfolder is a self-contained implementation in a different language.

- **Problem and requirements:** [SPEC.md](SPEC.md)
- **Interview flow and tips:** [INTERVIEW.md](INTERVIEW.md)

## Spec summary

- **Free users:** 5 total requests ever (lifetime cap). Constants are configurable.
- **Paid users:** 2 requests per 5-second window; window resets.
- **API:** One function `rateLimiter(userId)` returns `true` (allowed) or `false` (rate limited). A Map from user ID to tier (`'free'` | `'paid'`) determines which limits apply.

## Languages

| Language   | Setup & tests |
| ---------- | -------------- |
| [TypeScript](typescript/README.md) | [typescript/README.md](typescript/README.md) |
| [Go](go/README.md)                 | [go/README.md](go/README.md)                 |
| [Python](python/README.md)         | [python/README.md](python/README.md)         |
| [Java](java/README.md)             | [java/README.md](java/README.md)             |
| [C#](csharp/README.md)             | [csharp/README.md](csharp/README.md)         |
| [Rust](rust/README.md)             | [rust/README.md](rust/README.md)             |
| [Ruby](ruby/README.md)             | [ruby/README.md](ruby/README.md)             |
| [Kotlin](kotlin/README.md)         | [kotlin/README.md](kotlin/README.md)         |

**Open in VS Code or [GitHub Codespaces](https://github.com/features/codespaces):** If you use Codespaces (or Dev Containers), the [.devcontainer](.devcontainer/devcontainer.json) will install Node, Go, Python, Java, .NET, Rust, Ruby, and Kotlin (Gradle) and run `./scripts/install.sh all` so the repo is ready to go. Otherwise, open the folder for your language and install dependencies (see below or that folder’s README).

**Install and test from the command line (macOS/Linux):** From the repo root, run `./scripts/install.sh <language>` to install dependencies for one language (or `all`), then `./scripts/test.sh <language>` to run tests. See [INTERVIEW.md](INTERVIEW.md) for a quick-reference table. On Windows without Bash, use the commands in each language’s README. **Problems?** See [TROUBLESHOOTING.md](TROUBLESHOOTING.md).

## Running tests from VS Code

- **Tasks:** **Terminal → Run Task…** (or `Cmd+Shift+B` / `Ctrl+Shift+B`), then choose **Tests: TypeScript**, **Tests: Go**, **Tests: Python**, **Tests: Java**, **Tests: C#**, **Tests: Rust**, **Tests: Ruby**, **Tests: Kotlin**, or **Tests: All** to run every language’s tests.
- **Run and Debug:** Open the **Run and Debug** view (`Cmd+Shift+D` / `Ctrl+Shift+D`), pick a “Run … tests” configuration from the dropdown, and press F5 to run that language’s tests.
- **Recommended extensions:** When opening in VS Code or Codespaces, accept the recommended extensions (TypeScript/ESLint, Go, Python, Java, C#, Vitest, Rust, Ruby, Kotlin) for the best experience.

## Scripts (repo root)

| Script | Purpose |
|--------|--------|
| `./scripts/install.sh [lang\|all]` | Install dependencies for one language or all. |
| `./scripts/test.sh [lang\|all]` | Run tests for one language or all. |
| `./scripts/verify.sh [lang]` | Check runtimes; with a language, install and run that language’s tests. |

Languages: `typescript`, `go`, `python`, `java`, `csharp`, `rust`, `ruby`, `kotlin`.
