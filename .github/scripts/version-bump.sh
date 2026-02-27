#!/bin/bash

# Directory of this script
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"

#
# Takes a version number, and the mode to bump it, and increments/resets
# the proper components so that the result is placed in the variable
# `NEW_VERSION`.
#
# $1 = mode (major, minor, patch)
# $2 = version (x.y.z)
#
function bump {
    local mode="$1"
    local old="$2"
    local parts=(${old//./ })
    case "$1" in
    major)
        local bv=$((parts[0] + 1))
        NEW_VERSION="${bv}.0.0"
        ;;
    minor)
        local bv=$((parts[1] + 1))
        NEW_VERSION="${parts[0]}.${bv}.0"
        ;;
    patch|auto)
        local bv=$((parts[2] + 1))
        NEW_VERSION="${parts[0]}.${parts[1]}.${bv}"
        ;;
    esac
}

OLD_VERSION=$($DIR/get-version.sh)

BUMP_MODE="${1:-auto}"
NEW_VERSION="-"

echo "$BUMP_MODE version bump detected"
bump $BUMP_MODE $OLD_VERSION
echo "version will be bumped from $OLD_VERSION to $NEW_VERSION"
sed -i "s/\(version *= *['\"]*\)${OLD_VERSION}\(['\"]*\)/\1${NEW_VERSION}\2/" gradle.properties
