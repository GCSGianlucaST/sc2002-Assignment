#!/bin/sh
# Build src + test and run the automated rules check
set -e
cd "$(dirname "$0")"
rm -rf out
javac --release 17 -d out $(find src test -name "*.java")
java -cp out restaurantrush.test.RulesCheck
