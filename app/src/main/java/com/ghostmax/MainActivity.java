package com.ghostmax;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.*;

public class MainActivity extends AppCompatActivity {
    private Prefs prefs;
    private EditText inputText;
    private Button sendButton, stopButton, copyButton, imageButton, serverButton;
    private Spinner providerSpinner, personalitySpinner;
    private LinearLayout chatContainer, quickRepliesContainer;
    private ScrollView scrollView;
    private String currentProvider = "LocalLLM";
    private String currentPersonality = "Freundlicher Assistent";
    private List<ChatMessage> history = new ArrayList<>();
    private AtomicBoolean isProcessing = new AtomicBoolean(false);
    private Thread currentThread;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private OkHttpClient httpClient = new OkHttpClient();
    private static final String CONTROL_URL = "http://127.0.0.1:8888";
    private List<String> allProvs;
    private List<PersonalityManager.Personality> allPers;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new Prefs(this);
        if (prefs.getDarkMode()) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        currentPersonality = prefs.getPersonality();
        PersonalityManager.Personality p = PersonalityManager.findByName(currentPersonality, prefs);
        prefs.setSystemPrompt(p.systemPrompt);
        currentProvider = p.provider;
        prefs.setTemperature(p.temperature);
        prefs.setMaxTokens(p.maxTokens);

        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(0,0,0,0);

        HorizontalScrollView topScroll = new HorizontalScrollView(this);
        topScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout topBar = new LinearLayout(this); topBar.setOrientation(LinearLayout.HORIZONTAL); topBar.setPadding(5,5,5,5);

