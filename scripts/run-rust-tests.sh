#!/usr/bin/env bash
# Run Rust tests. Called by .vscode/launch.json "Run Rust tests".

set -e
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

export PATH="$HOME/.cargo/bin:/usr/local/cargo/bin:$PATH"

cd "$REPO_ROOT/rust"
# HARD_MODE=1 also runs the #[ignore]-marked hard-mode concurrency tests.
if [ -n "${HARD_MODE:-}" ]; then
  cargo test -- --test-threads=1 --include-ignored
else
  cargo test -- --test-threads=1
fi
