#!/bin/bash
set -euo pipefail
cd "$HOME/GhostMax" || { echo "❌ Projekt nicht gefunden"; exit 1; }
cat > app/src/main/java/com/ghostmax/CryptoHelper.java << 'CRYPTO_EOF'
package com.ghostmax;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import android.content.Context;
import android.content.SharedPreferences;
import java.security.GeneralSecurityException;
import java.io.IOException;

public class CryptoHelper {
    private static final String KEY_ALIAS = "ghostmax_master_key";

    public static SharedPreferences getEncryptedPrefs(Context ctx) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(ctx, KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        return EncryptedSharedPreferences.create(
                ctx,
                "ghostmax_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}
CRYPTO_EOF
echo "🔎 Selbst-Check: IOException in CryptoHelper.java deklariert?"
if ! grep -q "throws GeneralSecurityException, IOException" app/src/main/java/com/ghostmax/CryptoHelper.java; then
    echo "❌ Fix nicht angewendet - Abbruch."
    exit 1
fi
echo "✅ CryptoHelper.java korrigiert."
echo "🚀 Starte Build erneut..."
./gradlew clean assembleDebug --no-daemon --max-workers=1

APK_SRC="app/build/outputs/apk/debug/app-debug.apk"
APK_DEST="/storage/emulated/0/Download/GhostMax_fixed5.apk"
if [ -f "$APK_SRC" ]; then
    cp "$APK_SRC" "$APK_DEST"
    echo "✅ APK kopiert nach $APK_DEST"
else
    echo "❌ Build fehlgeschlagen oder APK nicht gefunden."
    exit 1
fi
