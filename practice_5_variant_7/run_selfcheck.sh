#!/usr/bin/env bash
set -eu
cd -- "$(dirname -- "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/ru/edu/pr02/*.java
java -cp out ru.edu.pr02.SelfCheck
