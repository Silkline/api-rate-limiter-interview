#!/usr/bin/env bash
# Install dependencies for one language or all. Run from repo root.
# Usage: ./scripts/install.sh [typescript|go|python|java|csharp|all]

set -e
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

LANG="${1:-all}"

install_typescript() {
  if ! command -v node &>/dev/null; then
    echo "Node.js not found. Install from https://nodejs.org/ (LTS, 18+)."
    return 1
  fi
  echo "Installing TypeScript dependencies..."
  (cd typescript && npm install)
}

install_go() {
  if ! command -v go &>/dev/null; then
    echo "Go not found. Install from https://go.dev/dl/."
    return 1
  fi
  echo "Go: no external deps (stdlib only). Downloading modules..."
  (cd go && go mod download)
}

install_python() {
  if ! command -v python3 &>/dev/null; then
    echo "Python 3 not found. Install from https://www.python.org/."
    return 1
  fi
  echo "Installing Python dependencies..."
  if [ -d "python/.venv" ]; then
    python/.venv/bin/pip install -r python/requirements.txt -q
  else
    (cd python && python3 -m venv .venv && .venv/bin/pip install -r requirements.txt -q)
  fi
}

install_java() {
  if ! command -v mvn &>/dev/null; then
    echo "Maven not found. Install from https://maven.apache.org/ or use SDKMAN."
    return 1
  fi
  echo "Installing Java dependencies (Maven)..."
  (cd java && mvn dependency:resolve -q)
}

install_csharp() {
  if ! command -v dotnet &>/dev/null; then
    echo ".NET SDK not found. Install from https://dotnet.microsoft.com/download."
    return 1
  fi
  echo "Restoring C# packages..."
  (cd csharp && dotnet restore -q)
}

case "$LANG" in
  typescript) install_typescript ;;
  go)         install_go ;;
  python)     install_python ;;
  java)       install_java ;;
  csharp)     install_csharp ;;
  all)
    for lang in typescript go python java csharp; do
      echo "--- $lang ---"
      "install_$lang" || true
    done
    ;;
  *)
    echo "Usage: $0 [typescript|go|python|java|csharp|all]"
    exit 1
    ;;
esac

echo "Done."
