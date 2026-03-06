#!/usr/bin/env bash
# Check that runtimes are available and (optionally) run tests for one language.
# Usage: ./scripts/verify.sh [typescript|go|python|java|csharp]
# With no arg, only checks that at least one runtime is present.

set -e
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

LANG="${1:-}"

check() {
  if command -v "$1" &>/dev/null; then
    echo "  $1: $(command -v "$1")"
    return 0
  fi
  echo "  $1: not found"
  return 1
}

echo "Runtime check:"
ANY=0
check node   && ANY=1
check go     && ANY=1
check python3 && ANY=1
check mvn    && ANY=1
check dotnet && ANY=1

if [ "$ANY" -eq 0 ]; then
  echo "No supported runtimes found. Install at least one: Node.js, Go, Python 3, Maven (Java), .NET SDK."
  exit 1
fi

if [ -z "$LANG" ]; then
  echo "Runtimes OK. Run ./scripts/install.sh <lang> then ./scripts/test.sh <lang> to verify."
  exit 0
fi

echo ""
echo "Installing and testing: $LANG"
"$REPO_ROOT/scripts/install.sh" "$LANG"
"$REPO_ROOT/scripts/test.sh" "$LANG"
echo "Verify OK: $LANG"
