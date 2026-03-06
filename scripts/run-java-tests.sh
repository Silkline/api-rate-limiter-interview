#!/usr/bin/env bash
# Run Java tests with PATH that works in devcontainer and on macOS/Linux.
# Called by .vscode/launch.json "Run Java tests" so Maven is found in both environments.

set -e
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# SDKMAN: load so mvn is on PATH if installed via sdk install maven
if [ -f "${HOME}/.sdkman/bin/sdkman-init.sh" ]; then
  source "${HOME}/.sdkman/bin/sdkman-init.sh"
fi

# Common Maven locations: Homebrew (Apple + Intel), SDKMAN, apt/devcontainer
export PATH="/opt/homebrew/bin:/opt/homebrew/opt/maven/bin:/usr/local/bin:${HOME}/.sdkman/candidates/maven/current/bin:/usr/bin:$PATH"

cd "$REPO_ROOT/java"
if ! command -v mvn &>/dev/null; then
  echo "Maven (mvn) not found. Install with: brew install maven"
  echo "Or from repo root run: ./scripts/test.sh java  (uses Terminal PATH)"
  exit 1
fi
mvn test
