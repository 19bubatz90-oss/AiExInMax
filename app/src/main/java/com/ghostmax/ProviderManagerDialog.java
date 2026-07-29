package com.ghostmax;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
public class ProviderManagerDialog {
    public interface OnProviderSaved { void onSaved(); }
    public static void show(Context ctx, Prefs prefs, OnProviderSaved callback) {
        LinearLayout root = new LinearLayout(ctx); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(30,30,30,30);
        EditText nameInput = new EditText(ctx); nameInput.setHint("Name"); root.addView(nameInput);
        EditText urlInput = new EditText(ctx); urlInput.setHint("Basis-URL"); root.addView(urlInput);
        EditText keyInput = new EditText(ctx); keyInput.setHint("API-Key"); root.addView(keyInput);
        EditText modelInput = new EditText(ctx); modelInput.setHint("Modell"); root.addView(modelInput);
        Spinner catSpinner = new Spinner(ctx);
        catSpinner.setAdapter(new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, new String[]{"Chat","Coding","Bild"}));
        root.addView(catSpinner);
        new AlertDialog.Builder(ctx)
                .setTitle("Provider hinzufügen")
                .setView(root)
                .setPositiveButton("Hinzufügen", (d,w) -> {
                    String name = nameInput.getText().toString().trim();
                    String url = urlInput.getText().toString().trim();
                    String key = keyInput.getText().toString().trim();
                    String model = modelInput.getText().toString().trim();
                    String[] cats = {"chat","coding","image"};
                    String category = cats[catSpinner.getSelectedItemPosition()];
                    if (name.isEmpty() || url.isEmpty() || model.isEmpty()) {
                        Toast.makeText(ctx, "Name, URL und Modell benötigt.", Toast.LENGTH_SHORT).show(); return;
                    }
                    prefs.addCustomProvider(new CustomProvider(name, url, key, model, category));
                    Toast.makeText(ctx, "Provider \""+name+"\" hinzugefügt.", Toast.LENGTH_SHORT).show();
                    if (callback != null) callback.onSaved();
                }).setNegativeButton("Abbrechen",null).show();
    }
}
