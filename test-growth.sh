#!/usr/bin/env sh
# Pure game-rule checks; no network or Minecraft dependencies required.
set -eu
cd "$(dirname "$0")"
task_test_dir="build/growth-checks"
mkdir -p "$task_test_dir"
javac -encoding UTF-8 -d "$task_test_dir" \
    src/main/java/cn/frogdevour/GrowthRules.java \
    src/main/java/cn/frogdevour/FeedingBudget.java \
    src/test/java/cn/frogdevour/GrowthRulesTest.java
java -cp "$task_test_dir" cn.frogdevour.GrowthRulesTest
