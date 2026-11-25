#!/usr/bin/env bash
# Builds, then starts the Swing GUI.
set -euo pipefail
cd "$(dirname "$0")/.."
./scripts/build.sh >/dev/null
java -cp out chess.gui.ChessGuiApp "$@"
