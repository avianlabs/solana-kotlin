#!/bin/sh

./gradlew properties --no-daemon --console=plain -q | grep "^version:" | awk '{printf $2}'
