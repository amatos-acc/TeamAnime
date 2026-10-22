#!/usr/bin/env bash
# Builds, then runs the labeled test scenarios (DemoDriver).
set -euo pipefail
cd "$(dirname "$0")/.."
./scripts/build.sh >/dev/null
java -cp out chess.console.DemoDriver
