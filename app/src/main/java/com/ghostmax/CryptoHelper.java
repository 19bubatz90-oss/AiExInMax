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
