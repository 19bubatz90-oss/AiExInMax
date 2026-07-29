#!/usr/bin/env bash
# setup_github_repo.sh – erstellt die komplette GhostMax-Struktur

PROJECT_DIR="$HOME/GhostMax"
JAVA_DIR="$PROJECT_DIR/app/src/main/java/com/ghostmax"
RES_DIR="$PROJECT_DIR/app/src/main/res"

mkdir -p "$JAVA_DIR" "$RES_DIR/values" "$RES_DIR/mipmap-hdpi"

# ─── README.md ───
cat > "$PROJECT_DIR/README.md" << 'EOF'
# GhostMax – Maximum All-in-One AI Chat App

**Vollbild, scrollbare Buttons, alle Provider, eigener Persönlichkeits-Manager, lokale Inferenz**

## Features
- 🧠 **8 eingebaute Provider** (OpenRouter, Groq, Gemini, OpenAI, Anthropic, DeepSeek, Google, LocalLLM)
- 📚 **Modellkatalog** mit No‑Filter und Filtered‑Modellen, kategorisiert (Chat, Coding, Creative, Science, …)
- ✨ **Eigene Persönlichkeiten** speichern und abrufen (Name, Prompt, Temperatur, Provider)
- 🖥️ **Lokaler Server** per Button starten/stoppen (benötigt `llama.cpp` in Termux)
- ⚙️ **Volle Einstellungen** (API‑Keys, URLs, Modelle, Temperatur, Tokens, Dark Mode, Auto‑Scroll, …)
- 📱 **Optimiert für Samsung A13** (1080×2408, 20:9)

## Voraussetzungen
- Termux mit Android SDK (`pkg install gradle android-sdk`)
- Für lokale Inferenz: `llama.cpp` und ein Modell (siehe `llama_control.sh`)

## Installation
```bash
chmod +x install.sh
./install.sh
