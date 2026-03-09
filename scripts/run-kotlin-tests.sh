#!/usr/bin/env bash
# Run Kotlin tests. Called by .vscode/launch.json "Run Kotlin tests".

set -e
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# Ensure Java is on PATH (e.g. SDKMAN, Homebrew, devcontainer)
if [ -f "${HOME}/.sdkman/bin/sdkman-init.sh" ]; then
  source "${HOME}/.sdkman/bin/sdkman-init.sh"
fi
export PATH="/opt/homebrew/bin:/usr/local/bin:${HOME}/.sdkman/candidates/java/current/bin:$PATH"

cd "$REPO_ROOT/kotlin"
if ! command -v java &>/dev/null; then
  echo "Java (JDK) not found. Install from https://adoptium.net/ or use SDKMAN."
  exit 1
fi
./gradlew test --no-daemon
