package com.ghostmax;
import org.json.JSONException;
import org.json.JSONObject;
public class CustomProvider {
    public static final String CATEGORY_CHAT = "chat", CATEGORY_CODING = "coding", CATEGORY_IMAGE = "image";
    public String name, baseUrl, apiKey, model, category;
    public CustomProvider(String name, String baseUrl, String apiKey, String model, String category) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.category = category == null ? CATEGORY_CHAT : category;
    }
    public boolean isImageProvider() { return CATEGORY_IMAGE.equals(category); }
    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("name", name);
        o.put("baseUrl", baseUrl);
        o.put("apiKey", apiKey == null ? "" : apiKey);
        o.put("model", model);
        o.put("category", category);
        return o;
    }
    public static CustomProvider fromJson(JSONObject o) throws JSONException {
        return new CustomProvider(o.getString("name"), o.getString("baseUrl"),
                o.optString("apiKey", ""), o.getString("model"), o.optString("category", CATEGORY_CHAT));
    }
}
