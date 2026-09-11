# Troubleshooting

Start here: from the repo root run

```bash
./scripts/verify.sh <language>
```

It installs dependencies, compiles, and runs the always-passing `harness smoke` test. If it prints
`Verify OK`, your environment works and any remaining failures are in the implementation.
Languages: `typescript`, `go`, `python`, `java`, `csharp`, `rust`, `ruby`, `kotlin`.

## "All the tests fail" right after cloning

Expected. The rate limiter is a stub that returns `false`, so every test except `harness smoke` fails with an
assertion error until you implement it. A **compile error**, **"command not found"**, or a **failing smoke test**
is a real environment problem; see the sections below.

## Runtime not found (`node`, `go`, `python3`, `java`, `dotnet`, `cargo`, `ruby`)

`./scripts/verify.sh` with no argument lists what is installed. Install links:

| Runtime | Install |
| --- | --- |
| Node.js 18+ | https://nodejs.org/ or `nvm install --lts` |
| Go 1.22+ | https://go.dev/dl/ or `brew install go` |
| Python 3.10+ | https://www.python.org/downloads/ or `brew install python` |
| JDK 17+ (Java and Kotlin) | https://adoptium.net/ or `brew install openjdk@17` or `sdk install java` |
| .NET SDK 8+ | https://dotnet.microsoft.com/download or `brew install dotnet` |
| Rust | https://rustup.rs/ |
| Ruby 2.6+ | preinstalled on macOS; otherwise https://www.ruby-lang.org/ or `brew install ruby` |

Maven and Gradle are **not** required: `java/mvnw` and `kotlin/gradlew` download them on first use.

## Windows

The `scripts/*.sh` files need Bash. Use **Git Bash** or **WSL**, or run the native command from the language
folder (see the Quick reference in [INTERVIEW.md](INTERVIEW.md)). Java has `mvnw.cmd`; Kotlin has `gradlew.bat`.
The repo's `.gitattributes` keeps shell scripts LF-terminated, so `\r: command not found` should not occur;
if it does, run `git config core.autocrlf false` and re-clone.

## Python

- **`pip install` refuses to run ("externally-managed-environment")** — Python 3.12+ blocks installing outside a
  virtual environment. `./scripts/install.sh python` creates `python/.venv` for you and `./scripts/test.sh python`
  uses it. Manually:

  ```bash
  cd python
  python3 -m venv .venv
  source .venv/bin/activate      # Windows: .venv\Scripts\activate
  pip install -r requirements.txt
  pytest -v
  ```

- **`python3 -m venv` fails on Debian/Ubuntu** — `sudo apt install python3-venv`.

## Java / Kotlin

- **`./mvnw` or `./gradlew` hangs or is slow the first time** — it downloads Maven (about 10 MB) or Gradle plus the
  Kotlin compiler (about 200 MB). Later runs are fast.
- **`JAVA_HOME is not set and no 'java' command could be found`** — install a JDK (see table) and open a new
  terminal. With SDKMAN: `sdk install java 17.0.10-tem`.
- **`Unsupported class file major version`** — the JDK is older than 17. Install 17 or newer.

## C#

- **"To install missing framework..." / "The framework 'Microsoft.NETCore.App', version '8.0.0' was not found"**
  — the project targets .NET 8 but rolls forward to any newer runtime, so any SDK 8, 9 or 10 works. Install one from
  https://dotnet.microsoft.com/download (`brew install dotnet` on macOS) and run `dotnet --list-sdks` to confirm.
- **`dotnet test` shows no test names** — add `--logger "console;verbosity=normal"`.

## Go

- **`go: command not found` from VS Code Run and Debug** — `scripts/run-go-tests.sh` adds the common install
  paths (`/usr/local/go/bin`, `/opt/homebrew/bin`). If Go is installed elsewhere, use the terminal:
  `./scripts/test.sh go`.
- **"Configured debug type 'go' is not supported"** — the Go extension is not installed. Use
  **Terminal → Run Task → Tests: Go** or `./scripts/test.sh go`; neither needs the extension.
- **Tests time out** — the scripts pass `-timeout=300s`; the suite needs about 40 seconds. If you changed
  `WINDOW_SECONDS` to something large, raise the timeout.

## Rust

- **Tests interfere with each other** — the tests share one global tier map, so they must run single-threaded:
  `cargo test -- --test-threads=1` (the scripts already do this).

## Ruby

- **`bundle install` fails with a permissions error** (macOS system Ruby) — you do not need Bundler. `minitest`
  ships with Ruby: `ruby -Ilib test/rate_limiter_test.rb`.
- **`cannot load such file -- minitest/autorun`** — very old Ruby without bundled minitest. `gem install minitest`
  or install Ruby 3.x.

## TypeScript

- **`vitest: command not found`** — run `npm install` in `typescript/` (or `./scripts/install.sh typescript`).
- **Tests time out** — the timeout is derived from `WINDOW_SECONDS` in `typescript/vitest.config.ts`; a correct
  implementation never gets near it. Check for an infinite loop.

## VS Code

- **Tasks fail with "command not found"** — the integrated terminal inherits your shell PATH. Open a new window
  after installing a runtime.
- **Run and Debug configurations** call `scripts/run-<lang>-tests.sh`, which extend PATH with common install
  locations (Homebrew, SDKMAN, `~/.cargo/bin`, `/usr/local/go/bin`) and then run `./scripts/test.sh <lang>`.

## GitHub Codespaces / Dev Containers

The dev container installs every runtime and runs `scripts/install.sh all`. If the post-create step reports a
failure for a language you do not need, ignore it. To re-run: `./scripts/install.sh <language>`.
