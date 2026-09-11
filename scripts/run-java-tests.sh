#!/usr/bin/env bash
# Run Java tests with a PATH that works in the devcontainer and on macOS/Linux.
# Called by .vscode/launch.json "Run Java tests". Uses the Maven wrapper, so only a JDK is required.
set -euo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [ -f "${HOME}/.sdkman/bin/sdkman-init.sh" ]; then
  # shellcheck disable=SC1091
  source "${HOME}/.sdkman/bin/sdkman-init.sh"
fi
export PATH="/opt/homebrew/bin:/usr/local/bin:${HOME}/.sdkman/candidates/java/current/bin:$PATH"
exec "$REPO_ROOT/scripts/test.sh" java
