#!/usr/bin/env bash
# Run Ruby tests. Called by .vscode/launch.json "Run Ruby tests".
set -euo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export PATH="/opt/homebrew/opt/ruby/bin:/opt/homebrew/bin:$HOME/.rbenv/shims:$PATH"
exec "$REPO_ROOT/scripts/test.sh" ruby
