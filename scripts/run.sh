#!/usr/bin/env bash
# Builds, then starts the interactive game. Optional args: whiteName blackName
set -euo pipefail
cd "$(dirname "$0")/.."
./scripts/build.sh >/dev/null
java -cp out chess.console.ChessApp "$@"
