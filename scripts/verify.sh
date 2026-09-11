#!/usr/bin/env bash
# Pre-flight check. Run from anywhere; the script cd's to the repo root.
#
# Usage: ./scripts/verify.sh [language]
#
#   no argument  print which runtimes are installed (with versions)
#   language     install dependencies, compile, and run the "harness smoke" test.
#                Prints "Verify OK" when the environment works. Does NOT require the
#                rate limiter to be implemented.
#
# Interviewers: run this for the candidate's language before the session.
# Candidates: run this first if anything looks broken.

set -euo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

TARGET_LANG="${1:-}"

show() {
  # show <label> <command> <version args...>
  local label="$1" cmd="$2"; shift 2
  if command -v "$cmd" &>/dev/null; then
    local v
    v="$("$cmd" "$@" 2>&1 | head -n 1)"
    printf "  %-12s %s\n" "$label" "$v"
  else
    printf "  %-12s not found\n" "$label"
  fi
}

echo "Installed runtimes:"
show "node"    node    --version
show "go"      go      version
show "python3" python3 --version
show "java"    java    -version
show "dotnet"  dotnet  --version
show "cargo"   cargo   --version
show "ruby"    ruby    --version
echo "  (Java and Kotlin use bundled wrappers: mvn/gradle need not be installed.)"

if [ -z "$TARGET_LANG" ]; then
  echo ""
  echo "Next: ./scripts/verify.sh <language> to install, compile and smoke-test one language."
  exit 0
fi

compile() {
  case "$1" in
    typescript) (cd typescript && npx tsc --noEmit) ;;
    go)         (cd go && go vet ./...) ;;
    python)     python/.venv/bin/python -m py_compile python/rate_limiter.py python/test_rate_limiter.py ;;
    java)       (cd java && ./mvnw -q test-compile) ;;
    csharp)     (cd csharp && dotnet build --nologo -v quiet) ;;
    rust)       (cd rust && cargo check --tests) ;;
    ruby)       (cd ruby && ruby -c lib/rate_limiter.rb >/dev/null && ruby -c test/rate_limiter_test.rb >/dev/null) ;;
    kotlin)     (cd kotlin && ./gradlew --no-daemon -q compileTestKotlin) ;;
    *) echo "Unknown language: $1"; exit 1 ;;
  esac
}

echo ""
echo "== [1/3] Installing dependencies: $TARGET_LANG"
"$REPO_ROOT/scripts/install.sh" "$TARGET_LANG"
echo ""
echo "== [2/3] Compiling / type-checking: $TARGET_LANG"
compile "$TARGET_LANG"
echo "   compile OK"
echo ""
echo "== [3/3] Running the harness smoke test: $TARGET_LANG"
"$REPO_ROOT/scripts/test.sh" "$TARGET_LANG" smoke
echo ""
echo "Verify OK: $TARGET_LANG environment works."
echo "Run the full suite with: ./scripts/test.sh $TARGET_LANG"
echo "(Until the rate limiter is implemented, every test except the smoke test fails. That is expected.)"
