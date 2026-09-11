#!/usr/bin/env bash
# Run Rust tests. Called by .vscode/launch.json "Run Rust tests".
set -euo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export PATH="$HOME/.cargo/bin:/usr/local/cargo/bin:/opt/homebrew/bin:$PATH"
exec "$REPO_ROOT/scripts/test.sh" rust
