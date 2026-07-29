package com.ghostmax;
import java.util.*;
public class ModelCatalog {
    public static class ModelEntry {
        public final String name, provider, modelId, category, filterType, description, apiBase;
        public final boolean needsKey, free;
        public ModelEntry(String name, String provider, String modelId, String category,
                          String filterType, String description, boolean needsKey, boolean free, String apiBase) {
            this.name=name; this.provider=provider; this.modelId=modelId; this.category=category;
            this.filterType=filterType; this.description=description; this.needsKey=needsKey; this.free=free; this.apiBase=apiBase;
        }
    }
    public static List<ModelEntry> getAll() {
        List<ModelEntry> list = new ArrayList<>();
        list.add(new ModelEntry("GPT-OSS 120B","OpenRouter","openai/gpt-oss-120b:free","Chat","No Filter","Aktuell kostenlos, stark, allgemein",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Dolphin 2.9.1 Mixtral","OpenRouter","cognitivecomputations/dolphin-2.9.1-mixtral-8x7b","Chat","No Filter","Flexibles Chat-Modell",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Mistral 7B Instruct","OpenRouter","mistralai/mistral-7b-instruct:free","Chat","No Filter","Leicht, schnell",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Phi-3.5 Mini","OpenRouter","microsoft/phi-3.5-mini-128k-instruct:free","Chat","No Filter","Klein, leistungsfähig",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Hermes 2 Pro","OpenRouter","nousresearch/hermes-2-pro-llama-3-8b","Chat","No Filter","Kreativ, Dialoge",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Mythomax","OpenRouter","gryphe/mythomax-l2-13b","Chat","No Filter","Rollenspiele",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Qwen3 Coder","OpenRouter","qwen/qwen3-coder:free","Coding","No Filter","Aktuell kostenlos, starkes Coding-Modell, 1M Kontext",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("DeepSeek V3","DeepSeek API","deepseek-chat","Coding","No Filter","Extrem stark im Coding",true,false,"https://api.deepseek.com/v1"));
        list.add(new ModelEntry("DeepSeek Coder V2","DeepSeek API","deepseek-coder","Coding","No Filter","Spezialisiert auf Code",true,false,"https://api.deepseek.com/v1"));
        list.add(new ModelEntry("Hermes 2 Pro (Creative)","OpenRouter","nousresearch/hermes-2-pro-llama-3-8b","Creative","No Filter","Kreatives Schreiben",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Mythomax (Creative)","OpenRouter","gryphe/mythomax-l2-13b","Creative","No Filter","Kreative Texte",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Mythomax (Roleplay)","OpenRouter","gryphe/mythomax-l2-13b","Roleplay","No Filter","Optimiert für Rollenspiele",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("WizardLM 2 8x22B","OpenRouter","microsoft/wizardlm-2-8x22b","Reasoning","No Filter","Stark in Logik/Mathe",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Llama 3.3 70B (Science)","OpenRouter","meta-llama/llama-3.3-70b-instruct:free","Science","No Filter","Gute wissenschaftliche Erklärungen",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Dolphin 2.9.1 (Business)","OpenRouter","cognitivecomputations/dolphin-2.9.1-mixtral-8x7b","Business","No Filter","Flexibel für Business",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Dolphin 2.9.1 (Philosophy)","OpenRouter","cognitivecomputations/dolphin-2.9.1-mixtral-8x7b","Philosophy","No Filter","Tiefgründige Diskussionen",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("Llama 3.3 70B (Health)","OpenRouter","meta-llama/llama-3.3-70b-instruct:free","Health","No Filter","Medizin, Psychologie",true,true,"https://openrouter.ai/api/v1"));
        list.add(new ModelEntry("GPT-4o","OpenAI","gpt-4o","Chat","Filtered","OpenAI neuestes Modell",true,false,"https://api.openai.com/v1"));
        list.add(new ModelEntry("Claude 3.5 Sonnet","Anthropic","claude-3-5-sonnet-20241022","Chat","Filtered","Anthropic starkes Modell",true,false,"https://api.anthropic.com/v1"));
        list.add(new ModelEntry("Gemini 2.5 Flash","Google","gemini-2.5-flash","Chat","Filtered","Googles schnelles Modell",true,true,"https://generativelanguage.googleapis.com/v1beta/openai"));
        list.add(new ModelEntry("Claude 3.5 Sonnet (Coding)","Anthropic","claude-3-5-sonnet-20241022","Coding","Filtered","Sehr gut im Coding",true,false,"https://api.anthropic.com/v1"));
        list.add(new ModelEntry("GPT-4o (Coding)","OpenAI","gpt-4o","Coding","Filtered","OpenAI für Code",true,false,"https://api.openai.com/v1"));
        list.add(new ModelEntry("DALL-E 3","OpenAI","dall-e-3","Image","Filtered","OpenAI Bildgenerator",true,false,"https://api.openai.com/v1"));
        list.add(new ModelEntry("Imagen 3","Google","imagen-3.0-generate","Image","Filtered","Google Bildmodell",true,false,"https://generativelanguage.googleapis.com/v1beta"));
        list.add(new ModelEntry("Llama-3-8B Lexi Uncensored (lokal)","LocalLLM","","Chat","No Filter","Läuft direkt auf deinem Handy",false,true,"http://127.0.0.1:8080/completion"));
        return list;
    }
    public static List<ModelEntry> getByFilterType(String filterType) {
        List<ModelEntry> result = new ArrayList<>();
        for (ModelEntry e : getAll()) if (e.filterType.equals(filterType)) result.add(e);
        return result;
    }
    public static List<String> getAllCategories(String filterType) {
        Set<String> set = new LinkedHashSet<>();
        for (ModelEntry e : getAll()) if (e.filterType.equals(filterType)) set.add(e.category);
        return new ArrayList<>(set);
    }
}
