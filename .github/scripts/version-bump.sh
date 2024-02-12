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
    patch)
        local bv=$((parts[2] + 1))
        NEW_VERSION="${parts[0]}.${parts[1]}.${bv}"
        ;;
    auto)
        if [ "${AUTO_HIGHER}" == "true" ] && [[ "${parts[2]}" == *"${AUTO_SPLITTER}"* ]]; then
            local higher=(${parts[2]//${AUTO_SPLITTER}/ })
            local bv=$((higher[1] + 1))
            NEW_VERSION="${parts[0]}.${parts[1]}.$((parts[2] + 0))${AUTO_SPLITTER}${bv}"
        elif [ "${AUTO_HIGHER}" == "false" ] && [[ "${parts[2]}" == *"${AUTO_SPLITTER}"* ]]; then
            local higher=(${parts[2]//${AUTO_SPLITTER}/ })
            local bv=$((higher[1] + 0))
            NEW_VERSION="${parts[0]}.${parts[1]}.$((parts[2] + 0))${AUTO_SPLITTER}${bv}"
        elif [ "${AUTO_HIGHER}" == "true" ]; then
            NEW_VERSION="${parts[0]}.${parts[1]}.$((parts[2] + 0))${AUTO_SPLITTER}0"
        else
            NEW_VERSION="${parts[0]}.${parts[1]}.$((parts[2] + 0))${AUTO_SPLITTER}"
        fi
        ;;
    esac
}

OLD_VERSION=$($DIR/get-version.sh)

BUMP_MODE="none"
NEW_VERSION="-"
if git log -1 | grep -q "#major"; then
    BUMP_MODE="major"
elif git log -1 | grep -q "#minor"; then
    BUMP_MODE="minor"
elif git log -1 | grep -q "#patch"; then
    BUMP_MODE="patch"
elif [[ "${AUTO}" == "true" ]]; then
    BUMP_MODE="${AUTO_MODE}"
fi

if [ -f gradle.properties ] && grep -E -q "version=${CURRENT_VERSION}" gradle.properties; then
    BUILD_FILE=./gradle.properties
elif [ -f ./build.gradle ]; then
    BUILD_FILE=./build.gradle
elif [ -f ./build.gradle.kts ]; then
    BUILD_FILE=./build.gradle.kts
fi

if [[ "${BUMP_MODE}" == "none" ]]; then
    echo "No matching commit tags found.
    Version will remain at" $OLD_VERSION
else
    echo $BUMP_MODE "version bump detected"
    bump $BUMP_MODE $OLD_VERSION
    echo "version will be bumped from" $OLD_VERSION "to" $NEW_VERSION
    sed -i "s/\(version *= *['\"]*\)${OLD_VERSION}\(['\"]*\)/\1${NEW_VERSION}\2/" ${BUILD_FILE}
    git add $BUILD_FILE
    git commit -m "Bump version from $OLD_VERSION to $NEW_VERSION"
    if [[ "${BUMP_MODE}" == "auto" ]] && [[ "${AUTO_RELEASE}" == "false" ]]; then
        echo "Doing no new tag for this bump because its disabled for auto mode"
        git push $REPO
    else
        git tag $NEW_VERSION
        git push $REPO --follow-tags
        git push $REPO --tags
        echo "Created a new tag for this bump"
    fi
fi
