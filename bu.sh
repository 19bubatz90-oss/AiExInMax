#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$HOME/GhostMax"
APK_DEST="/storage/emulated/0/Download/GhostMax.apk"

cd "$PROJECT_DIR"

# SDK-Pfad ermitteln
if [ -f local.properties ]; then
    SDK_DIR=$(grep 'sdk.dir' local.properties | cut -d'=' -f2)
elif [ -d "$HOME/android-sdk" ]; then
    SDK_DIR="$HOME/android-sdk"
elif [ -n "${ANDROID_HOME:-}" ]; then
    SDK_DIR="$ANDROID_HOME"
elif [ -n "${ANDROID_SDK_ROOT:-}" ]; then
    SDK_DIR="$ANDROID_SDK_ROOT"
else
    SDK_DIR="$HOME/Android/Sdk"
fi
echo "sdk.dir=$SDK_DIR" > local.properties

# Systemweites Gradle nutzen
gradle assembleDebug

# APK kopieren
APK_SRC="app/build/outputs/apk/debug/app-debug.apk"
cp "$APK_SRC" "$APK_DEST" 2>/dev/null || {
    mkdir -p "$(dirname "$APK_DEST")"
    cp "$APK_SRC" "$APK_DEST"
}
echo "✅ APK gespeichert unter: $APK_DEST"

# Installation anstoßen
command -v termux-open >/dev/null && termux-open "$APK_DEST"
