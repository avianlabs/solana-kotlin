#!/bin/bash

set -e # Exit on error

# Function to check if xcodebuild is available
check_xcode() {
    if ! command -v xcodebuild >/dev/null 2>&1; then
        echo "Error: xcodebuild not found. Please install Xcode Command Line Tools."
        exit 1
    fi
}

# Function to get the active Xcode developer directory
get_developer_dir() {
    local developer_dir
    developer_dir=$(xcode-select -p)
    echo "$developer_dir"
}

# Function to get SDK information
get_sdk_info() {
    local sdk_type="$1" # iphoneos or iphonesimulator
    xcodebuild -version -sdk "$sdk_type" Path
}

# Function to get default architecture for SDK
get_sdk_archs() {
    local sdk_type="$1"
    if [[ "$sdk_type" == "iphonesimulator" ]]; then
        echo "arm64 x86_64"
    else
        echo "arm64"
    fi
}

# Main script
echo "Detecting Xcode environment..."

# Check for Xcode installation
check_xcode

# Get developer directory
DEVELOPER_DIR=$(get_developer_dir)
echo "Developer Directory: $DEVELOPER_DIR"

# Determine SDK (preferring physical device SDK, falling back to simulator)
if xcodebuild -version -sdk iphoneos >/dev/null 2>&1; then
    SDK_NAME="iphoneos"
    PLATFORM_NAME="iphoneos"
    EFFECTIVE_PLATFORM_NAME="-iphoneos"
else
    SDK_NAME="iphonesimulator"
    PLATFORM_NAME="iphonesimulator"
    EFFECTIVE_PLATFORM_NAME="-iphonesimulator"
fi

# Read SDK information
IFS=$'\n' read -r -d '' SDKROOT SDK_PLATFORM_VERSION SDK_VERSION < <(get_sdk_info "$SDK_NAME" && printf '\0')

# Get architecture
ARCHS=$(get_sdk_archs "$SDK_NAME")
VALID_ARCHS=${ARCHS:-"arm64"} # Fallback to arm64 if detection fails

# Set up build directories
BASE_DIR="$(pwd)/build/xcode-env"
CONFIGURATION="Debug"  # Can be parameterized if needed
KOTLIN_FRAMEWORK_BUILD_TYPE="debug"  # Matches CONFIGURATION in lowercase
TARGET_BUILD_DIR="$BASE_DIR/Products"
BUILT_PRODUCTS_DIR="$TARGET_BUILD_DIR"  # Required by Kotlin plugin
FRAMEWORKS_FOLDER_PATH="$BASE_DIR/Frameworks"

# Create required directories
mkdir -p "$TARGET_BUILD_DIR"
mkdir -p "$FRAMEWORKS_FOLDER_PATH"

# Export all required variables
export SDK_NAME
export SDKROOT
export CONFIGURATION
export KOTLIN_FRAMEWORK_BUILD_TYPE
export TARGET_BUILD_DIR
export BUILT_PRODUCTS_DIR
export ARCHS="$VALID_ARCHS"
export FRAMEWORKS_FOLDER_PATH
export PLATFORM_NAME
export EFFECTIVE_PLATFORM_NAME

# Print environment setup
echo "Environment variables set:"
echo "SDK_NAME=$SDK_NAME"
echo "SDKROOT=$SDKROOT"
echo "CONFIGURATION=$CONFIGURATION"
echo "KOTLIN_FRAMEWORK_BUILD_TYPE=$KOTLIN_FRAMEWORK_BUILD_TYPE"
echo "TARGET_BUILD_DIR=$TARGET_BUILD_DIR"
echo "BUILT_PRODUCTS_DIR=$BUILT_PRODUCTS_DIR"
echo "ARCHS=$ARCHS"
echo "FRAMEWORKS_FOLDER_PATH=$FRAMEWORKS_FOLDER_PATH"
echo "PLATFORM_NAME=$PLATFORM_NAME"
echo "EFFECTIVE_PLATFORM_NAME=$EFFECTIVE_PLATFORM_NAME"

# Run the gradle task
echo "Running gradle task..."
./gradlew :solana-kotlin:embedSwiftExportForXcode --stacktrace
