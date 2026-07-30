#!/bin/bash
set -euo pipefail
PROJECT_DIR="$HOME/GhostMax"
cd "$PROJECT_DIR" || { echo "❌ Projekt nicht gefunden"; exit 1; }

echo "============================================================"
echo "GhostMax - Finales Update (Termux-Build + GitHub-Workflow)"
echo "============================================================"

echo "🔄 [1/6] Lege Ordnerstruktur an..."
mkdir -p app/src/main/res/layout
mkdir -p app/src/main/res/menu
mkdir -p app/src/main/res/values
mkdir -p .github/workflows

echo "🔄 [2/6] Schreibe Android-Ressourcen (Layouts, Menu, Farben, Theme)..."
cat > app/src/main/res/values/colors.xml << 'COLORS_EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="colorPrimary">#1E88E5</color>
    <color name="colorPrimaryDark">#1565C0</color>
    <color name="colorAccent">#00BFA5</color>
    <color name="bg_chat">#F2F4F7</color>
    <color name="bubble_user">#1E88E5</color>
    <color name="bubble_bot">#FFFFFF</color>
    <color name="bubble_bot_text">#1B1B1B</color>
    <color name="bubble_system_bg">#E3E7ED</color>
    <color name="bubble_system_text">#5A6472</color>
    <color name="input_bar_bg">#FFFFFF</color>
</resources>
COLORS_EOF

cat > app/src/main/res/values/styles.xml << 'STYLES_EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="AppTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
        <item name="android:windowBackground">@color/bg_chat</item>
    </style>
</resources>
STYLES_EOF

cat > app/src/main/res/menu/main_menu.xml << 'MENU_EOF'
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <item android:id="@+id/action_settings" android:title="⚙️ Einstellungen" app:showAsAction="never"/>
    <item android:id="@+id/action_add_provider" android:title="➕ Provider hinzufügen" app:showAsAction="never"/>
    <item android:id="@+id/action_catalog" android:title="📚 KI-Katalog" app:showAsAction="never"/>
    <item android:id="@+id/action_add_personality" android:title="🧠 Neue Persönlichkeit" app:showAsAction="never"/>
    <item android:id="@+id/action_image" android:title="🖼️ Bildgenerierung" app:showAsAction="never"/>
    <item android:id="@+id/action_server" android:title="🖥️ Lokalen Server umschalten" app:showAsAction="never"/>
    <item android:id="@+id/action_secret" android:title="🕵️ Geheim-Modus" app:showAsAction="never"/>
    <item android:id="@+id/action_clear" android:title="🗑️ Chat leeren" app:showAsAction="never"/>
</menu>
MENU_EOF

cat > app/src/main/res/layout/item_message_user.xml << 'ITEMUSER_EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="end"
    android:paddingStart="48dp"
    android:paddingEnd="12dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <com.google.android.material.card.MaterialCardView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:cardCornerRadius="16dp"
        app:cardElevation="1dp"
        app:cardBackgroundColor="@color/bubble_user">

        <LinearLayout
            android:orientation="vertical"
            android:paddingStart="14dp"
            android:paddingEnd="14dp"
            android:paddingTop="9dp"
            android:paddingBottom="9dp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content">

            <TextView
                android:id="@+id/tvText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="#FFFFFF"
                android:textSize="15sp"
                android:textIsSelectable="true"/>

            <TextView
                android:id="@+id/tvMeta"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="#CFE3FA"
                android:textSize="10sp"
                android:layout_marginTop="2dp"
                android:visibility="gone"/>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>
</LinearLayout>
ITEMUSER_EOF

cat > app/src/main/res/layout/item_message_bot.xml << 'ITEMBOT_EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="start"
    android:paddingStart="12dp"
    android:paddingEnd="48dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <com.google.android.material.card.MaterialCardView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:cardCornerRadius="16dp"
        app:cardElevation="1dp"
        app:strokeWidth="1dp"
        app:strokeColor="#E0E4EA"
        app:cardBackgroundColor="@color/bubble_bot">

        <LinearLayout
            android:orientation="vertical"
            android:paddingStart="14dp"
            android:paddingEnd="14dp"
            android:paddingTop="9dp"
            android:paddingBottom="9dp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content">

            <TextView
                android:id="@+id/tvText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="@color/bubble_bot_text"
                android:textSize="15sp"
                android:textIsSelectable="true"/>

            <TextView
                android:id="@+id/tvMeta"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="#8A93A0"
                android:textSize="10sp"
                android:layout_marginTop="2dp"
                android:visibility="gone"/>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>
</LinearLayout>
ITEMBOT_EOF

cat > app/src/main/res/layout/item_message_system.xml << 'ITEMSYS_EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">

    <com.google.android.material.card.MaterialCardView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:cardCornerRadius="10dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="@color/bubble_system_bg">

        <TextView
            android:id="@+id/tvText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:paddingStart="10dp"
            android:paddingEnd="10dp"
            android:paddingTop="5dp"
            android:paddingBottom="5dp"
            android:textColor="@color/bubble_system_text"
            android:textSize="11sp"/>
    </com.google.android.material.card.MaterialCardView>
</LinearLayout>
ITEMSYS_EOF

cat > app/src/main/res/layout/activity_main.xml << 'ACTMAIN_EOF'
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/appBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:theme="@style/ThemeOverlay.MaterialComponents.Dark.ActionBar">

        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            app:title="GhostMax"
            app:titleTextColor="#FFFFFF"
            android:background="@color/colorPrimary"/>

        <HorizontalScrollView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@color/colorPrimaryDark"
            android:scrollbars="none">

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:paddingStart="8dp"
                android:paddingEnd="8dp"
                android:paddingTop="6dp"
                android:paddingBottom="6dp">

                <Spinner
                    android:id="@+id/filterSpinner"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginEnd="8dp"
                    android:minWidth="110dp"/>

                <Spinner
                    android:id="@+id/modelSpinner"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginEnd="8dp"
                    android:minWidth="170dp"/>

                <Spinner
                    android:id="@+id/personalitySpinner"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:minWidth="140dp"/>
            </LinearLayout>
        </HorizontalScrollView>
    </com.google.android.material.appbar.AppBarLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/chatRecycler"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:background="@color/bg_chat"
            android:clipToPadding="false"
            android:paddingTop="8dp"
            android:paddingBottom="8dp"/>

        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:cardCornerRadius="0dp"
            app:cardElevation="6dp"
            app:cardBackgroundColor="@color/input_bar_bg">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:paddingStart="10dp"
                android:paddingEnd="6dp"
                android:paddingTop="8dp"
                android:paddingBottom="8dp">

                <EditText
                    android:id="@+id/inputText"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:hint="Nachricht ..."
                    android:maxLines="4"
                    android:background="@null"
                    android:paddingStart="8dp"
                    android:paddingEnd="8dp"
                    android:textSize="15sp"
                    android:inputType="textMultiLine|textCapSentences"/>

                <ImageButton
                    android:id="@+id/copyButton"
                    android:layout_width="42dp"
                    android:layout_height="42dp"
                    android:src="@android:drawable/ic_menu_save"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:contentDescription="Kopieren"/>

                <ImageButton
                    android:id="@+id/stopButton"
                    android:layout_width="42dp"
                    android:layout_height="42dp"
                    android:enabled="false"
                    android:alpha="0.4"
                    android:src="@android:drawable/ic_media_pause"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:contentDescription="Stopp"/>

                <ImageButton
                    android:id="@+id/sendButton"
                    android:layout_width="42dp"
                    android:layout_height="42dp"
                    android:src="@android:drawable/ic_menu_send"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    app:tint="@color/colorPrimary"
                    android:contentDescription="Senden"/>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>
    </LinearLayout>
</androidx.coordinatorlayout.widget.CoordinatorLayout>
ACTMAIN_EOF

echo "🔄 [3/6] Schreibe Java-Klassen (CryptoHelper, Prefs, ApiClient, MainActivity, ModelCatalog, ChatAdapter)..."
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

