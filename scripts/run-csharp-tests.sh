#!/usr/bin/env bash
# Run C# tests with PATH that works in devcontainer and on macOS/Linux.
# Called by .vscode/launch.json "Run C# tests" so dotnet is found in both environments.

set -e
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# Ensure dotnet is on PATH: macOS often /usr/local/share/dotnet; Homebrew /opt/homebrew/bin;
# Linux/devcontainer typically /usr/share/dotnet or already on PATH
export PATH="/usr/local/share/dotnet:/usr/share/dotnet:/opt/homebrew/bin:$PATH"

if ! command -v dotnet &>/dev/null; then
  echo ".NET SDK not found. Install .NET 8 from https://dotnet.microsoft.com/download/dotnet/8.0"
  echo "Or: brew install dotnet@8   (then add to PATH if needed)"
  exit 1
fi

cd "$REPO_ROOT/csharp"
dotnet test --logger "console;verbosity=normal"