        providerSpinner = new Spinner(this);
        allProvs = ApiClient.getAllProviderNames(prefs);
        refreshProviderList();
        providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos >= 0 && pos < allProvs.size()) currentProvider = allProvs.get(pos);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        topBar.addView(providerSpinner);

        personalitySpinner = new Spinner(this);
        loadPersonalities();
        personalitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos >= 0 && pos < allPers.size()) {
                    PersonalityManager.Personality sel = allPers.get(pos);
                    currentPersonality = sel.name; prefs.setPersonality(sel.name);
                    prefs.setSystemPrompt(sel.systemPrompt); prefs.setTemperature(sel.temperature); prefs.setMaxTokens(sel.maxTokens);
                    currentProvider = sel.provider;
                    int idx = allProvs.indexOf(currentProvider);
                    if (idx >= 0) providerSpinner.setSelection(idx);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        topBar.addView(personalitySpinner);

        Button settingsBtn = new Button(this); settingsBtn.setText("⚙️"); settingsBtn.setOnClickListener(v -> showSettingsDialog());
        topBar.addView(settingsBtn);
        Button addProvBtn = new Button(this); addProvBtn.setText("➕"); addProvBtn.setOnClickListener(v -> { ProviderManagerDialog.show(this, prefs, () -> refreshProviderList()); });
        topBar.addView(addProvBtn);
        Button catalogBtn = new Button(this); catalogBtn.setText("📚"); catalogBtn.setOnClickListener(v -> showModelCatalog());
        topBar.addView(catalogBtn);
        imageButton = new Button(this); imageButton.setText("🖼️"); imageButton.setOnClickListener(v -> showImageDialog());
        topBar.addView(imageButton);
        serverButton = new Button(this); serverButton.setText("🖥️ Server");
        serverButton.setOnClickListener(v -> toggleLocalServer());
        topBar.addView(serverButton);
        Button persAddBtn = new Button(this); persAddBtn.setText("🧠+"); persAddBtn.setOnClickListener(v -> showAddPersonalityDialog());
        topBar.addView(persAddBtn);

        topScroll.addView(topBar);
        root.addView(topScroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        quickRepliesContainer = new LinearLayout(this); quickRepliesContainer.setOrientation(LinearLayout.HORIZONTAL); quickRepliesContainer.setPadding(5,5,5,5);
        if (prefs.getQuickReplies()) {
            String[] qr = {"👋 Hallo","❓ Was ist das?","💡 Erkläre es mir","🤔 Denke nach","😊 Danke"};
            for (String s : qr) {
                Button b = new Button(this); b.setText(s); b.setTextSize(12);
                b.setOnClickListener(v -> { inputText.setText(s); sendMessage(); });
                quickRepliesContainer.addView(b);
            }
        }
        root.addView(quickRepliesContainer);

        scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFFF5F5F5);
        chatContainer = new LinearLayout(this); chatContainer.setOrientation(LinearLayout.VERTICAL); chatContainer.setPadding(10,10,10,10);
        scrollView.addView(chatContainer);
        root.addView(scrollView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));

        LinearLayout inputRow = new LinearLayout(this); inputRow.setOrientation(LinearLayout.HORIZONTAL); inputRow.setPadding(5,5,5,5); inputRow.setBackgroundColor(0xFFFFFFFF);
        inputText = new EditText(this); inputText.setHint("Nachricht an "+currentProvider+" ...");
        inputText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        inputText.setOnKeyListener((v,keyCode,event) -> { if (keyCode==KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN) { sendMessage(); return true; } return false; });
        sendButton = new Button(this); sendButton.setText("Senden"); sendButton.setOnClickListener(v -> sendMessage());
        stopButton = new Button(this); stopButton.setText("⏹"); stopButton.setEnabled(false); stopButton.setOnClickListener(v -> stopProcessing());
        copyButton = new Button(this); copyButton.setText("📋"); copyButton.setOnClickListener(v -> copyLastMessage());
        inputRow.addView(inputText); inputRow.addView(sendButton); inputRow.addView(stopButton);
        if (prefs.getCopyButton()) inputRow.addView(copyButton);
        root.addView(inputRow);

        setContentView(root);
        loadHistory();
        addSystemMessage("GhostMax – Server-Steuerung, eigene Persönlichkeiten");
        updateServerButtonState();
    }

    private void refreshProviderList() {
        allProvs = ApiClient.getAllProviderNames(prefs);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, allProvs);
        providerSpinner.setAdapter(adapter);
        int idx = allProvs.indexOf(currentProvider);
        if (idx >= 0) providerSpinner.setSelection(idx);
    }

    private void loadPersonalities() {
        allPers = new ArrayList<>();
        allPers.addAll(PersonalityManager.getDefaultPersonalities());
        allPers.addAll(PersonalityManager.loadCustom(prefs));
        List<String> persNames = new ArrayList<>();
        for (PersonalityManager.Personality per : allPers) persNames.add(per.icon+" "+per.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, persNames);
        personalitySpinner.setAdapter(adapter);
        int idx = -1;
        for (int i=0; i<allPers.size(); i++) if (allPers.get(i).name.equals(currentPersonality)) { idx = i; break; }
        if (idx >= 0) personalitySpinner.setSelection(idx);
    }

    private void showAddPersonalityDialog() {
        LinearLayout lay = new LinearLayout(this); lay.setOrientation(LinearLayout.VERTICAL); lay.setPadding(30,30,30,30);
        EditText nameInput = new EditText(this); nameInput.setHint("Name"); lay.addView(nameInput);
        EditText descInput = new EditText(this); descInput.setHint("Beschreibung"); lay.addView(descInput);
        EditText promptInput = new EditText(this); promptInput.setHint("System-Prompt"); lay.addView(promptInput);
        Spinner provSpinner = new Spinner(this);
        provSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, allProvs));
        lay.addView(provSpinner);
        EditText tempInput = new EditText(this); tempInput.setHint("Temperature (0.0-2.0)"); lay.addView(tempInput);
        EditText tokInput = new EditText(this); tokInput.setHint("Max Tokens"); lay.addView(tokInput);
        EditText iconInput = new EditText(this); iconInput.setHint("Icon (Emoji)"); lay.addView(iconInput);
        new AlertDialog.Builder(this)
            .setTitle("Neue Persönlichkeit")
            .setView(lay)
            .setPositiveButton("Speichern", (d,w) -> {
                String name = nameInput.getText().toString().trim();
                String desc = descInput.getText().toString().trim();
                String prompt = promptInput.getText().toString().trim();
                String provider = allProvs.get(provSpinner.getSelectedItemPosition());
                double temp = 0.7; try { temp = Double.parseDouble(tempInput.getText().toString()); } catch (Exception ignored) {}
                int tokens = 1024; try { tokens = Integer.parseInt(tokInput.getText().toString()); } catch (Exception ignored) {}
                String icon = iconInput.getText().toString().trim(); if (icon.isEmpty()) icon = "🧠";
                if (name.isEmpty() || prompt.isEmpty()) { Toast.makeText(this, "Name und Prompt benötigt", Toast.LENGTH_SHORT).show(); return; }
                PersonalityManager.Personality newP = new PersonalityManager.Personality(name, desc, prompt, provider, temp, tokens, icon);
                List<PersonalityManager.Personality> customs = PersonalityManager.loadCustom(prefs);
                customs.add(newP);
                PersonalityManager.saveCustom(customs, prefs);
                Toast.makeText(this, "Persönlichkeit gespeichert!", Toast.LENGTH_SHORT).show();
                loadPersonalities();
            }).setNegativeButton("Abbrechen", null).show();
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
                runOnUiThread(() -> updateServerButtonState());
            } catch (Exception e) {
                runOnUiThread(() -> addSystemMessage("❌ Steuerdienst nicht erreichbar. Läuft llama_control.sh?"));
            }
        }).start();
    }

    private void updateServerButtonState() {
        new Thread(() -> {
            try {
                Request req = new Request.Builder().url(CONTROL_URL + "/status").get().build();
                String status = httpClient.newCall(req).execute().body().string().trim();
                runOnUiThread(() -> {
                    if ("RUNNING".equals(status)) serverButton.setText("🖥️ Stop");
                    else serverButton.setText("🖥️ Start");
                });
            } catch (Exception e) {
                runOnUiThread(() -> serverButton.setText("🖥️ ?"));
            }
        }).start();
    }

    private void showSettingsDialog() {
        ScrollView sc = new ScrollView(this);
        LinearLayout lay = new LinearLayout(this); lay.setOrientation(LinearLayout.VERTICAL); lay.setPadding(30,30,30,30);
        for (String prov : ApiClient.ALL_PROVIDERS) {
            TextView tv = new TextView(this); tv.setText("🔹 "+prov); tv.setTextSize(16); tv.setPadding(0,20,0,5); lay.addView(tv);
            EditText keyInput = new EditText(this); keyInput.setHint("API-Key"); keyInput.setText(prefs.getApiKey(prov));
            keyInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            lay.addView(keyInput); keyInput.setTag(prov+"_key");
            EditText urlInput = new EditText(this); urlInput.setHint("URL"); urlInput.setText(prefs.getProviderUrl(prov).isEmpty() ? getDefaultUrl(prov) : prefs.getProviderUrl(prov));
            lay.addView(urlInput); urlInput.setTag(prov+"_url");
            EditText modelInput = new EditText(this); modelInput.setHint("Modell"); modelInput.setText(prefs.getProviderModel(prov).isEmpty() ? getDefaultModel(prov) : prefs.getProviderModel(prov));
            lay.addView(modelInput); modelInput.setTag(prov+"_model");
        }
        TextView tempLabel = new TextView(this); tempLabel.setText("🌡️ Temperatur: "+prefs.getTemperature()); lay.addView(tempLabel);
        SeekBar tempBar = new SeekBar(this); tempBar.setMax(200); tempBar.setProgress((int)(prefs.getTemperature()*100));
        tempBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) { tempLabel.setText("🌡️ Temperatur: "+String.format("%.2f",p/100.0)); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        }); lay.addView(tempBar);
        TextView tokLabel = new TextView(this); tokLabel.setText("📝 Tokens: "+prefs.getMaxTokens()); lay.addView(tokLabel);
        SeekBar tokBar = new SeekBar(this); tokBar.setMax(4000); tokBar.setProgress(prefs.getMaxTokens());
        tokBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) { tokLabel.setText("📝 Tokens: "+p); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        }); lay.addView(tokBar);
        CheckBox fallbackCheck = new CheckBox(this); fallbackCheck.setText("🔄 Fallback"); fallbackCheck.setChecked(prefs.getFallbackEnabled()); lay.addView(fallbackCheck);
        CheckBox darkCheck = new CheckBox(this); darkCheck.setText("🌙 Dark Mode"); darkCheck.setChecked(prefs.getDarkMode()); lay.addView(darkCheck);
        CheckBox showProvCheck = new CheckBox(this); showProvCheck.setText("🏷️ Provider anzeigen"); showProvCheck.setChecked(prefs.getShowProvider()); lay.addView(showProvCheck);
        CheckBox autoScrollCheck = new CheckBox(this); autoScrollCheck.setText("📜 Auto-Scroll"); autoScrollCheck.setChecked(prefs.getAutoScroll()); lay.addView(autoScrollCheck);
        CheckBox animCheck = new CheckBox(this); animCheck.setText("🎬 Animationen"); animCheck.setChecked(prefs.getAnimations()); lay.addView(animCheck);
        CheckBox timeCheck = new CheckBox(this); timeCheck.setText("🕒 Timestamps"); timeCheck.setChecked(prefs.getShowTimestamps()); lay.addView(timeCheck);
        CheckBox latCheck = new CheckBox(this); latCheck.setText("⚡ Latenz"); latCheck.setChecked(prefs.getShowLatency()); lay.addView(latCheck);
        CheckBox notiCheck = new CheckBox(this); notiCheck.setText("🔔 Benachrichtigungen"); notiCheck.setChecked(prefs.getNotifications()); lay.addView(notiCheck);
        CheckBox quickCheck = new CheckBox(this); quickCheck.setText("⚡ Quick Replies"); quickCheck.setChecked(prefs.getQuickReplies()); lay.addView(quickCheck);
        sc.addView(lay);
        new AlertDialog.Builder(this).setTitle("⚙️ Einstellungen").setView(sc)
            .setPositiveButton("Speichern", (d,w) -> {
                for (String prov : ApiClient.ALL_PROVIDERS) {
                    EditText k = lay.findViewWithTag(prov+"_key"); if (k != null) prefs.setApiKey(prov, k.getText().toString().trim());
                    EditText u = lay.findViewWithTag(prov+"_url"); if (u != null) prefs.setProviderUrl(prov, u.getText().toString().trim());
                    EditText m = lay.findViewWithTag(prov+"_model"); if (m != null) prefs.setProviderModel(prov, m.getText().toString().trim());
                }
                prefs.setTemperature(tempBar.getProgress()/100.0);
                prefs.setMaxTokens(tokBar.getProgress());
                prefs.setFallbackEnabled(fallbackCheck.isChecked());
                prefs.setDarkMode(darkCheck.isChecked());
                prefs.setShowProvider(showProvCheck.isChecked());
                prefs.setAutoScroll(autoScrollCheck.isChecked());
                prefs.setAnimations(animCheck.isChecked());
                prefs.setShowTimestamps(timeCheck.isChecked());
                prefs.setShowLatency(latCheck.isChecked());
                prefs.setNotifications(notiCheck.isChecked());
                prefs.setQuickReplies(quickCheck.isChecked());
                if (darkCheck.isChecked()) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(this, "Gespeichert", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Abbrechen", null).show();
    }

    private String getDefaultUrl(String prov) {
        switch (prov) {
            case "LocalLLM": return "http://127.0.0.1:8080/completion";
            case "OpenRouter": return "https://openrouter.ai/api/v1/chat/completions";
            case "Groq": return "https://api.groq.com/openai/v1/chat/completions";
            case "Gemini": case "Google": return "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
            case "OpenAI": return "https://api.openai.com/v1/chat/completions";
            case "Anthropic": return "https://api.anthropic.com/v1/chat/completions";
            case "DeepSeek API": return "https://api.deepseek.com/v1/chat/completions";
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
            default: return "";
        }
    }

    private void sendMessage() {
        if (isProcessing.get()) return;
        String userText = inputText.getText().toString().trim();
        if (userText.isEmpty()) return;
        inputText.setText("");
        addMessage(userText, true);
        history.add(new ChatMessage(ChatMessage.TYPE_USER, userText, false));
        addSystemMessage("⏳ Antwort wird geladen...");
        isProcessing.set(true); sendButton.setEnabled(false); stopButton.setEnabled(true);
        currentThread = new Thread(() -> {
            try {
                ApiClient.Result result = ApiClient.callWithFallback(currentProvider, prefs, history, userText);
                runOnUiThread(() -> {
                    if (!isProcessing.get()) return;
                    removeLastSystemMessage();
                    if (result.isError) {
                        addMessage("❌ Fehler: "+result.text, false);
                        history.add(new ChatMessage(ChatMessage.TYPE_ASSISTANT, "Fehler: "+result.text, true));
                    } else {
                        String display = result.text;
                        if (prefs.getShowProvider()) display = "["+result.actualProvider+" | "+result.latency+"ms] "+display;
                        if (prefs.getShowTimestamps()) display = timeFormat.format(new Date())+" "+display;
                        addMessage(display, false);
                        history.add(new ChatMessage(ChatMessage.TYPE_ASSISTANT, result.text, false));
                    }
                    isProcessing.set(false); sendButton.setEnabled(true); stopButton.setEnabled(false);
                    saveHistory();
                    if (prefs.getAutoScroll()) scrollToBottom();
                    if (prefs.getNotifications()) Toast.makeText(MainActivity.this, "Neue Antwort von "+result.actualProvider, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    removeLastSystemMessage();
                    addMessage("❌ Fehler: "+e.getMessage(), false);
                    isProcessing.set(false); sendButton.setEnabled(true); stopButton.setEnabled(false);
                });
            }
        });
        currentThread.start();
    }

    private void stopProcessing() {
        isProcessing.set(false);
        if (currentThread != null) currentThread.interrupt();
        stopButton.setEnabled(false); sendButton.setEnabled(true);
        removeLastSystemMessage();
        addSystemMessage("⏹ Anfrage abgebrochen.");
    }

    private void copyLastMessage() {
        int count = chatContainer.getChildCount();
        if (count>0) {
            View last = chatContainer.getChildAt(count-1);
            if (last instanceof TextView) {
                String text = ((TextView)last).getText().toString();
                if (text.startsWith("🤖")) {
                    String content = text.substring(2).trim();
                    android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("Copied Text", content));
                    Toast.makeText(this, "📋 Kopiert!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void addMessage(String text, boolean isUser) {
        TextView tv = new TextView(this);
        tv.setText((isUser?"👤 ":"🤖 ")+text);
        tv.setPadding(18, 12, 18, 12);
        tv.setTextSize(prefs.getFontSize());
        int bgColor = isUser ? 0xFF2196F3 : 0xFF4CAF50;
        tv.setTextColor(0xFFFFFFFF);
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setCornerRadius(18);
        shape.setColor(bgColor);
        tv.setBackground(shape);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 6, 8, 6);
        tv.setLayoutParams(params);
        if (prefs.getAnimations()) {
            android.view.animation.AlphaAnimation anim = new android.view.animation.AlphaAnimation(0.0f,1.0f);
            anim.setDuration(300); tv.startAnimation(anim);
        }
        chatContainer.addView(tv);
        if (prefs.getAutoScroll()) scrollToBottom();
    }

    private void addSystemMessage(String text) {
        TextView tv = new TextView(this);
        tv.setText("⚙️ "+text);
        tv.setPadding(12, 6, 12, 6);
        tv.setTextSize(12);
        tv.setTextColor(0xFF666666);
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setCornerRadius(10);
        shape.setColor(0xAAEEEEEE);
        tv.setBackground(shape);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 4, 8, 4);
        tv.setLayoutParams(params);
        chatContainer.addView(tv);
        if (prefs.getAutoScroll()) scrollToBottom();
    }

    private void removeLastSystemMessage() {
        int count = chatContainer.getChildCount();
        if (count>0) {
            View last = chatContainer.getChildAt(count-1);
            if (last instanceof TextView && ((TextView)last).getText().toString().startsWith("⚙️") && ((TextView)last).getText().toString().contains("wird geladen"))
                chatContainer.removeViewAt(count-1);
        }
    }

    private void scrollToBottom() { scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN)); }

    private void loadHistory() {
        try {
            String json = prefs.getHistory();
            if (json != null && !json.isEmpty() && !"[]".equals(json)) {
                org.json.JSONArray arr = new org.json.JSONArray(json);
                for (int i=0; i<arr.length(); i++) {
                    org.json.JSONObject obj = arr.getJSONObject(i);
                    int type = obj.getInt("type"); String text = obj.getString("text"); boolean err = obj.optBoolean("isError",false);
                    history.add(new ChatMessage(type,text,err));
                    if (type==ChatMessage.TYPE_USER) addMessage(text,true);
                    else if (type==ChatMessage.TYPE_ASSISTANT) addMessage(text,false);
                }
            }
        } catch (Exception ignored) {}
    }

    private void saveHistory() {
        try {
            org.json.JSONArray arr = new org.json.JSONArray();
            for (ChatMessage m : history) {
                org.json.JSONObject o = new org.json.JSONObject();
                o.put("type",m.type); o.put("text",m.text); o.put("isError",m.isError); arr.put(o);
            }
            prefs.saveHistory(arr.toString());
        } catch (Exception ignored) {}
    }

    private void showModelCatalog() {
        final String[] mainCategories = {"No Filter", "Filtered"};
        new AlertDialog.Builder(this)
            .setTitle("📚 KI-Katalog")
            .setItems(mainCategories, (d, which) -> {
                String filterType = mainCategories[which];
                final List<ModelCatalog.ModelEntry> entries = ModelCatalog.getByFilterType(filterType);
                if (entries.isEmpty()) { Toast.makeText(this, "Keine Modelle in dieser Kategorie.", Toast.LENGTH_SHORT).show(); return; }

                final List<String> itemLabels = new ArrayList<>();
                final List<ModelCatalog.ModelEntry> orderedEntries = new ArrayList<>();
                for (String cat : ModelCatalog.getAllCategories(filterType)) {
                    for (ModelCatalog.ModelEntry e : entries) {
                        if (e.category.equals(cat)) {
                            itemLabels.add(cat + " | " + e.name + " (" + e.provider + ")");
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
                            Toast.makeText(this, "🔑 Key für "+selected.provider+" fehlt – bitte in Einstellungen hinterlegen.", Toast.LENGTH_LONG).show();
                            showSettingsDialog();
                            return;
                        }
                        String name = selected.name + " (" + selected.provider + ")";
                        prefs.addCustomProvider(new CustomProvider(name, selected.apiBase, key, selected.modelId, selected.category.toLowerCase()));
                        Toast.makeText(this, "✅ "+selected.name+" als Provider hinzugefügt!", Toast.LENGTH_LONG).show();
                        refreshProviderList();
                    })
                    .setNegativeButton("Zurück", null)
                    .show();
            })
            .setNegativeButton("Schließen", null)
            .show();
    }

    private void showImageDialog() {
        Toast.makeText(this, "Bildgenerierung nicht verfügbar", Toast.LENGTH_SHORT).show();
    }
}