cat > app/src/main/java/com/ghostmax/Prefs.java << 'PREF_EOF'
package com.ghostmax;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Prefs {
    private SharedPreferences sp;
    private boolean secureMode = false;

    public Prefs(Context ctx) {
        try {
            sp = CryptoHelper.getEncryptedPrefs(ctx);
        } catch (Exception e) {
            sp = ctx.getSharedPreferences("ghostmax_fallback", Context.MODE_PRIVATE);
        }
    }

    public void setSecureMode(boolean enabled) { this.secureMode = enabled; }
    public boolean isSecureMode() { return secureMode; }

    public String getApiKey(String provider) { return sp.getString("key_" + provider, ""); }
    public void setApiKey(String provider, String key) { sp.edit().putString("key_" + provider, key).apply(); }
    public String getProviderUrl(String provider) { return sp.getString("url_" + provider, ""); }
    public void setProviderUrl(String provider, String url) { sp.edit().putString("url_" + provider, url).apply(); }
    public String getProviderModel(String provider) { return sp.getString("model_" + provider, ""); }
    public void setProviderModel(String provider, String model) { sp.edit().putString("model_" + provider, model).apply(); }
    public String getHistory() { return sp.getString("chat_history", "[]"); }
    public void saveHistory(String json) { if (!secureMode) sp.edit().putString("chat_history", json).apply(); }
    public double getTemperature() { return sp.getFloat("temperature", 0.7f); }
    public void setTemperature(double v) { sp.edit().putFloat("temperature", (float) v).apply(); }
    public int getMaxTokens() { return sp.getInt("max_tokens", 1024); }
    public void setMaxTokens(int v) { sp.edit().putInt("max_tokens", v).apply(); }
    public boolean getFallbackEnabled() { return sp.getBoolean("fallback_enabled", true); }
    public void setFallbackEnabled(boolean v) { sp.edit().putBoolean("fallback_enabled", v).apply(); }
    public int getHistoryWindow() { return sp.getInt("history_window", 8); }
    public void setHistoryWindow(int v) { sp.edit().putInt("history_window", v).apply(); }
    public String getSystemPrompt() { return sp.getString("system_prompt", "Du bist ein hilfreicher KI-Assistent."); }
    public void setSystemPrompt(String v) { sp.edit().putString("system_prompt", v).apply(); }
    public boolean getAutoScroll() { return sp.getBoolean("auto_scroll", true); }
    public void setAutoScroll(boolean v) { sp.edit().putBoolean("auto_scroll", v).apply(); }
    public boolean getShowProvider() { return sp.getBoolean("show_provider", true); }
    public void setShowProvider(boolean v) { sp.edit().putBoolean("show_provider", v).apply(); }
    public boolean getDarkMode() { return sp.getBoolean("dark_mode", false); }
    public void setDarkMode(boolean v) { sp.edit().putBoolean("dark_mode", v).apply(); }
    public int getFontSize() { return sp.getInt("font_size", 18); }
    public void setFontSize(int v) { sp.edit().putInt("font_size", v).apply(); }
    public boolean getQuickReplies() { return sp.getBoolean("quick_replies", true); }
    public void setQuickReplies(boolean v) { sp.edit().putBoolean("quick_replies", v).apply(); }
    public boolean getCopyButton() { return sp.getBoolean("copy_button", true); }
    public void setCopyButton(boolean v) { sp.edit().putBoolean("copy_button", v).apply(); }
    public boolean getNotifications() { return sp.getBoolean("notifications", true); }
    public void setNotifications(boolean v) { sp.edit().putBoolean("notifications", v).apply(); }
    public boolean getAnimations() { return sp.getBoolean("animations", true); }
    public void setAnimations(boolean v) { sp.edit().putBoolean("animations", v).apply(); }
    public boolean getShowTimestamps() { return sp.getBoolean("show_timestamps", true); }
    public void setShowTimestamps(boolean v) { sp.edit().putBoolean("show_timestamps", v).apply(); }
    public boolean getShowLatency() { return sp.getBoolean("show_latency", true); }
    public void setShowLatency(boolean v) { sp.edit().putBoolean("show_latency", v).apply(); }
    public int getAutoDeleteDays() { return sp.getInt("auto_delete_days", 0); }
    public void setAutoDeleteDays(int v) { sp.edit().putInt("auto_delete_days", v).apply(); }
    public String getPersonality() { return sp.getString("personality", "Freundlicher Assistent"); }
    public void setPersonality(String v) { sp.edit().putString("personality", v).apply(); }
    public String getCustomPersonalities() { return sp.getString("custom_personalities", "[]"); }
    public void saveCustomPersonalities(String json) { sp.edit().putString("custom_personalities", json).apply(); }

    public List<CustomProvider> getCustomProviders() {
        String json = sp.getString("custom_providers", "");
        if (!json.isEmpty()) {
            try {
                JSONArray arr = new JSONArray(json);
                List<CustomProvider> list = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) list.add(CustomProvider.fromJson(arr.getJSONObject(i)));
                return list;
            } catch (JSONException ignored) {}
        }
        return new ArrayList<>();
    }
    public void addCustomProvider(CustomProvider p) {
        List<CustomProvider> list = getCustomProviders(); list.add(p);
        try {
            JSONArray arr = new JSONArray();
            for (CustomProvider cp : list) arr.put(cp.toJson());
            sp.edit().putString("custom_providers", arr.toString()).apply();
        } catch (JSONException ignored) {}
    }
    public void removeCustomProvider(String name) {
        List<CustomProvider> list = getCustomProviders();
        list.removeIf(pr -> pr.name.equals(name));
        try {
            JSONArray arr = new JSONArray();
            for (CustomProvider cp : list) arr.put(cp.toJson());
            sp.edit().putString("custom_providers", arr.toString()).apply();
        } catch (JSONException ignored) {}
    }

    public JSONArray filterHistoryByAge(JSONArray arr, int maxAgeDays) {
        if (maxAgeDays <= 0) return arr;
        long now = System.currentTimeMillis();
        long cutoff = now - (maxAgeDays * 24L * 60 * 60 * 1000);
        JSONArray filtered = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject obj = arr.getJSONObject(i);
                long ts = obj.optLong("timestamp", now);
                if (ts >= cutoff) filtered.put(obj);
            } catch (JSONException ignored) {}
        }
        return filtered;
    }
}
PREF_EOF

