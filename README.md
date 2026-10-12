# Anki Supporter

Anki Supporter is an Android application built with Jetpack Compose designed to assist English learners in generating flashcards and example sentences using AI. It supports both local Large Language Models via Ollama and cloud AI via Google Gemini.

---

## Features

- **AI-Powered Card Generation**:
  - Enter any English word, idiom, or Japanese text to automatically generate:
    - Target English word or phrase
    - IPA phonetic transcription (e.g., `/æp.əl/`)
    - Japanese translation
    - Natural English example sentence
    - Japanese translation of the example sentence
    - Nuance and usage explanation
    - **3 Japanese University Entrance Exam Level Synonyms (CEFR B1-B2)**: Curated synonyms rendered as interactive chips that trigger one-tap re-generation.
- **Dual AI Provider Support**:
  - **Local LLM (Ollama)**: Run models locally (e.g., `llama3.2`, `gemma2:9b`) via Ollama's OpenAI-compatible endpoint (`/v1/chat/completions`). No external API keys required.
  - **Cloud AI (Google Gemini)**: Use Google AI Studio models (e.g., `gemini-3.1-flash-lite`, `gemini-1.5-flash`) with your API key.
  - In-app connection testing for both providers.
- **Text-to-Speech (TTS)**:
  - Pronounce words and generated example sentences with standard US English TTS.
  - Configurable speech rate.
- **Local Vocabulary Storage & Search History**:
  - **My Vocabulary (マイ単語帳)**: Explicitly saved flashcards with memorized/unmemorized tracking.
  - **Search History (検索履歴 - Recent 100)**: Automatically records generated cards with automatic trimming of the oldest unsaved cards beyond 100 entries. Saved words are protected from trimming.
  - **One-Tap Promotion**: Easily promote any card from search history to My Vocabulary.
  - **Export (JSON)**: Export vocabulary data and trigger Android's share sheet (`Intent.ACTION_SEND` via `FileProvider`) to save to Google Drive, Gmail, or local storage.
  - **Import (JSON)**: Restore vocabulary data from JSON backup files using the system document picker (`ActivityResultContracts.OpenDocument`).
- **Polished Compose UI**:
  - Clean Material 3 design with loading indicators and provider indicators.
  - Automatic software keyboard dismissal and focus clearing upon generation to maximize screen visibility.

---

## Tech Stack

- **Platform**: Android (Kotlin)
- **UI Toolkit**: Jetpack Compose, Material 3
- **Architecture**: MVVM with Kotlin Coroutines & Flow
- **Local Database**: Room
- **Networking & Serialization**: OkHttp, Kotlinx Serialization
- **Speech**: Android `TextToSpeech` API

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17+
- Android device or emulator running Android 8.0 (API 26) or higher

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd anki-supporter
   ```

2. Open the project in Android Studio.

3. (Optional) Configure default Gemini API key:
   Add your Gemini API key to your `local.properties` file:
   ```properties
   gemini.api.key=YOUR_GEMINI_API_KEY
   ```

4. Build and run the application on your emulator or physical device.

---

## AI Backend Setup

### 1. Ollama (Local LLM)

To use Ollama from an Android emulator or device:

1. **Host Binding**: Ensure Ollama listens to network requests (by default it binds only to `127.0.0.1`):
   - Set the environment variable:
     ```bash
     OLLAMA_HOST=0.0.0.0:11434
     ```
   - Restart the Ollama service.

2. **Host URL in App Settings**:
   - **Android Emulator**: Set Host URL to `http://10.0.2.2:11434`
   - **Physical Device**: Set Host URL to `http://<YOUR_PC_LOCAL_IP>:11434` (e.g., `http://192.168.1.50:11434`)

3. **Model Selection**: Enter the model name you pulled (e.g., `llama3.2`, `gemma2:9b`).

### 2. Google Gemini

1. Obtain a free API key from [Google AI Studio](https://aistudio.google.com/).
2. Open **AI Settings** within the app and select **Google Gemini**.
3. Paste your API key and choose the model name (default: `gemini-3.1-flash-lite`).
4. Tap **Test Connection** to verify your configuration.

---

## Troubleshooting & Debugging

To monitor app network activity or troubleshoot Ollama / Gemini connection errors using `adb logcat`:

```bash
# Filter logs by app package
adb logcat -v time --package=com.example.ankisupporter

# Filter for errors and connection logs
adb logcat -v time | grep -iE "ankisupporter|ollama|okhttp|http|fatal|exception"
```

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.
