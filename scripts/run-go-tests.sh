#!/usr/bin/env bash
# Run Go tests with a PATH that works in the devcontainer and on macOS/Linux.
# Called by .vscode/launch.json "Run Go tests".
set -euo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export PATH="/usr/local/go/bin:/go/bin:/opt/homebrew/bin:$PATH"
exec "$REPO_ROOT/scripts/test.sh" go
