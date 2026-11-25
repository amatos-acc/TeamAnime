#!/usr/bin/env bash
# Generates HTML Javadoc into docs/api (open docs/api/index.html).
set -euo pipefail
cd "$(dirname "$0")/.."
rm -rf docs/api
javadoc -d docs/api -sourcepath src -subpackages chess -private -author -version \
        -windowtitle "TeamAnime Chess API" -doctitle "TeamAnime Chess - Phase 2 (Swing GUI)" -Xdoclint:all
echo "Javadoc -> docs/api/index.html"
