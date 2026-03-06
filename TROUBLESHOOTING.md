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

Install Maven from [maven.apache.org](https://maven.apache.org/) or via [SDKMAN](https://sdkman.io/). Ensure `mvn` is on your PATH. Then from the repo root:

```bash
./scripts/install.sh java
```

## Go / dotnet not found

- **Go:** Install from [go.dev/dl](https://go.dev/dl/). Run `./scripts/install.sh go`.
- **.NET:** Install the SDK from [dotnet.microsoft.com/download](https://dotnet.microsoft.com/download). Run `./scripts/install.sh csharp`.

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
