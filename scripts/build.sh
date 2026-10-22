#!/usr/bin/env bash
# Compiles every .java file under src/ into out/ (targets Java 17+).
set -euo pipefail
cd "$(dirname "$0")/.."
rm -rf out
mkdir -p out
javac --release 17 -Xlint:all -d out $(find src -name '*.java')
echo "Build OK -> out/"
