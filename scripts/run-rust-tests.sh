#!/usr/bin/env bash
# Run Rust tests. Called by .vscode/launch.json "Run Rust tests".

set -e
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

export PATH="$HOME/.cargo/bin:/usr/local/cargo/bin:$PATH"

cd "$REPO_ROOT/rust"
cargo test -- --test-threads=1
