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
