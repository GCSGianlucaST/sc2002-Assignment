#!/bin/sh
# Build and play:            ./run.sh
set -e
cd "$(dirname "$0")"
rm -rf out
javac --release 17 -d out $(find src -name "*.java")
java -cp out restaurantrush.app.RestaurantRushApp "$@"
