# Troubleshooting

## Node not found

Install Node.js 18+ from [nodejs.org](https://nodejs.org/) or use [nvm](https://github.com/nvm-sh/nvm). From the repo root, run:

```bash
./scripts/install.sh typescript
```

## Python: use a venv

Create and use a virtual environment, then install dependencies:

```bash
cd python
python3 -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

Or from the repo root, run `./scripts/install.sh python`, which creates or uses `python/.venv` for you.

## Maven not found

Install Maven from [maven.apache.org](https://maven.apache.org/) or via [SDKMAN](https://sdkman.io/) or `brew install maven`. Ensure `mvn` is on your PATH. Then from the repo root:

```bash
./scripts/install.sh java
```

**Run and Debug → Run Java tests** uses [scripts/run-java-tests.sh](scripts/run-java-tests.sh), which adds common Maven paths (`/opt/homebrew/bin`, SDKMAN, `/usr/bin`) so it works in the devcontainer and on macOS. If you still get "Can't find Node.js binary 'mvn'", use **Terminal → Run Task → Tests: Java** or `./scripts/test.sh java`.

## Go / dotnet not found

- **Go:** Install from [go.dev/dl](https://go.dev/dl/). Run `./scripts/install.sh go`.
- **.NET:** Install the SDK from [dotnet.microsoft.com/download](https://dotnet.microsoft.com/download). Run `./scripts/install.sh csharp`.

## C#: "Missing framework Microsoft.NETCore.App" / Test Run Aborted

The C# project targets **.NET 10**. If you see "To install missing framework" or "The following frameworks were found" listing only a different version, install the **.NET 10 SDK** (or the version the project targets):

- **macOS (Homebrew):** `brew install dotnet` (or `dotnet@10` if available).
- **Direct download:** [.NET downloads](https://dotnet.microsoft.com/download) — pick the SDK for the version in `csharp/ApiRateLimiter.csproj` (e.g. 10.0) and your OS/architecture.

After installing, run **Run and Debug → Run C# tests** again, or `./scripts/test.sh csharp` from the repo root. In the devcontainer, the dotnet feature installs the matching version.

## "Configured debug type 'go' is not supported"

This appears when you use **Run and Debug** → "Run Go tests" and the [Go extension](https://marketplace.visualstudio.com/items?itemName=golang.go) is not installed. Use **Terminal → Run Task → Tests: Go** or `./scripts/test.sh go` from the repo root instead. The "Run Go tests" launch config is set to run `go test` without the Go debugger, so it should not require the extension.

## Go debug adapter: "The argument 'file' cannot be empty" / queryGOROOT

If you see `TypeError [ERR_INVALID_ARG_VALUE]: The argument 'file' cannot be empty` or an error in the Go extension's debug adapter when running or debugging Go tests, the extension is trying to use the Go toolchain but can't find it (e.g. Go not in PATH, or GOROOT empty). Fix it by:

1. **Using the launch config:** In **Run and Debug**, choose **"Run Go tests"** (not "Debug Go: Current Test" or a CodeLens "Debug test"). That config runs `go test` directly and does not use the Go debug adapter.
2. **Or run tests without debugging:** **Terminal → Run Task → Tests: Go**, or from the repo root: `./scripts/test.sh go`.
3. **If you need breakpoints in Go:** Install Go and ensure it's on your PATH (`go version` works in a terminal). Restart the editor so the Go extension can find the toolchain.

## Go: "command not found" when using Run and Debug

**Run Go tests** uses [scripts/run-go-tests.sh](scripts/run-go-tests.sh), which adds common Go paths to PATH (`/usr/local/go/bin`, `/go/bin`, `/opt/homebrew/bin`) so it works in the devcontainer and on macOS/Linux. If you still see `go: command not found`:

1. **Install Go** if you haven't: [go.dev/dl](https://go.dev/dl/) or `brew install go` (puts it in `/opt/homebrew/bin` on Apple Silicon).
2. **Terminal → Run Task → Tests: Go** — the integrated terminal uses your shell PATH.
3. **From a terminal:** `./scripts/test.sh go` from the repo root.

In the devcontainer, Go is installed by the Go feature, so **Run Go tests** should work without extra setup.

## Tests fail after clone

Dependencies are not installed by default. From the repo root, run:

```bash
./scripts/install.sh <language>
```

Use `typescript`, `go`, `python`, `java`, or `csharp`. To install all:

```bash
./scripts/install.sh all
```

Then run tests with `./scripts/test.sh <language>` or `./scripts/test.sh all`.
