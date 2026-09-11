#!/usr/bin/env bash
# Run tests for one language or all. Run from anywhere; the script cd's to the repo root.
#
# Usage: ./scripts/test.sh <language|all> [smoke]
#
#   language  typescript | go | python | java | csharp | rust | ruby | kotlin | all
#   smoke     run only the always-passing "harness smoke" test. Proves the toolchain,
#             build and test runner work without requiring an implementation.
#
# Exit code is non-zero if any test fails. With the unimplemented stub, every test
# except the smoke test fails - that is expected.

set -euo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

TARGET_LANG="${1:-}"
MODE="${2:-full}"

usage() {
  echo "Usage: $0 <typescript|go|python|java|csharp|rust|ruby|kotlin|all> [smoke]"
  exit 1
}

[ -n "$TARGET_LANG" ] || usage
case "$MODE" in full|smoke) ;; *) usage ;; esac

smoke() { [ "$MODE" = "smoke" ]; }

run_typescript() {
  echo "Running TypeScript tests..."
  if smoke; then
    (cd typescript && npx vitest run -t "harness smoke")
  else
    (cd typescript && npm test)
  fi
}

run_go() {
  echo "Running Go tests..."
  if smoke; then
    (cd go && go test -v -run 'TestHarnessSmoke' ./...)
  else
    (cd go && go test -v -timeout=300s ./...)
  fi
}

run_python() {
  echo "Running Python tests..."
  local pytest_cmd
  if [ -x "python/.venv/bin/pytest" ]; then
    pytest_cmd="python/.venv/bin/pytest"
  elif python3 -c "import pytest" 2>/dev/null; then
    pytest_cmd="python3 -m pytest"
  else
    echo "pytest not found. Run ./scripts/install.sh python first (creates python/.venv)."
    return 1
  fi
  if smoke; then
    $pytest_cmd python -v -k harness_smoke
  else
    $pytest_cmd python -v
  fi
}

run_java() {
  echo "Running Java tests..."
  if smoke; then
    (cd java && ./mvnw test -Dtest='RateLimiterTest#harnessSmoke')
  else
    (cd java && ./mvnw test)
  fi
}

run_csharp() {
  echo "Running C# tests..."
  if smoke; then
    (cd csharp && dotnet test --filter 'FullyQualifiedName~HarnessSmoke')
  else
    (cd csharp && dotnet test)
  fi
}

run_rust() {
  echo "Running Rust tests..."
  # Tests share one global tier map, so they must run on a single thread.
  if smoke; then
    (cd rust && cargo test harness_smoke -- --test-threads=1)
  else
    (cd rust && cargo test -- --test-threads=1)
  fi
}

run_ruby() {
  echo "Running Ruby tests..."
  # minitest ships with Ruby, so no Bundler is needed.
  if smoke; then
    (cd ruby && ruby -Ilib test/rate_limiter_test.rb -n /harness_smoke/)
  else
    (cd ruby && ruby -Ilib test/rate_limiter_test.rb)
  fi
}

run_kotlin() {
  echo "Running Kotlin tests..."
  if smoke; then
    (cd kotlin && ./gradlew test --no-daemon --tests 'com.silkline.ratelimit.RateLimiterTest.harnessSmoke')
  else
    (cd kotlin && ./gradlew test --no-daemon)
  fi
}

case "$TARGET_LANG" in
  typescript|go|python|java|csharp|rust|ruby|kotlin) "run_$TARGET_LANG" ;;
  all)
    FAILED=""
    for lang in typescript go python java csharp rust ruby kotlin; do
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
  *) usage ;;
esac
