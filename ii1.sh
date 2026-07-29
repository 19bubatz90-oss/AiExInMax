#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$HOME/GhostMax"
APK_DEST="/storage/emulated/0/Download/GhostMax.apk"

cd "$PROJECT_DIR"

# SDK-Pfad
if [ -f local.properties ]; then
    SDK_DIR=$(grep 'sdk.dir' local.properties | cut -d'=' -f2)
else
    SDK_DIR="$HOME/android-sdk"
    [ -d "$SDK_DIR" ] || SDK_DIR="$HOME/Android/Sdk"
    echo "sdk.dir=$SDK_DIR" > local.properties
fi

# Wrapper-JAR
mkdir -p gradle/wrapper
if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
    wget -q https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar \
         -O gradle/wrapper/gradle-wrapper.jar
fi
chmod +x gradlew

# Build
./gradlew assembleDebug

# APK bereitstellen
APK_SRC="app/build/outputs/apk/debug/app-debug.apk"
cp "$APK_SRC" "$APK_DEST" 2>/dev/null || {
    mkdir -p "$(dirname "$APK_DEST")"
    cp "$APK_SRC" "$APK_DEST"
}
echo "✅ APK gespeichert unter: $APK_DEST"
command -v termux-open >/dev/null && termux-open "$APK_DEST"
