#!/usr/bin/env bash
# Run Ruby tests. Called by .vscode/launch.json "Run Ruby tests".

set -e
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

cd "$REPO_ROOT/ruby"
bundle exec ruby -Ilib:test test/rate_limiter_test.rb
