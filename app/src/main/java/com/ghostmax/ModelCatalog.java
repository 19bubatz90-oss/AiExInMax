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
