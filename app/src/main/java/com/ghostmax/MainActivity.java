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
