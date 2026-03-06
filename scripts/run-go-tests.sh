#!/usr/bin/env bash
# Run Go tests with PATH that works in devcontainer and on macOS/Linux.
# Called by .vscode/launch.json "Run Go tests" so Go is found in both environments.

set -e
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# Ensure Go is on PATH: devcontainer uses /usr/local/go/bin or /go/bin;
# macOS Homebrew uses /opt/homebrew/bin; official installer uses /usr/local/go/bin
export PATH="/usr/local/go/bin:/go/bin:/opt/homebrew/bin:$PATH"

cd "$REPO_ROOT/go"
go test -v -timeout=15s ./...
