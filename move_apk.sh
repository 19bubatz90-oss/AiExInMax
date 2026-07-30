#!/bin/bash
set -e
APK_SRC="app/build/outputs/apk/debug/app-debug.apk"
APK_DEST="/storage/emulated/0/Download/GhostMax_v${NEW_VERSION}.apk"
if [ -f "$APK_SRC" ]; then
    cp "$APK_SRC" "$APK_DEST"
    echo "✅ APK gespeichert: $APK_DEST"
    rm -rf "app/build/outputs/apk/debug/"
    echo "🧹 Build-Ordner gelöscht."
else
    echo "❌ APK nicht gefunden: $APK_SRC"
    exit 1
fi
