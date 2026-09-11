#!/usr/bin/env bash
# Install dependencies for one language or all. Run from anywhere; the script cd's to the repo root.
# Usage: ./scripts/install.sh <typescript|go|python|java|csharp|rust|ruby|kotlin|all>
#
# Each installer checks that the runtime exists and prints an install link if not.
# Errors are NOT hidden: if something fails you will see why.

set -euo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

TARGET_LANG="${1:-}"

usage() {
  echo "Usage: $0 <typescript|go|python|java|csharp|rust|ruby|kotlin|all>"
  exit 1
}
[ -n "$TARGET_LANG" ] || usage

need() {
  # need <command> <friendly name> <install url>
  if ! command -v "$1" &>/dev/null; then
    echo "ERROR: $2 not found on PATH. Install from $3"
    return 1
  fi
}

install_typescript() {
  need node "Node.js (18+)" "https://nodejs.org/" || return 1
  echo "Installing TypeScript dependencies (npm install)..."
  (cd typescript && npm install)
}

install_go() {
  need go "Go (1.22+)" "https://go.dev/dl/" || return 1
  echo "Go: standard library only; verifying the module builds..."
  (cd go && go build ./... && go vet ./...)
}

install_python() {
  need python3 "Python 3 (3.10+)" "https://www.python.org/downloads/" || return 1
  echo "Installing Python dependencies into python/.venv..."
  if [ ! -x "python/.venv/bin/pip" ]; then
    if ! python3 -m venv python/.venv; then
      echo "ERROR: could not create a virtual environment."
      echo "On Debian/Ubuntu: sudo apt install python3-venv   then re-run this script."
      return 1
    fi
  fi
  python/.venv/bin/pip install -q -r python/requirements.txt
}

install_java() {
  need java "Java JDK (17+)" "https://adoptium.net/" || return 1
  echo "Resolving Java dependencies with the Maven wrapper (downloads Maven on first run)..."
  (cd java && ./mvnw -q dependency:resolve)
}

install_csharp() {
  need dotnet ".NET SDK (8.0 or newer)" "https://dotnet.microsoft.com/download" || return 1
  echo "Restoring C# packages..."
  (cd csharp && dotnet restore)
}

install_rust() {
  need cargo "Rust (rustup)" "https://rustup.rs/" || return 1
  echo "Rust: standard library only; fetching and building..."
  (cd rust && cargo build --tests)
}

install_ruby() {
  need ruby "Ruby (3.x recommended; 2.6+ works)" "https://www.ruby-lang.org/en/downloads/" || return 1
  echo "Ruby: minitest ships with Ruby; nothing to install."
  ruby -e 'require "minitest"; puts "  minitest #{Minitest::VERSION} OK"'
}

install_kotlin() {
  need java "Java JDK (17+)" "https://adoptium.net/" || return 1
  echo "Resolving Kotlin dependencies with the Gradle wrapper (downloads Gradle on first run; can take a few minutes)..."
  (cd kotlin && ./gradlew --no-daemon -q compileTestKotlin)
}

case "$TARGET_LANG" in
  typescript|go|python|java|csharp|rust|ruby|kotlin) "install_$TARGET_LANG" ;;
  all)
    FAILED=""
    for lang in typescript go python java csharp rust ruby kotlin; do
      echo "--- $lang ---"
      "install_$lang" || FAILED="$FAILED $lang"
    done
    if [ -n "$FAILED" ]; then
      echo ""
      echo "Could not install:$FAILED (missing runtime or install error above)."
      echo "That is fine if you only need one language."
      exit 1
    fi
    ;;
  *) usage ;;
esac

echo "Done: $TARGET_LANG dependencies installed."