cat > app/src/main/java/com/ghostmax/ApiClient.java << 'API_EOF'
package com.ghostmax;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // FIX: nicht mehr final, damit Geheim-Modus einen Proxy nachtraeglich einbauen kann
    private static OkHttpClient CLIENT;
    private static Proxy currentProxy = null;

    private static void rebuildClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);
        if (currentProxy != null) builder.proxy(currentProxy);
        CLIENT = builder.build();
    }
    static { rebuildClient(); }

    public static void setProxy(Proxy proxy) { currentProxy = proxy; rebuildClient(); }
    public static void disableProxy() { currentProxy = null; rebuildClient(); }

    private static final Map<String,ProviderInfo> BUILTIN = new LinkedHashMap<>();
    static {
        BUILTIN.put("LocalLLM", new ProviderInfo("LocalLLM", "http://127.0.0.1:8080/completion", "", "local"));
        BUILTIN.put("OpenRouter", new ProviderInfo("OpenRouter", "https://openrouter.ai/api/v1/chat/completions", "meta-llama/llama-3.3-70b-instruct:free", "openai"));
        BUILTIN.put("Groq", new ProviderInfo("Groq", "https://api.groq.com/openai/v1/chat/completions", "llama-3.3-70b-versatile", "openai"));
        BUILTIN.put("Gemini", new ProviderInfo("Gemini", "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent", "gemini-2.5-flash", "gemini"));
        BUILTIN.put("OpenAI", new ProviderInfo("OpenAI", "https://api.openai.com/v1/chat/completions", "gpt-4o", "openai"));
        BUILTIN.put("Anthropic", new ProviderInfo("Anthropic", "https://api.anthropic.com/v1/messages", "claude-3-5-sonnet-20241022", "anthropic"));
        BUILTIN.put("DeepSeek API", new ProviderInfo("DeepSeek API", "https://api.deepseek.com/v1/chat/completions", "deepseek-chat", "openai"));
        BUILTIN.put("Google", new ProviderInfo("Google", "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent", "gemini-2.5-flash", "gemini"));
        BUILTIN.put("Cerebras", new ProviderInfo("Cerebras", "https://api.cerebras.ai/v1/chat/completions", "llama3.1-8b", "openai"));
        BUILTIN.put("Mistral", new ProviderInfo("Mistral", "https://api.mistral.ai/v1/chat/completions", "mistral-large-latest", "openai"));
        // FIX: Cohere war im zuletzt gepasteten Script komplett verschwunden - wieder da, wie gefordert nichts weglassen
        BUILTIN.put("Cohere", new ProviderInfo("Cohere", "https://api.cohere.ai/compatibility/v1/chat/completions", "command-r", "openai"));
        BUILTIN.put("Perplexity", new ProviderInfo("Perplexity", "https://api.perplexity.ai/chat/completions", "llama-3.1-sonar-small-128k-online", "openai"));
    }
    public static final String[] ALL_PROVIDERS = BUILTIN.keySet().toArray(new String[0]);

    public static class Result {
        public String text; public String actualProvider; public boolean isError;
        public boolean isImage; public byte[] imageBytes; public long latency;
        Result(String text, String actualProvider, boolean isError) {
            this.text=text; this.actualProvider=actualProvider; this.isError=isError; latency=0; isImage=false;
        }
    }

    public static List<String> getAllProviderNames(Prefs prefs) {
        List<String> names = new ArrayList<>(Arrays.asList(ALL_PROVIDERS));
        for (CustomProvider cp : prefs.getCustomProviders()) if (!names.contains(cp.name)) names.add(cp.name);
        return names;
    }

    private static ProviderInfo getProviderInfo(String name, Prefs prefs) {
        String url = prefs.getProviderUrl(name);
        String model = prefs.getProviderModel(name);
        ProviderInfo builtin = BUILTIN.get(name);
        if (builtin != null) {
            if (url.isEmpty()) url = builtin.baseUrl;
            if (model.isEmpty()) model = builtin.model;
            return new ProviderInfo(name, url, model, builtin.type);
        }
        for (CustomProvider cp : prefs.getCustomProviders()) {
            if (cp.name.equals(name)) return new ProviderInfo(cp.name, cp.baseUrl, cp.model, "openai");
        }
        return null;
    }

    public static Result callWithFallback(String prefProvider, Prefs prefs, List<ChatMessage> history, String userText, String category) {
        if ("Image".equalsIgnoreCase(category)) {
            long start = System.currentTimeMillis();
            Result r = generateImage(prefProvider, prefs, userText);
            r.latency = System.currentTimeMillis() - start;
            return r;
        }
        String sysPrompt = prefs.getSystemPrompt();
        long start = System.currentTimeMillis();
        Result r = callSingle(prefProvider, prefs, history, userText, sysPrompt);
        r.latency = System.currentTimeMillis()-start;
        if (!r.isError || !prefs.getFallbackEnabled()) return r;
        for (String cand : ALL_PROVIDERS) {
            if (cand.equals(prefProvider)) continue;
            long fbs = System.currentTimeMillis();
            Result alt = callSingle(cand, prefs, history, userText, sysPrompt);
            alt.latency = System.currentTimeMillis()-fbs;
            if (!alt.isError) return alt;
        }
        return r;
    }

    private static Result callSingle(String provider, Prefs prefs, List<ChatMessage> history, String userText, String sysPrompt) {
        ProviderInfo info = getProviderInfo(provider, prefs);
        if (info == null) return new Result("Provider nicht gefunden", provider, true);
        String key = prefs.getApiKey(provider);
        if (!"LocalLLM".equals(provider) && key.isEmpty()) return new Result("Kein API-Key für "+provider, provider, true);
        try {
            String text;
            if ("local".equals(info.type)) {
                text = callLocalLLM(info.baseUrl, userText, sysPrompt);
            } else if ("gemini".equals(info.type)) {
                text = callGemini(info.baseUrl, key, info.model, prefs, history, userText, sysPrompt);
            } else if ("anthropic".equals(info.type)) {
                text = callAnthropic(info.baseUrl, key, info.model, prefs, history, userText, sysPrompt);
            } else {
                text = callOpenAICompatible(info.baseUrl, key, info.model, prefs, history, userText, sysPrompt);
            }
            boolean err = text.startsWith("Fehler")||text.startsWith("Parsing-Fehler")||text.startsWith("Kein ");
            return new Result(text, provider, err);
        } catch (Exception e) {
            return new Result("Fehler: "+e.getMessage(), provider, true);
        }
    }

    // ---- Vollstaendige Implementierungen (keine Platzhalter) ----

    private static String callLocalLLM(String url, String userText, String sysPrompt) throws IOException {
        try {
            String fullPrompt = sysPrompt.isEmpty() ? userText : sysPrompt + "\n\nUser: " + userText + "\nAssistant:";
            JSONObject payload = new JSONObject();
            payload.put("prompt", fullPrompt);
            payload.put("temperature", 0.7);
            payload.put("n_predict", 256);
            Request req = new Request.Builder().url(url)
                    .post(RequestBody.create(MediaType.parse("application/json"), payload.toString()))
                    .build();
            try (Response r = CLIENT.newCall(req).execute()) {
                if (!r.isSuccessful()) return "Fehler " + r.code() + ": " + r.body().string();
                JSONObject obj = new JSONObject(r.body().string());
                return obj.optString("content", "Keine Antwort");
            }
        } catch (Exception e) {
            return "Lokaler Fehler: " + e.getMessage();
        }
    }

    private static String callGemini(String url, String key, String model, Prefs prefs, List<ChatMessage> history, String userText, String sysPrompt) throws IOException {
        try {
            JSONArray contents = new JSONArray();
            int ws = Math.max(0, history.size()-prefs.getHistoryWindow());
            for (int i=ws; i<history.size(); i++) {
                ChatMessage m = history.get(i);
                if (m.isError) continue;
                JSONObject c = new JSONObject(); c.put("role", m.type==ChatMessage.TYPE_USER?"user":"model");
                JSONArray parts = new JSONArray(); parts.put(new JSONObject().put("text", m.text));
                c.put("parts", parts); contents.put(c);
            }
            JSONObject u = new JSONObject(); u.put("role","user");
            u.put("parts", new JSONArray().put(new JSONObject().put("text", userText)));
            contents.put(u);
            JSONObject body = new JSONObject(); body.put("contents", contents);
            if (sysPrompt != null && !sysPrompt.trim().isEmpty()) {
                JSONObject sys = new JSONObject(); sys.put("parts", new JSONArray().put(new JSONObject().put("text", sysPrompt)));
                body.put("system_instruction", sys);
            }
            JSONObject gen = new JSONObject(); gen.put("temperature", prefs.getTemperature()); gen.put("maxOutputTokens", prefs.getMaxTokens());
            body.put("generationConfig", gen);
            Request req = new Request.Builder().url(url).addHeader("x-goog-api-key",key)
                    .post(RequestBody.create(MediaType.parse("application/json"), body.toString())).build();
            try (Response r = CLIENT.newCall(req).execute()) {
                String json = r.body().string();
                if (!r.isSuccessful()) return "Fehler "+r.code()+": "+json;
                return new JSONObject(json).getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
            }
        } catch (Exception e) {
            return "Parsing-Fehler: "+e.getMessage();
        }
    }

    private static String callAnthropic(String url, String key, String model, Prefs prefs, List<ChatMessage> history, String userText, String sysPrompt) throws IOException {
        try {
            JSONArray msgs = new JSONArray();
            int ws = Math.max(0, history.size()-prefs.getHistoryWindow());
            for (int i=ws; i<history.size(); i++) {
                ChatMessage m = history.get(i);
                if (m.isError) continue;
                msgs.put(new JSONObject().put("role", m.type==ChatMessage.TYPE_USER?"user":"assistant").put("content", m.text));
            }
            msgs.put(new JSONObject().put("role","user").put("content", userText));
            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("messages", msgs);
            body.put("max_tokens", prefs.getMaxTokens());
            body.put("temperature", prefs.getTemperature());
            if (sysPrompt != null && !sysPrompt.trim().isEmpty()) body.put("system", sysPrompt);
            Request req = new Request.Builder().url(url)
                    .addHeader("x-api-key", key)
                    .addHeader("anthropic-version", "2023-06-01")
                    .post(RequestBody.create(MediaType.parse("application/json"), body.toString()))
                    .build();
            try (Response r = CLIENT.newCall(req).execute()) {
                String json = r.body().string();
                if (!r.isSuccessful()) return "Fehler "+r.code()+": "+json;
                JSONObject obj = new JSONObject(json);
                JSONArray content = obj.optJSONArray("content");
                if (content != null && content.length() > 0) return content.getJSONObject(0).optString("text", "Keine Antwort");
                return "Keine Antwort vom Modell.";
            }
        } catch (Exception e) {
            return "Anthropic Fehler: " + e.getMessage();
        }
    }

    private static String callOpenAICompatible(String url, String key, String model, Prefs prefs,
                                               List<ChatMessage> history, String userText, String sysPrompt) throws IOException {
        try {
            JSONArray msgs = new JSONArray();
            if (sysPrompt != null && !sysPrompt.trim().isEmpty()) msgs.put(new JSONObject().put("role","system").put("content",sysPrompt));
            int ws = Math.max(0, history.size()-prefs.getHistoryWindow());
            for (int i=ws; i<history.size(); i++) {
                ChatMessage m = history.get(i);
                if (m.isError) continue;
                msgs.put(new JSONObject().put("role", m.type==ChatMessage.TYPE_USER?"user":"assistant").put("content", m.text));
            }
            msgs.put(new JSONObject().put("role","user").put("content", userText));
            JSONObject body = new JSONObject(); body.put("model",model); body.put("messages",msgs);
            body.put("temperature",prefs.getTemperature()); body.put("max_tokens",prefs.getMaxTokens());
            Request.Builder builder = new Request.Builder().url(url);
            if (!key.isEmpty()) builder.addHeader("Authorization","Bearer "+key);
            Request req = builder.post(RequestBody.create(MediaType.parse("application/json"), body.toString())).build();
            for (int attempt=0; attempt<2; attempt++) {
                try (Response r = CLIENT.newCall(req).execute()) {
                    String json = r.body().string();
                    if (r.isSuccessful()) return new JSONObject(json).getJSONArray("choices")
                            .getJSONObject(0).getJSONObject("message").getString("content");
                    if (r.code()<500 || attempt==1) return "Fehler "+r.code()+": "+json;
                } catch (Exception e) { if (attempt==1) return "Parsing-Fehler: "+e.getMessage(); }
            }
            return "Unbekannter Fehler";
        } catch (Exception e) {
            return "Parsing-Fehler: "+e.getMessage();
        }
    }

    // ---- Bildgenerierung: OpenAI DALL-E 3 UND Google Imagen 3 (Imagen war vorher nur eine Fehlermeldung) ----

    public static Result generateImage(String provider, Prefs prefs, String prompt) {
        ProviderInfo info = getProviderInfo(provider, prefs);
        if (info == null) return new Result("Provider nicht gefunden", provider, true);
        String key = prefs.getApiKey(provider);
        if (key.isEmpty()) return new Result("Kein API-Key für "+provider, provider, true);
        try {
            if ("OpenAI".equals(provider)) return callDalle3(key, prompt);
            if ("Google".equals(provider)) return callImagen(key, prompt);
            return new Result("Bildgenerierung für " + provider + " ist noch nicht implementiert.", provider, true);
        } catch (Exception e) {
            return new Result("Bild-Fehler: " + e.getMessage(), provider, true);
        }
    }

    private static Result callDalle3(String key, String prompt) throws IOException, JSONException {
        JSONObject body = new JSONObject();
        body.put("model", "dall-e-3");
        body.put("prompt", prompt);
        body.put("n", 1);
        body.put("size", "1024x1024");
        body.put("response_format", "b64_json");
        Request req = new Request.Builder().url("https://api.openai.com/v1/images/generations")
                .addHeader("Authorization", "Bearer " + key)
                .post(RequestBody.create(MediaType.parse("application/json"), body.toString()))
                .build();
        try (Response r = CLIENT.newCall(req).execute()) {
            String json = r.body().string();
            if (!r.isSuccessful()) return new Result("Fehler "+r.code()+": "+json, "OpenAI", true);
            JSONObject obj = new JSONObject(json);
            JSONArray data = obj.getJSONArray("data");
            if (data.length() == 0) return new Result("Kein Bild erhalten", "OpenAI", true);
            String b64 = data.getJSONObject(0).getString("b64_json");
            Result res = new Result("🖼️ Bild erzeugt (DALL-E 3)", "OpenAI", false);
            res.isImage = true;
            res.imageBytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT);
            return res;
        }
    }

    // FIX (verifiziert gegen Google-Doku): Imagen laeuft ueber :predict mit instances/parameters,
    // NICHT ueber :generateContent - das vorherige Format war schlicht falsch und haette immer
    // einen Fehler zurueckgegeben. Modellname jetzt auch korrekt versioniert (imagen-3.0-generate-002).
    private static Result callImagen(String key, String prompt) throws IOException, JSONException {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict?key=" + key;
        JSONObject instance = new JSONObject();
        instance.put("prompt", prompt);
        JSONObject parameters = new JSONObject();
        parameters.put("sampleCount", 1);
        JSONObject body = new JSONObject();
        body.put("instances", new JSONArray().put(instance));
        body.put("parameters", parameters);
        Request req = new Request.Builder().url(url)
                .post(RequestBody.create(MediaType.parse("application/json"), body.toString()))
                .build();
        try (Response r = CLIENT.newCall(req).execute()) {
            String json = r.body().string();
            if (!r.isSuccessful()) return new Result("Fehler "+r.code()+": "+json, "Google", true);
            JSONObject obj = new JSONObject(json);
            JSONArray predictions = obj.optJSONArray("predictions");
            if (predictions == null || predictions.length() == 0) return new Result("Kein Bild erhalten", "Google", true);
            String b64 = predictions.getJSONObject(0).getString("bytesBase64Encoded");
            Result res = new Result("🖼️ Bild erzeugt (Imagen 3)", "Google", false);
            res.isImage = true;
            res.imageBytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT);
            return res;
        }
    }
}
API_EOF

