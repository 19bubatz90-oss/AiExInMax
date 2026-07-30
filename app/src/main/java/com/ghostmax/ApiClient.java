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
