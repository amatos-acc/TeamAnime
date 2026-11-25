#!/usr/bin/env bash
# Compiles the application and the JUnit 5 tests, then runs every test.
# Requires lib/junit-platform-console-standalone-1.11.4.jar (included in the repo).
set -euo pipefail
cd "$(dirname "$0")/.."
JUNIT=lib/junit-platform-console-standalone-1.11.4.jar

./scripts/build.sh >/dev/null
rm -rf out-test
mkdir -p out-test
javac --release 17 -d out-test -cp "out:$JUNIT" $(find test -name '*.java')

# Keep the game's log messages off the console; LogConfigTest still writes its own log file.
java -Djava.util.logging.config.file=test/logging.properties \
     -jar "$JUNIT" execute \
     --class-path "out:out-test" \
     --scan-class-path out-test \
     --details=tree \
     --disable-banner "$@"