cat > app/src/main/java/com/ghostmax/MainActivity.java << 'MAIN_EOF'
package com.ghostmax;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.*;

public class MainActivity extends AppCompatActivity {
    private Prefs prefs;
    private EditText inputText;
    private ImageButton sendButton, stopButton, copyButton;
    private Spinner filterSpinner, modelSpinner, personalitySpinner;
    private RecyclerView chatRecycler;
    private ChatAdapter chatAdapter;
    private String currentProvider = "OpenRouter";
    private String currentCategory = "Chat";
    private String currentPersonality = "Freundlicher Assistent";
    private List<ChatMessage> history = new ArrayList<>();
    private AtomicBoolean isProcessing = new AtomicBoolean(false);
    private Thread currentThread;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private OkHttpClient httpClient = new OkHttpClient();
    private static final String CONTROL_URL = "http://127.0.0.1:8888";
    private List<PersonalityManager.Personality> allPers;
    private List<ModelCatalog.ModelEntry> currentModels = new ArrayList<>();

    // Geheim-Modus / Proxy
    private boolean secretMode = false;

    private static final Map<String, String> KEY_URLS = new HashMap<>();
    static {
        KEY_URLS.put("OpenRouter", "https://openrouter.ai/keys");
        KEY_URLS.put("Groq", "https://console.groq.com/keys");
        KEY_URLS.put("OpenAI", "https://platform.openai.com/api-keys");
        KEY_URLS.put("Anthropic", "https://console.anthropic.com/settings/keys");
        KEY_URLS.put("Google", "https://ai.google.dev/gemini-api/docs/api-key");
        KEY_URLS.put("DeepSeek API", "https://platform.deepseek.com/api_keys");
        KEY_URLS.put("Cerebras", "https://cloud.cerebras.ai/account/api-keys");
        KEY_URLS.put("Mistral", "https://console.mistral.ai/api-keys/");
        KEY_URLS.put("Cohere", "https://dashboard.cohere.ai/api-keys");
        KEY_URLS.put("Perplexity", "https://www.perplexity.ai/settings/api");
        KEY_URLS.put("LocalLLM", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new Prefs(this);
        if (prefs.getDarkMode()) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        currentPersonality = prefs.getPersonality();
        PersonalityManager.Personality p = PersonalityManager.findByName(currentPersonality, prefs);
        prefs.setSystemPrompt(p.systemPrompt);
        prefs.setTemperature(p.temperature);
        prefs.setMaxTokens(p.maxTokens);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        filterSpinner = findViewById(R.id.filterSpinner);
        modelSpinner = findViewById(R.id.modelSpinner);
        personalitySpinner = findViewById(R.id.personalitySpinner);
        chatRecycler = findViewById(R.id.chatRecycler);
        inputText = findViewById(R.id.inputText);
        sendButton = findViewById(R.id.sendButton);
        stopButton = findViewById(R.id.stopButton);
        copyButton = findViewById(R.id.copyButton);

        chatAdapter = new ChatAdapter();
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(chatAdapter);

        if (!prefs.getCopyButton()) copyButton.setVisibility(View.GONE);

        String[] filterOptions = {"No Filter", "Filtered"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filterOptions);
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) { refreshModels(filterOptions[pos]); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos >= 0 && pos < currentModels.size()) applyModel(currentModels.get(pos));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadPersonalities();
        personalitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos >= 0 && pos < allPers.size()) {
                    PersonalityManager.Personality sel = allPers.get(pos);
                    currentPersonality = sel.name;
                    prefs.setPersonality(sel.name);
                    prefs.setSystemPrompt(sel.systemPrompt);
                    prefs.setTemperature(sel.temperature);
                    prefs.setMaxTokens(sel.maxTokens);
                    if (sel.provider != null && !sel.provider.isEmpty()) {
                        boolean found = false;
                        for (int mi = 0; mi < currentModels.size(); mi++) {
                            if (currentModels.get(mi).provider.equals(sel.provider)) {
                                modelSpinner.setSelection(mi);
                                applyModel(currentModels.get(mi));
                                found = true;
                                break;
                            }
                        }
                        if (!found) Toast.makeText(MainActivity.this, "Hinweis: Provider \"" + sel.provider + "\" aktuell nicht im Filter sichtbar.", Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        inputText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) { sendMessage(); return true; }
            return false;
        });
        sendButton.setOnClickListener(v -> sendMessage());
        stopButton.setOnClickListener(v -> stopProcessing());
        copyButton.setOnClickListener(v -> copyLastMessage());

        loadHistory();
        addSystemMessage("GhostMax bereit");

