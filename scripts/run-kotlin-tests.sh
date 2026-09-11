#!/usr/bin/env bash
# Run Kotlin tests. Called by .vscode/launch.json "Run Kotlin tests". Uses the Gradle wrapper.
set -euo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [ -f "${HOME}/.sdkman/bin/sdkman-init.sh" ]; then
  # shellcheck disable=SC1091
  source "${HOME}/.sdkman/bin/sdkman-init.sh"
fi
export PATH="/opt/homebrew/bin:/usr/local/bin:${HOME}/.sdkman/candidates/java/current/bin:$PATH"
exec "$REPO_ROOT/scripts/test.sh" kotlin
