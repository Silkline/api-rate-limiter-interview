#!/usr/bin/env bash
# Install dependencies for one language or all. Run from repo root.
# Usage: ./scripts/install.sh [typescript|go|python|java|csharp|rust|ruby|kotlin|all]

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

install_rust() {
  if ! command -v cargo &>/dev/null; then
    echo "Rust/Cargo not found. Install from https://rustup.rs/."
    return 1
  fi
  echo "Rust: no external deps (stdlib only). Fetching..."
  (cd rust && cargo fetch 2>/dev/null || cargo build --release 2>/dev/null || true)
}

install_ruby() {
  if ! command -v ruby &>/dev/null; then
    echo "Ruby not found. Install from https://www.ruby-lang.org/."
    return 1
  fi
  echo "Installing Ruby dependencies..."
  (cd ruby && bundle install 2>/dev/null || true)
}

install_kotlin() {
  if ! command -v java &>/dev/null; then
    echo "Java (JDK) not found. Install from https://adoptium.net/ or use SDKMAN."
    return 1
  fi
  echo "Installing Kotlin dependencies (Gradle)..."
  (cd kotlin && ./gradlew dependencies --no-daemon -q 2>/dev/null || ./gradlew build -x test --no-daemon -q 2>/dev/null || true)
}

case "$LANG" in
  typescript) install_typescript ;;
  go)         install_go ;;
  python)     install_python ;;
  java)       install_java ;;
  csharp)     install_csharp ;;
  rust)       install_rust ;;
  ruby)       install_ruby ;;
  kotlin)     install_kotlin ;;
  all)
    for lang in typescript go python java csharp rust ruby kotlin; do
      echo "--- $lang ---"
      "install_$lang" || true
    done
    ;;
  *)
    echo "Usage: $0 [typescript|go|python|java|csharp|rust|ruby|kotlin|all]"
    exit 1
    ;;
esac

echo "Done."