        filterSpinner.setSelection(0);
        refreshModels("No Filter");
        if (!currentModels.isEmpty()) {
            modelSpinner.setSelection(0);
            applyModel(currentModels.get(0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem secretItem = menu.findItem(R.id.action_secret);
        if (secretItem != null) secretItem.setTitle(secretMode ? "🕵️✔️ Geheim-Modus (aktiv)" : "🕵️ Geheim-Modus");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) { showSettingsDialog(); return true; }
        if (id == R.id.action_add_provider) { ProviderManagerDialog.show(this, prefs, () -> refreshModels(filterSpinner.getSelectedItem().toString())); return true; }
        if (id == R.id.action_catalog) { showModelCatalog(); return true; }
        if (id == R.id.action_add_personality) { showAddPersonalityDialog(); return true; }
        if (id == R.id.action_image) { showImageDialog(); return true; }
        if (id == R.id.action_server) { toggleLocalServer(); return true; }
        if (id == R.id.action_secret) { toggleSecretMode(); return true; }
        if (id == R.id.action_clear) { confirmClearChat(); return true; }
        return super.onOptionsItemSelected(item);
    }

    // ----- Geheim-Modus / Proxy (aus dem letzten Script uebernommen, vollstaendig) -----
    private void toggleSecretMode() {
        if (!secretMode) {
            new AlertDialog.Builder(this)
                    .setTitle("🕵️ Geheim-Modus")
                    .setMessage("Im Geheim-Modus wird der Chatverlauf nicht gespeichert. Proxy aktivieren?")
                    .setPositiveButton("Tor (SOCKS5 127.0.0.1:9050)", (d, w) -> {
                        setProxy(Proxy.Type.SOCKS, "127.0.0.1", 9050);
                        activateSecretMode();
                    })
                    .setNeutralButton("Eigener Proxy", (d, w) -> showProxyDialog())
                    .setNegativeButton("Nur ohne Speichern (kein Proxy)", (d, w) -> activateSecretMode())
                    .show();
        } else {
            secretMode = false;
            prefs.setSecureMode(false);
            ApiClient.disableProxy();
            invalidateOptionsMenu();
            Toast.makeText(this, "Geheim-Modus deaktiviert", Toast.LENGTH_SHORT).show();
        }
    }

    private void activateSecretMode() {
        secretMode = true;
        prefs.setSecureMode(true);
        invalidateOptionsMenu();
        Toast.makeText(this, "🕵️ Geheim-Modus aktiv – Verlauf wird nicht gespeichert", Toast.LENGTH_LONG).show();
    }

    private void showProxyDialog() {
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        lay.setPadding(pad, pad, pad, pad);

        Spinner typeSpinner = new Spinner(this);
        typeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"HTTP", "SOCKS"}));
        lay.addView(typeSpinner);

        EditText hostInput = new EditText(this);
        hostInput.setHint("Host (z.B. 127.0.0.1)");
        lay.addView(hostInput);

        EditText portInput = new EditText(this);
        portInput.setHint("Port (z.B. 9050)");
        portInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        lay.addView(portInput);

        new AlertDialog.Builder(this)
                .setTitle("Proxy-Einstellungen")
                .setView(lay)
                .setPositiveButton("OK", (d, w) -> {
                    String host = hostInput.getText().toString().trim();
                    String portStr = portInput.getText().toString().trim();
                    if (host.isEmpty() || portStr.isEmpty()) {
                        Toast.makeText(this, "Bitte Host und Port angeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int port;
                    try { port = Integer.parseInt(portStr); } catch (NumberFormatException e) {
                        Toast.makeText(this, "Ungültiger Port", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Proxy.Type type = typeSpinner.getSelectedItemPosition() == 0 ? Proxy.Type.HTTP : Proxy.Type.SOCKS;
                    setProxy(type, host, port);
                    activateSecretMode();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void setProxy(Proxy.Type type, String host, int port) {
        ApiClient.setProxy(new Proxy(type, new InetSocketAddress(host, port)));
    }

    private void refreshModels(String filterType) {
        currentModels = new ArrayList<>(ModelCatalog.getByFilterType(filterType));
        for (CustomProvider cp : prefs.getCustomProviders()) {
            currentModels.add(new ModelCatalog.ModelEntry(cp.name, cp.name, cp.model, "Custom", filterType, "", true, false, cp.baseUrl));
        }
        List<String> modelNames = new ArrayList<>();
        for (ModelCatalog.ModelEntry e : currentModels) {
            String label = e.name + " (" + e.provider + ")";
            if (e.free) label += " 🆓";
            modelNames.add(label);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, modelNames);
        modelSpinner.setAdapter(adapter);
        if (!currentModels.isEmpty()) {
            modelSpinner.setSelection(0);
            applyModel(currentModels.get(0));
        }
    }

    private void applyModel(ModelCatalog.ModelEntry entry) {
        currentProvider = entry.provider;
        currentCategory = entry.category;
        prefs.setProviderUrl(entry.provider, entry.apiBase);
        prefs.setProviderModel(entry.provider, entry.modelId);
        inputText.setHint("Nachricht an " + entry.name + " ...");
        String key = prefs.getApiKey(entry.provider);
        if (key.isEmpty() && !entry.provider.equals("LocalLLM")) {
            Toast.makeText(this, "🔑 API-Key für " + entry.provider + " fehlt – Einstellungen öffnen.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadPersonalities() {
        allPers = new ArrayList<>();
        allPers.addAll(PersonalityManager.getDefaultPersonalities());
        allPers.addAll(PersonalityManager.loadCustom(prefs));
        List<String> persNames = new ArrayList<>();
        for (PersonalityManager.Personality per : allPers) persNames.add(per.icon + " " + per.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, persNames);
        personalitySpinner.setAdapter(adapter);
        int idx = -1;
        for (int i = 0; i < allPers.size(); i++) if (allPers.get(i).name.equals(currentPersonality)) { idx = i; break; }
        if (idx >= 0) personalitySpinner.setSelection(idx);
    }

    private void showSettingsDialog() {
        ScrollView sc = new ScrollView(this);
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        lay.setPadding(pad, pad, pad, pad);

        for (String prov : ApiClient.ALL_PROVIDERS) {
            LinearLayout provLayout = new LinearLayout(this);
            provLayout.setOrientation(LinearLayout.VERTICAL);
            provLayout.setPadding(0, dp(10), 0, dp(10));

            TextView tv = new TextView(this);
            tv.setText("🔹 " + prov);
            tv.setTextSize(16);
            provLayout.addView(tv);

            EditText keyInput = new EditText(this);
            keyInput.setHint("API-Key");
            keyInput.setText(prefs.getApiKey(prov));
            keyInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            provLayout.addView(keyInput);
            keyInput.setTag(prov + "_key");

            EditText urlInput = new EditText(this);
            urlInput.setHint("URL (überschreibt Standard)");
            urlInput.setText(prefs.getProviderUrl(prov).isEmpty() ? getDefaultUrl(prov) : prefs.getProviderUrl(prov));
            provLayout.addView(urlInput);
            urlInput.setTag(prov + "_url");

            EditText modelInput = new EditText(this);
            modelInput.setHint("Modell (überschreibt Standard)");
            modelInput.setText(prefs.getProviderModel(prov).isEmpty() ? getDefaultModel(prov) : prefs.getProviderModel(prov));
            provLayout.addView(modelInput);
            modelInput.setTag(prov + "_model");

            String keyUrl = KEY_URLS.get(prov);
            if (keyUrl != null && !keyUrl.isEmpty()) {
                Button keyButton = new Button(this);
                keyButton.setText("🔑 API-Key beziehen");
                keyButton.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(keyUrl))));
                provLayout.addView(keyButton);
            } else {
                TextView info = new TextView(this);
                info.setText("(kein API-Key erforderlich)");
                info.setTextSize(12);
                info.setTextColor(0xFF888888);
                provLayout.addView(info);
            }
            lay.addView(provLayout);
        }

        TextView tempLabel = new TextView(this);
        tempLabel.setText("🌡️ Temperatur: " + prefs.getTemperature());
        lay.addView(tempLabel);
        SeekBar tempBar = new SeekBar(this);
        tempBar.setMax(200);
        tempBar.setProgress((int) (prefs.getTemperature() * 100));
        tempBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int pr, boolean fromUser) { tempLabel.setText("🌡️ Temperatur: " + String.format("%.2f", pr / 100.0)); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        lay.addView(tempBar);

        TextView tokLabel = new TextView(this);
        tokLabel.setText("📝 Tokens: " + prefs.getMaxTokens());
        lay.addView(tokLabel);
        SeekBar tokBar = new SeekBar(this);
        tokBar.setMax(4000);
        tokBar.setProgress(prefs.getMaxTokens());
        tokBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int pr, boolean fromUser) { tokLabel.setText("📝 Tokens: " + pr); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        lay.addView(tokBar);

        TextView promptLabel = new TextView(this);
        promptLabel.setText("📝 System-Prompt:");
        lay.addView(promptLabel);
        EditText promptInput = new EditText(this);
        promptInput.setText(prefs.getSystemPrompt());
        promptInput.setMinLines(3);
        lay.addView(promptInput);

        TextView histLabel = new TextView(this);
        histLabel.setText("📜 Historie (Anzahl Nachrichten): " + prefs.getHistoryWindow());
        lay.addView(histLabel);
        SeekBar histBar = new SeekBar(this);
        histBar.setMax(30);
        histBar.setProgress(prefs.getHistoryWindow());
        histBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int pr, boolean fromUser) { histLabel.setText("📜 Historie (Anzahl Nachrichten): " + pr); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        lay.addView(histBar);

        CheckBox fallbackCheck = new CheckBox(this);
        fallbackCheck.setText("🔄 Fallback aktivieren");
        fallbackCheck.setChecked(prefs.getFallbackEnabled());
        lay.addView(fallbackCheck);

        CheckBox darkCheck = new CheckBox(this);
        darkCheck.setText("🌙 Dark Mode");
        darkCheck.setChecked(prefs.getDarkMode());
        lay.addView(darkCheck);

        CheckBox showProvCheck = new CheckBox(this);
        showProvCheck.setText("🏷️ Provider anzeigen");
        showProvCheck.setChecked(prefs.getShowProvider());
        lay.addView(showProvCheck);

        CheckBox autoScrollCheck = new CheckBox(this);
        autoScrollCheck.setText("📜 Auto-Scroll");
        autoScrollCheck.setChecked(prefs.getAutoScroll());
        lay.addView(autoScrollCheck);

        CheckBox animCheck = new CheckBox(this);
        animCheck.setText("🎬 Animationen");
        animCheck.setChecked(prefs.getAnimations());
        lay.addView(animCheck);

        CheckBox timeCheck = new CheckBox(this);
        timeCheck.setText("🕒 Timestamps");
        timeCheck.setChecked(prefs.getShowTimestamps());
        lay.addView(timeCheck);

        CheckBox latCheck = new CheckBox(this);
        latCheck.setText("⚡ Latenz anzeigen");
        latCheck.setChecked(prefs.getShowLatency());
        lay.addView(latCheck);

        CheckBox notiCheck = new CheckBox(this);
        notiCheck.setText("🔔 Benachrichtigungen");
        notiCheck.setChecked(prefs.getNotifications());
        lay.addView(notiCheck);

        CheckBox copyCheck = new CheckBox(this);
        copyCheck.setText("📋 Copy-Button");
        copyCheck.setChecked(prefs.getCopyButton());
        lay.addView(copyCheck);

        TextView sizeLabel = new TextView(this);
        sizeLabel.setText("🔤 Schriftgröße: " + prefs.getFontSize());
        lay.addView(sizeLabel);
        SeekBar sizeBar = new SeekBar(this);
        sizeBar.setMax(30);
        sizeBar.setProgress(prefs.getFontSize() - 10);
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int pr, boolean fromUser) { sizeLabel.setText("🔤 Schriftgröße: " + (pr + 10)); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        lay.addView(sizeBar);

        TextView delLabel = new TextView(this);
        delLabel.setText("🗑️ Auto-Löschen (Tage, 0 = deaktiviert): " + prefs.getAutoDeleteDays());
        lay.addView(delLabel);
        SeekBar delBar = new SeekBar(this);
        delBar.setMax(30);
        delBar.setProgress(prefs.getAutoDeleteDays());
        delBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int pr, boolean fromUser) { delLabel.setText("🗑️ Auto-Löschen (Tage, 0 = deaktiviert): " + pr); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        lay.addView(delBar);

        // FIX: Auto-Loeschen ist jetzt echt aktiv (siehe loadHistory/saveHistory mit Zeitstempel)
        TextView delHint = new TextView(this);
        delHint.setText("✅ Auto-Löschen ist jetzt aktiv und wirkt beim naechsten App-Start.");
        delHint.setTextSize(11);
        delHint.setTextColor(0xFF2E7D32);
        lay.addView(delHint);

        sc.addView(lay);

        new AlertDialog.Builder(this)
                .setTitle("⚙️ Einstellungen")
                .setView(sc)
                .setPositiveButton("Speichern", (d, w) -> {
                    for (String prov : ApiClient.ALL_PROVIDERS) {
                        EditText k = lay.findViewWithTag(prov + "_key");
                        if (k != null) prefs.setApiKey(prov, k.getText().toString().trim());
                        EditText u = lay.findViewWithTag(prov + "_url");
                        if (u != null) prefs.setProviderUrl(prov, u.getText().toString().trim());
                        EditText m = lay.findViewWithTag(prov + "_model");
                        if (m != null) prefs.setProviderModel(prov, m.getText().toString().trim());
                    }
                    prefs.setTemperature(tempBar.getProgress() / 100.0);
                    prefs.setMaxTokens(tokBar.getProgress());
                    prefs.setSystemPrompt(promptInput.getText().toString().trim());
                    prefs.setHistoryWindow(histBar.getProgress());
                    prefs.setFallbackEnabled(fallbackCheck.isChecked());
                    prefs.setDarkMode(darkCheck.isChecked());
                    prefs.setShowProvider(showProvCheck.isChecked());
                    prefs.setAutoScroll(autoScrollCheck.isChecked());
                    prefs.setAnimations(animCheck.isChecked());
                    prefs.setShowTimestamps(timeCheck.isChecked());
                    prefs.setShowLatency(latCheck.isChecked());
                    prefs.setNotifications(notiCheck.isChecked());
                    prefs.setCopyButton(copyCheck.isChecked());
                    prefs.setFontSize(sizeBar.getProgress() + 10);
                    prefs.setAutoDeleteDays(delBar.getProgress());
                    copyButton.setVisibility(copyCheck.isChecked() ? View.VISIBLE : View.GONE);

                    if (darkCheck.isChecked()) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                    refreshModels(filterSpinner.getSelectedItem().toString());
                    Toast.makeText(this, "✅ Einstellungen gespeichert", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showModelCatalog() {
        final String[] mainCategories = {"No Filter", "Filtered"};
        new AlertDialog.Builder(this)
                .setTitle("📚 KI-Katalog")
                .setItems(mainCategories, (d, which) -> {
                    String filterType = mainCategories[which];
                    final List<ModelCatalog.ModelEntry> entries = ModelCatalog.getByFilterType(filterType);
                    if (entries.isEmpty()) {
                        Toast.makeText(this, "Keine Modelle in dieser Kategorie.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final List<String> itemLabels = new ArrayList<>();
                    final List<ModelCatalog.ModelEntry> orderedEntries = new ArrayList<>();
                    for (String cat : ModelCatalog.getAllCategories(filterType)) {
                        for (ModelCatalog.ModelEntry e : entries) {
                            if (e.category.equals(cat)) {
                                String label = cat + " | " + e.name + " (" + e.provider + ")";
                                if (e.free) label += " 🆓";
                                if (e.needsKey) label += " 🔑";
                                itemLabels.add(label);
                                orderedEntries.add(e);
                            }
                        }
                    }
                    new AlertDialog.Builder(this)
                            .setTitle("🔹 " + filterType)
                            .setItems(itemLabels.toArray(new String[0]), (d2, which2) -> {
                                ModelCatalog.ModelEntry selected = orderedEntries.get(which2);
                                String key = prefs.getApiKey(selected.provider);
                                if (key.isEmpty() && !selected.provider.equals("LocalLLM")) {
                                    Toast.makeText(this, "🔑 Key für " + selected.provider + " fehlt – bitte in Einstellungen hinterlegen.", Toast.LENGTH_LONG).show();
                                    showSettingsDialog();
                                    return;
                                }
                                String name = selected.name + " (" + selected.provider + ")";
                                prefs.addCustomProvider(new CustomProvider(name, selected.apiBase, key, selected.modelId, selected.category.toLowerCase()));
                                Toast.makeText(this, "✅ " + selected.name + " als Provider hinzugefügt!", Toast.LENGTH_LONG).show();
                                refreshModels(filterType);
                                for (int mi = 0; mi < currentModels.size(); mi++) {
                                    if (currentModels.get(mi).name.equals(name)) {
                                        modelSpinner.setSelection(mi);
                                        applyModel(currentModels.get(mi));
                                        break;
                                    }
                                }
                            })
                            .setNegativeButton("Zurück", null)
                            .show();
                })
                .setNegativeButton("Schließen", null)
                .show();
    }

    private void showAddPersonalityDialog() {
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        lay.setPadding(pad, pad, pad, pad);

        EditText nameInput = new EditText(this); nameInput.setHint("Name"); lay.addView(nameInput);
        EditText descInput = new EditText(this); descInput.setHint("Beschreibung"); lay.addView(descInput);
        EditText promptInput = new EditText(this); promptInput.setHint("System-Prompt"); lay.addView(promptInput);

        Spinner provSpinner = new Spinner(this);
        List<String> provNames = ApiClient.getAllProviderNames(prefs);
        provSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, provNames));
        lay.addView(provSpinner);

        EditText tempInput = new EditText(this); tempInput.setHint("Temperature (0.0-2.0)"); lay.addView(tempInput);
        EditText tokInput = new EditText(this); tokInput.setHint("Max Tokens"); lay.addView(tokInput);
        EditText iconInput = new EditText(this); iconInput.setHint("Icon (Emoji)"); lay.addView(iconInput);

        new AlertDialog.Builder(this)
                .setTitle("🧠 Neue Persönlichkeit")
                .setView(lay)
                .setPositiveButton("Speichern", (d, w) -> {
                    String name = nameInput.getText().toString().trim();
                    String desc = descInput.getText().toString().trim();
                    String prompt = promptInput.getText().toString().trim();
                    String provider = provNames.get(provSpinner.getSelectedItemPosition());
                    double temp = 0.7;
                    try { temp = Double.parseDouble(tempInput.getText().toString()); } catch (Exception ignored) {}
                    int tokens = 1024;
                    try { tokens = Integer.parseInt(tokInput.getText().toString()); } catch (Exception ignored) {}
                    String icon = iconInput.getText().toString().trim();
                    if (icon.isEmpty()) icon = "🧠";
                    if (name.isEmpty() || prompt.isEmpty()) {
                        Toast.makeText(this, "Name und Prompt benötigt", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PersonalityManager.Personality newP = new PersonalityManager.Personality(name, desc, prompt, provider, temp, tokens, icon);
                    List<PersonalityManager.Personality> customs = PersonalityManager.loadCustom(prefs);
                    customs.add(newP);
                    PersonalityManager.saveCustom(customs, prefs);
                    Toast.makeText(this, "Persönlichkeit gespeichert!", Toast.LENGTH_SHORT).show();
                    loadPersonalities();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showImageDialog() {
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        lay.setPadding(pad, pad, pad, pad);
        TextView hint = new TextView(this);
        hint.setText("Bildbeschreibung eingeben (aktuell: OpenAI DALL-E 3 und Google Imagen 3):");
        lay.addView(hint);
        EditText promptInput = new EditText(this);
        promptInput.setHint("z.B. ein Fuchs im Schnee, digital art");
        lay.addView(promptInput);

        Spinner provSpinner = new Spinner(this);
        provSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"OpenAI (DALL-E 3)", "Google (Imagen 3)"}));
        lay.addView(provSpinner);

        new AlertDialog.Builder(this)
                .setTitle("🖼️ Bildgenerierung")
                .setView(lay)
                .setPositiveButton("Erzeugen", (d, w) -> {
                    String prompt = promptInput.getText().toString().trim();
                    if (prompt.isEmpty()) return;
                    String provider = provSpinner.getSelectedItemPosition() == 0 ? "OpenAI" : "Google";
                    addMessage(prompt, true, null);
                    addSystemMessage("⏳ Bild wird erzeugt...");
                    new Thread(() -> {
                        ApiClient.Result res = ApiClient.generateImage(provider, prefs, prompt);
                        runOnUiThread(() -> {
                            removeLastSystemMessage();
                            if (res.isImage && res.imageBytes != null) {
                                String path = saveImageToDownloads(res.imageBytes);
                                if (path != null) addMessage("🖼️ Bild gespeichert: " + path, false, null);
                                else addMessage("❌ Bild konnte nicht gespeichert werden.", false, null);
                            } else {
                                addMessage(res.text, false, null);
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    // FIX: App-eigener externer Speicher statt oeffentlichem Pictures-Ordner - braucht auf Android 10+
    // (Scoped Storage) keine zusaetzliche Permission und schlaegt daher nicht zur Laufzeit fehl.
    // Erreichbar ueber einen Dateimanager unter Android/data/com.ghostmax/files/Pictures.
    private String saveImageToDownloads(byte[] bytes) {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (dir == null) return null;
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, "ghostmax_" + System.currentTimeMillis() + ".png");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(bytes);
            }
            return out.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private void confirmClearChat() {
        new AlertDialog.Builder(this)
                .setTitle("Chat leeren?")
                .setMessage("Der gesamte sichtbare Verlauf wird gelöscht.")
                .setPositiveButton("Löschen", (d, w) -> {
                    history.clear();
                    chatAdapter.clear();
                    saveHistory();
                    addSystemMessage("Chat geleert.");
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void toggleLocalServer() {
        new Thread(() -> {
            try {
                Request statusReq = new Request.Builder().url(CONTROL_URL + "/status").get().build();
                Response resp = httpClient.newCall(statusReq).execute();
                String status = resp.body().string().trim();
                if ("RUNNING".equals(status)) {
                    httpClient.newCall(new Request.Builder().url(CONTROL_URL + "/stop").post(RequestBody.create(null, "")).build()).execute();
                    runOnUiThread(() -> addSystemMessage("🖥️ Server gestoppt"));
                } else {
                    httpClient.newCall(new Request.Builder().url(CONTROL_URL + "/start").post(RequestBody.create(null, "")).build()).execute();
                    runOnUiThread(() -> addSystemMessage("🖥️ Server gestartet"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> addSystemMessage("❌ Steuerdienst nicht erreichbar. Läuft llama_control.sh?"));
            }
        }).start();
    }

    private String getDefaultUrl(String prov) {
        switch (prov) {
            case "LocalLLM": return "http://127.0.0.1:8080/completion";
            case "OpenRouter": return "https://openrouter.ai/api/v1/chat/completions";
            case "Groq": return "https://api.groq.com/openai/v1/chat/completions";
            case "Gemini": case "Google": return "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
            case "OpenAI": return "https://api.openai.com/v1/chat/completions";
            case "Anthropic": return "https://api.anthropic.com/v1/messages";
            case "DeepSeek API": return "https://api.deepseek.com/v1/chat/completions";
            case "Cerebras": return "https://api.cerebras.ai/v1/chat/completions";
            case "Mistral": return "https://api.mistral.ai/v1/chat/completions";
            case "Cohere": return "https://api.cohere.ai/compatibility/v1/chat/completions";
            case "Perplexity": return "https://api.perplexity.ai/chat/completions";
            default: return "";
        }
    }

    private String getDefaultModel(String prov) {
        switch (prov) {
            case "LocalLLM": return "";
            case "OpenRouter": return "meta-llama/llama-3.3-70b-instruct:free";
            case "Groq": return "llama-3.3-70b-versatile";
            case "Gemini": case "Google": return "gemini-2.5-flash";
            case "OpenAI": return "gpt-4o";
            case "Anthropic": return "claude-3-5-sonnet-20241022";
            case "DeepSeek API": return "deepseek-chat";
            case "Cerebras": return "llama3.1-8b";
            case "Mistral": return "mistral-large-latest";
            case "Cohere": return "command-r";
            case "Perplexity": return "llama-3.1-sonar-small-128k-online";
            default: return "";
        }
    }

    private void sendMessage() {
        if (isProcessing.get()) return;
        String userText = inputText.getText().toString().trim();
        if (userText.isEmpty()) return;
        inputText.setText("");
        addMessage(userText, true, null);
        history.add(new ChatMessage(ChatMessage.TYPE_USER, userText, false));
        addSystemMessage("⏳ Antwort wird geladen...");
        isProcessing.set(true);
        sendButton.setEnabled(false);
        stopButton.setEnabled(true);
        stopButton.setAlpha(1.0f);
        currentThread = new Thread(() -> {
            try {
                ApiClient.Result result = ApiClient.callWithFallback(currentProvider, prefs, history, userText, currentCategory);
                runOnUiThread(() -> {
                    if (!isProcessing.get()) return;
                    removeLastSystemMessage();
                    if (result.isError) {
                        addMessage("❌ Fehler: " + result.text, false, null);
                        history.add(new ChatMessage(ChatMessage.TYPE_ASSISTANT, "Fehler: " + result.text, true));
                    } else {
                        String meta = null;
                        if (prefs.getShowProvider()) meta = result.actualProvider + (prefs.getShowLatency() ? " · " + result.latency + "ms" : "");
                        if (prefs.getShowTimestamps()) meta = (meta == null ? "" : meta + " · ") + timeFormat.format(new Date());
                        addMessage(result.text, false, meta);
                        history.add(new ChatMessage(ChatMessage.TYPE_ASSISTANT, result.text, false));
                    }
                    isProcessing.set(false);
                    sendButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    stopButton.setAlpha(0.4f);
                    saveHistory();
                    if (prefs.getNotifications()) Toast.makeText(MainActivity.this, "Neue Antwort von " + result.actualProvider, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    removeLastSystemMessage();
                    addMessage("❌ Fehler: " + e.getMessage(), false, null);
                    isProcessing.set(false);
                    sendButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    stopButton.setAlpha(0.4f);
                });
            }
        });
        currentThread.start();
    }

    private void stopProcessing() {
        isProcessing.set(false);
        if (currentThread != null) currentThread.interrupt();
        stopButton.setEnabled(false);
        stopButton.setAlpha(0.4f);
        sendButton.setEnabled(true);
        removeLastSystemMessage();
        addSystemMessage("⏹ Anfrage abgebrochen.");
    }

    private void addMessage(String text, boolean isUser, String meta) {
        chatAdapter.add(new ChatAdapter.DisplayMessage(isUser ? ChatAdapter.TYPE_USER : ChatAdapter.TYPE_BOT, text, meta, false));
        if (prefs.getAutoScroll()) scrollToBottom();
    }

    private void addSystemMessage(String text) {
        chatAdapter.add(new ChatAdapter.DisplayMessage(ChatAdapter.TYPE_SYSTEM, text, null, false));
        if (prefs.getAutoScroll()) scrollToBottom();
    }

    private void removeLastSystemMessage() {
        ChatAdapter.DisplayMessage last = chatAdapter.last();
        if (last != null && last.type == ChatAdapter.TYPE_SYSTEM && last.text.contains("wird geladen")) chatAdapter.removeLast();
    }

    private void scrollToBottom() {
        chatRecycler.post(() -> { if (chatAdapter.size() > 0) chatRecycler.smoothScrollToPosition(chatAdapter.size() - 1); });
    }

    private void copyLastMessage() {
        ChatAdapter.DisplayMessage last = chatAdapter.last();
        if (last != null && last.type == ChatAdapter.TYPE_BOT) {
            android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(android.content.ClipData.newPlainText("Copied Text", last.text));
            Toast.makeText(this, "📋 Kopiert!", Toast.LENGTH_SHORT).show();
        }
    }

    // ----- Historie: jetzt MIT Zeitstempel + echtem Auto-Loeschen (vorher nur eine Attrappe) -----
    private void loadHistory() {
        try {
            String rawJson = prefs.getHistory();
            if (rawJson != null && !rawJson.isEmpty() && !"[]".equals(rawJson)) {
                org.json.JSONArray rawArr = new org.json.JSONArray(rawJson);
                org.json.JSONArray arr = prefs.filterHistoryByAge(rawArr, prefs.getAutoDeleteDays());
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject obj = arr.getJSONObject(i);
                    int type = obj.getInt("type");
                    String text = obj.getString("text");
                    boolean err = obj.optBoolean("isError", false);
                    history.add(new ChatMessage(type, text, err));
                    if (type == ChatMessage.TYPE_USER) addMessage(text, true, null);
                    else if (type == ChatMessage.TYPE_ASSISTANT) addMessage(text, false, null);
                }
                // Wenn durch Auto-Loeschen Eintraege weggefallen sind, das gekuerzte Ergebnis gleich zurueckschreiben
                if (arr.length() != rawArr.length()) {
                    prefs.saveHistory(arr.toString());
                }
            }
        } catch (Exception ignored) {}
    }

    private void saveHistory() {
        try {
            org.json.JSONArray arr = new org.json.JSONArray();
            for (ChatMessage m : history) {
                org.json.JSONObject o = new org.json.JSONObject();
                o.put("type", m.type);
                o.put("text", m.text);
                o.put("isError", m.isError);
                o.put("timestamp", System.currentTimeMillis());
                arr.put(o);
            }
            prefs.saveHistory(arr.toString());
        } catch (Exception ignored) {}
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
MAIN_EOF

cat > app/src/main/java/com/ghostmax/ModelCatalog.java << 'MODEL_EOF'
package com.ghostmax;
import java.util.*;

public class ModelCatalog {
    public static class ModelEntry {
        public final String name, provider, modelId, category, filterType, description, apiBase;
        public final boolean needsKey, free;
        public ModelEntry(String name, String provider, String modelId, String category,
                          String filterType, String description, boolean needsKey, boolean free, String apiBase) {
            this.name = name; this.provider = provider; this.modelId = modelId; this.category = category;
            this.filterType = filterType; this.description = description; this.needsKey = needsKey; this.free = free; this.apiBase = apiBase;
        }
    }

    public static List<ModelEntry> getAll() {
        List<ModelEntry> list = new ArrayList<>();
        list.add(new ModelEntry("Llama 3.3 70B Instruct", "OpenRouter", "meta-llama/llama-3.3-70b-instruct:free", "Chat", "No Filter", "", true, true, "https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Dolphin 2.9.1 Mixtral", "OpenRouter", "cognitivecomputations/dolphin-2.9.1-mixtral-8x7b", "Chat", "No Filter", "", true, true, "https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Mistral 7B Instruct", "OpenRouter", "mistralai/mistral-7b-instruct:free", "Chat", "No Filter", "", true, true, "https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Phi-3.5 Mini", "OpenRouter", "microsoft/phi-3.5-mini-128k-instruct:free", "Chat", "No Filter", "", true, true, "https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Hermes 2 Pro", "OpenRouter", "nousresearch/hermes-2-pro-llama-3-8b", "Chat", "No Filter", "", true, true, "https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Mythomax", "OpenRouter", "gryphe/mythomax-l2-13b", "Chat", "No Filter", "", true, true, "https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Qwen 2.5 72B", "OpenRouter", "qwen/qwen-2.5-72b-instruct:free", "Chat", "No Filter", "", true, true, "https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("WizardLM 2 8x22B", "OpenRouter", "microsoft/wizardlm-2-8x22b", "Reasoning", "No Filter", "", true, true, "https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Llama 3.3 70B (Groq)", "Groq", "llama-3.3-70b-versatile", "Chat", "No Filter", "", true, true, "https://api.groq.com/openai/v1"));
        list.add(new ModelEntry("Mixtral 8x7B (Groq)", "Groq", "mixtral-8x7b-32768", "Chat", "No Filter", "", true, true, "https://api.groq.com/openai/v1"));
        list.add(new ModelEntry("Gemma 2 9B (Groq)", "Groq", "gemma2-9b-it", "Chat", "No Filter", "", true, true, "https://api.groq.com/openai/v1"));
        list.add(new ModelEntry("DeepSeek V3", "DeepSeek API", "deepseek-chat", "Coding", "No Filter", "", true, false, "https://api.deepseek.com/v1"));
        list.add(new ModelEntry("DeepSeek Coder V2", "DeepSeek API", "deepseek-coder", "Coding", "No Filter", "", true, false, "https://api.deepseek.com/v1"));
        list.add(new ModelEntry("Llama 3.1 8B (Cerebras)", "Cerebras", "llama3.1-8b", "Chat", "No Filter", "", true, true, "https://api.cerebras.ai/v1"));
        list.add(new ModelEntry("Mistral Large", "Mistral", "mistral-large-latest", "Chat", "No Filter", "", true, false, "https://api.mistral.ai/v1"));
        list.add(new ModelEntry("Mistral Small", "Mistral", "mistral-small-latest", "Chat", "No Filter", "", true, false, "https://api.mistral.ai/v1"));
        list.add(new ModelEntry("Command R", "Cohere", "command-r", "Chat", "No Filter", "", true, false, "https://api.cohere.ai/v1"));
        list.add(new ModelEntry("Command R+", "Cohere", "command-r-plus", "Chat", "No Filter", "", true, false, "https://api.cohere.ai/v1"));
        list.add(new ModelEntry("Llama 3.1 Sonar (Perplexity)", "Perplexity", "llama-3.1-sonar-small-128k-online", "Chat", "No Filter", "", true, true, "https://api.perplexity.ai"));
        list.add(new ModelEntry("Llama-3-8B Lexi Uncensored", "LocalLLM", "", "Chat", "No Filter", "Läuft lokal", false, true, "http://127.0.0.1:8080/completion"));
        list.add(new ModelEntry("GPT-4o", "OpenAI", "gpt-4o", "Chat", "Filtered", "", true, false, "https://api.openai.com/v1"));
        list.add(new ModelEntry("GPT-4o mini", "OpenAI", "gpt-4o-mini", "Chat", "Filtered", "", true, false, "https://api.openai.com/v1"));
        list.add(new ModelEntry("GPT-4 Turbo", "OpenAI", "gpt-4-turbo", "Chat", "Filtered", "", true, false, "https://api.openai.com/v1"));
        list.add(new ModelEntry("GPT-3.5 Turbo", "OpenAI", "gpt-3.5-turbo", "Chat", "Filtered", "", true, false, "https://api.openai.com/v1"));
        list.add(new ModelEntry("DALL-E 3", "OpenAI", "dall-e-3", "Image", "Filtered", "", true, false, "https://api.openai.com/v1"));
        list.add(new ModelEntry("Claude 3.5 Sonnet", "Anthropic", "claude-3-5-sonnet-20241022", "Chat", "Filtered", "", true, false, "https://api.anthropic.com/v1"));
        list.add(new ModelEntry("Claude 3 Opus", "Anthropic", "claude-3-opus-20240229", "Chat", "Filtered", "", true, false, "https://api.anthropic.com/v1"));
        list.add(new ModelEntry("Claude 3 Haiku", "Anthropic", "claude-3-haiku-20240307", "Chat", "Filtered", "", true, false, "https://api.anthropic.com/v1"));
        list.add(new ModelEntry("Gemini 2.5 Flash", "Google", "gemini-2.5-flash", "Chat", "Filtered", "", true, true, "https://generativelanguage.googleapis.com/v1beta/openai"));
        list.add(new ModelEntry("Gemini 2.0 Pro", "Google", "gemini-2.0-pro-exp", "Chat", "Filtered", "", true, false, "https://generativelanguage.googleapis.com/v1beta/openai"));
        list.add(new ModelEntry("Imagen 3", "Google", "imagen-3.0-generate-002", "Image", "Filtered", "", true, false, "https://generativelanguage.googleapis.com/v1beta"));
        return list;
    }

    public static List<ModelEntry> getByFilterType(String filterType) {
        List<ModelEntry> result = new ArrayList<>();
        for (ModelEntry e : getAll()) if (e.filterType.equals(filterType)) result.add(e);
        Collections.sort(result, (a, b) -> Boolean.compare(b.free, a.free));
        return result;
    }

    public static List<String> getAllCategories(String filterType) {
        Set<String> set = new LinkedHashSet<>();
        for (ModelEntry e : getAll()) if (e.filterType.equals(filterType)) set.add(e.category);
        return new ArrayList<>(set);
    }
}
MODEL_EOF

cat > app/src/main/java/com/ghostmax/ChatAdapter.java << 'CHATADAPTER_EOF'
package com.ghostmax;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    public static final int TYPE_SYSTEM = 2;

    public static class DisplayMessage {
        public int type;
        public String text;
        public String meta;
        public boolean isError;

        public DisplayMessage(int type, String text, String meta, boolean isError) {
            this.type = type; this.text = text; this.meta = meta; this.isError = isError;
        }
    }

    private final List<DisplayMessage> items = new ArrayList<>();

    public void add(DisplayMessage m) {
        items.add(m);
        notifyItemInserted(items.size() - 1);
    }

    public void removeLast() {
        if (!items.isEmpty()) {
            int idx = items.size() - 1;
            items.remove(idx);
            notifyItemRemoved(idx);
        }
    }

    public DisplayMessage last() {
        return items.isEmpty() ? null : items.get(items.size() - 1);
    }

    public void clear() {
        int n = items.size();
        items.clear();
        notifyItemRangeRemoved(0, n);
    }

    public int size() { return items.size(); }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        if (viewType == TYPE_USER) layoutRes = R.layout.item_message_user;
        else if (viewType == TYPE_BOT) layoutRes = R.layout.item_message_bot;
        else layoutRes = R.layout.item_message_system;
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayMessage m = items.get(position);
        MessageViewHolder h = (MessageViewHolder) holder;
        h.tvText.setText(m.text);
        if (h.tvMeta != null) {
            if (m.meta != null && !m.meta.isEmpty()) {
                h.tvMeta.setVisibility(View.VISIBLE);
                h.tvMeta.setText(m.meta);
            } else {
                h.tvMeta.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        TextView tvMeta;
        MessageViewHolder(View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            tvMeta = itemView.findViewById(R.id.tvMeta);
        }
    }
}
CHATADAPTER_EOF

echo "🔄 [4/6] Schreibe GitHub-Actions-Workflow (fuer Cloud-Builds bei jedem Push)..."
cat > .github/workflows/build.yml << 'WORKFLOW_EOF'
name: Build APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3
        with:
          packages: >-
            platform-tools
            platforms;android-34
            build-tools;34.0.0
            cmdline-tools;latest

      - name: Make gradlew executable
        run: chmod +x ./gradlew

      - name: Build debug APK
        run: ./gradlew assembleDebug --stacktrace

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: GhostMax-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
WORKFLOW_EOF

cat > gradle_patch.py << 'PYPATCH_EOF'
import re, sys

path = "app/build.gradle"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

additions = []
if "com.google.android.material:material" not in content:
    additions.append("    implementation 'com.google.android.material:material:1.11.0'")
if "androidx.recyclerview:recyclerview" not in content:
    additions.append("    implementation 'androidx.recyclerview:recyclerview:1.3.2'")
if "androidx.security:security-crypto" not in content:
    # Hinweis: falls Gradle diese Version nicht aufloesen kann (kein Internet-Zugriff zum Pruefen
    # der aktuell neuesten Version), bitte manuell auf eine aktuellere security-crypto-Version anheben.
    additions.append("    implementation 'androidx.security:security-crypto:1.1.0-alpha06'")

if additions:
    m = re.search(r"dependencies\s*\{", content)
    if not m:
        print("WARN: dependencies-Block nicht gefunden, bitte manuell ergaenzen:")
        for a in additions: print(a)
        sys.exit(0)
    insert_pos = m.end()
    new_content = content[:insert_pos] + "\n" + "\n".join(additions) + content[insert_pos:]
    with open(path, "w", encoding="utf-8") as f:
        f.write(new_content)
    print("Ergaenzt:")
    for a in additions: print(" " + a.strip())
else:
    print("Alle Abhaengigkeiten bereits vorhanden, nichts zu tun.")

# Versionscode/-name hochzaehlen - nur wenn die erwarteten Zeilen wirklich existieren,
# sonst NICHT blind sed-en (das wuerde die Datei sonst unbemerkt verstuemmeln)
vcode_match = re.search(r"versionCode\s+(\d+)", content_or_new := (new_content if additions else content))
vname_match = re.search(r'versionName\s+"([^"]*)"', content_or_new)
if vcode_match and vname_match:
    old_code = int(vcode_match.group(1))
    new_code = old_code + 1
    content_or_new = content_or_new[:vcode_match.start()] + f"versionCode {new_code}" + content_or_new[vcode_match.end():]
    # versionName-Position neu suchen, da sich durch die vorherige Ersetzung Indizes verschoben haben koennten
    vname_match2 = re.search(r'versionName\s+"([^"]*)"', content_or_new)
    content_or_new = content_or_new[:vname_match2.start()] + f'versionName "{new_code}"' + content_or_new[vname_match2.end():]
    with open(path, "w", encoding="utf-8") as f:
        f.write(content_or_new)
    print(f"Version hochgezaehlt: {old_code} -> {new_code}")
    with open("version_out.txt", "w") as vf:
        vf.write(str(new_code))
else:
    print("WARN: versionCode/versionName nicht in app/build.gradle gefunden - Versionszaehler uebersprungen (kein Rateversuch, um die Datei nicht zu beschaedigen).")
    with open("version_out.txt", "w") as vf:
        vf.write("unversioned")
PYPATCH_EOF

echo "🔄 [5/6] Pruefe/ergaenze Gradle-Abhaengigkeiten und Versionszaehler..."
python3 gradle_patch.py
NEW_VERSION=$(cat version_out.txt 2>/dev/null || echo "unversioned")
rm -f gradle_patch.py version_out.txt

echo "🔎 Selbst-Check: xmlns:app in allen Bubble-Layouts vorhanden?"
for f in app/src/main/res/layout/item_message_user.xml app/src/main/res/layout/item_message_bot.xml app/src/main/res/layout/item_message_system.xml; do
    if ! grep -q 'xmlns:app' "$f"; then
        echo "❌ xmlns:app fehlt in $f - Abbruch."
        exit 1
    fi
done
echo "✅ Layouts ok."

echo "🔎 Selbst-Check: JSONException in ApiClient.java korrekt deklariert?"
if ! grep -q "throws IOException, JSONException" app/src/main/java/com/ghostmax/ApiClient.java; then
    echo "❌ JSONException-Deklaration fehlt - Abbruch."
    exit 1
fi
echo "✅ ApiClient.java ok."

echo "🔎 Selbst-Check: Imagen-3 nutzt :predict (nicht :generateContent)?"
if ! grep -q ":predict" app/src/main/java/com/ghostmax/ApiClient.java; then
    echo "❌ Imagen-Endpunkt-Fix fehlt - Abbruch."
    exit 1
fi
echo "✅ Imagen-Endpunkt ok."

echo "🔎 Selbst-Check: IOException in CryptoHelper.java korrekt deklariert?"
if ! grep -q "throws GeneralSecurityException, IOException" app/src/main/java/com/ghostmax/CryptoHelper.java; then
    echo "❌ IOException-Deklaration fehlt - Abbruch."
    exit 1
fi
echo "✅ CryptoHelper.java ok."

echo "🚀 [6/6] Starte lokalen Build in Termux (RAM-schonend)..."
./gradlew clean assembleDebug --no-daemon --max-workers=1

APK_SRC="app/build/outputs/apk/debug/app-debug.apk"
APK_DEST="/storage/emulated/0/Download/GhostMax_v${NEW_VERSION}.apk"
if [ -f "$APK_SRC" ]; then
    cp "$APK_SRC" "$APK_DEST"
    echo "✅ APK kopiert nach $APK_DEST"
else
    echo "❌ Build fehlgeschlagen oder APK nicht gefunden."
    exit 1
fi

echo "🔄 Git: Aenderungen committen und pushen..."
if [ -d ".git" ]; then
    git add -A
    git commit -m "Finales Update v${NEW_VERSION}: Redesign, Geheim-Modus, Bildgenerierung, alle Bugfixes" || echo "ℹ️ Nichts zu committen."
    git push || echo "⚠️ git push fehlgeschlagen - bitte Remote/Branch pruefen."
else
    echo "ℹ️ Kein .git-Ordner gefunden - Git-Schritt uebersprungen."
fi

echo "============================================================"
echo "✅ Fertig. APK: $APK_DEST"
echo "✅ GitHub-Workflow: .github/workflows/build.yml (baut bei jedem 'git push' automatisch mit)"
echo "============================================================"
