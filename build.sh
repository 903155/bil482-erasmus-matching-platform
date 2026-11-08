#!/usr/bin/env bash
# Build, test and package UC2 "Find a Match" – needs only a JDK 17+ (no Maven, no internet).
#   ./build.sh        compile + run tests + create out/matching-uc2.jar
#   ./build.sh run    same, then start the web app on http://localhost:8080
set -euo pipefail
cd "$(dirname "$0")"
TESTS="com.sangreyoro.matching.StrategyTest com.sangreyoro.matching.MatchingServiceTest com.sangreyoro.matching.PatternTest com.sangreyoro.matching.ApiTest"

rm -rf out && mkdir -p out/classes/data out/test-classes
echo "== Compiling sources"
javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d out/classes $(find src/main/java -name '*.java')
cp -r src/main/resources/. out/classes/
cp data/students.csv out/classes/data/students.csv
echo "== Compiling tests"
javac --release 17 -encoding UTF-8 -cp out/classes -d out/test-classes $(find src/test/java -name '*.java')
echo "== Running tests"
java -Dstdout.encoding=UTF-8 -cp out/classes:out/test-classes com.sangreyoro.matching.testkit.TestRunner $TESTS
echo "== Packaging"
jar --create --file out/matching-uc2.jar --main-class com.sangreyoro.matching.MatchingApplication -C out/classes .
echo "Built out/matching-uc2.jar"
if [[ "${1:-}" == "run" ]]; then java -jar out/matching-uc2.jar "${PORT:-8080}"; fi
