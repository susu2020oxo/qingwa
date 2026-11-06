#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
sh ./gradlew clean build
printf '\n安装 build/libs/frog-devour-1.0.0.jar；sources.jar 是源码包。\n'
