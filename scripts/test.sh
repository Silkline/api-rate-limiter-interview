#!/usr/bin/env bash
# Run tests for one language or all. Run from repo root.
# Usage: ./scripts/test.sh [typescript|go|python|java|csharp|rust|ruby|all]

set -e
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

LANG="${1:-all}"

run_typescript() {
  echo "Running TypeScript tests..."
  (cd typescript && npm test)
}

run_go() {
  echo "Running Go tests..."
  (cd go && go test -v -timeout=15s ./...)
}

run_python() {
  echo "Running Python tests..."
  if [ -x "python/.venv/bin/pytest" ]; then
    python/.venv/bin/pytest python -v
  else
    (cd python && python3 -m pytest -v)
  fi
}

run_java() {
  echo "Running Java tests..."
  (cd java && mvn test -q)
}

run_csharp() {
  echo "Running C# tests..."
  (cd csharp && dotnet test --no-restore 2>/dev/null || dotnet test)
}

run_rust() {
  echo "Running Rust tests..."
  (cd rust && cargo test -- --test-threads=1)
}

run_ruby() {
  echo "Running Ruby tests..."
  (cd ruby && bundle exec ruby -Ilib:test test/rate_limiter_test.rb)
}

case "$LANG" in
  typescript) run_typescript ;;
  go)         run_go ;;
  python)     run_python ;;
  java)       run_java ;;
  csharp)     run_csharp ;;
  rust)       run_rust ;;
  ruby)       run_ruby ;;
  all)
    FAILED=""
    for lang in typescript go python java csharp rust ruby; do
      echo "========== $lang =========="
      if ! "run_$lang"; then
        FAILED="$FAILED $lang"
      fi
      echo ""
    done
    if [ -n "$FAILED" ]; then
      echo "Failed:$FAILED"
      exit 1
    fi
    ;;
  *)
    echo "Usage: $0 [typescript|go|python|java|csharp|rust|ruby|all]"
    exit 1
    ;;
esac
