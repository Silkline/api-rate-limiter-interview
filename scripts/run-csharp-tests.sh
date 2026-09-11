#!/usr/bin/env bash
# Run C# tests with a PATH that works in the devcontainer and on macOS/Linux.
# Called by .vscode/launch.json "Run C# tests".
set -euo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export PATH="/usr/local/share/dotnet:/usr/share/dotnet:/opt/homebrew/bin:$HOME/.dotnet:$PATH"
exec "$REPO_ROOT/scripts/test.sh" csharp
