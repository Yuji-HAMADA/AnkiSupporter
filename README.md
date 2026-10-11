# AnkiSupporter (暗記サポーター)

Androidネイティブ（Kotlin + Jetpack Compose）で構築された、**完全無料・オープンソース指向**のAI英語学習アプリです。

## 主な特徴と要件対応

### 1. コストゼロ・基本無料の構成
- 有料APIや月額サブスクリプションは一切不要。
- 完全ローカルで動作するオープンソースLLM（Ollama）または Google Gemini API の無料枠（Free Tier）を利用。

### 2. デュアルAI連携（動的切り替え）
- **ローカルLLM (Ollama等)**:
  - 同一Wi-Fi上のPCで起動した Ollama の OpenAI互換API（`http://<IP>:11434/v1/chat/completions`）へ接続。
  - APIキー不要・プライベート・完全無料。
  - エミュレータ接続時は `http://10.0.2.2:11434`、実機接続時はPCのローカルIPを指定。
- **クラウド無料AI (Google Gemini API)**:
  - Google AI Studio（aistudio.google.com）で発行できる無料枠APIキーを使用。
  - 高速・高品質な例文生成に対応。
- **クリーンなアーキテクチャ**:
  - `AiClient` インターフェースおよび `AiRepository` により、設定画面からの切り替えをスムーズかつ疎結合に実現。

### 3. Android標準音声読み上げ（TextToSpeech）
- 外部の有料音声合成APIを使わず、Android OS標準の `TextToSpeech` API を利用。
- 単語および例文の英語発音（`Locale.US`）と和訳（`Locale.JAPANESE`）に対応。
- 読み上げ速度の調整機能を搭載。

### 4. ローカルDB（Room）の土台 & 単語帳・復習画面
- 生成した例文カードを「単語帳に保存」ボタン一つでローカルDB（Room）に保存。
- 暗記済みフラグのトグル、検索、一覧表示、削除機能を備えた「単語帳」画面を完備。

---

## 画面構成
1. **例文生成（ホーム画面）**:
   - 単語・フレーズの入力
   - AIによる例文・発音記号・和訳・ニュアンス解説の自動生成
   - 英語音声読み上げボタン
   - 単語帳へのワンタップ保存
2. **単語帳（復習画面）**:
   - 保存済み単語カードの一覧・検索
   - 未暗記 / 暗記済みのフィルタリング
   - 音声読み上げと復習チェック
3. **設定画面**:
   - AI接続先の選択（Ollama / Gemini）
   - OllamaのホストURL / モデル名設定 & 接続テスト
   - Gemini APIキー / モデル名設定 & 接続テスト
   - TTS読み上げ速度調整

---

## ディレクトリ構成
```
app/src/main/java/com/example/ankisupporter/
├── AnkiSupporterApp.kt          # Application クラス (コンテナ・依存解決)
├── MainActivity.kt              # メインエントリーポイント
├── model/                       # データモデル (AiProvider, GeneratedCard, AppSettings)
├── data/
│   ├── ai/
│   │   ├── AiClient.kt          # AIクライアントの抽象インターフェース
│   │   ├── OllamaAiClient.kt    # ローカルLLM (Ollama OpenAI互換) 実装
│   │   ├── GeminiAiClient.kt    # Google Gemini API (無料枠) 実装
│   │   ├── AiRepository.kt      # プロバイダー切り替えリポジトリ
│   │   ├── AiPromptHelper.kt    # プロンプト生成 & JSONパースヘルパー
│   │   └── dto/                 # JSON DTO (Kotlinx Serialization)
│   ├── db/
│   │   ├── AppDatabase.kt       # Room Database
│   │   ├── WordEntity.kt        # 単語帳エンティティ
│   │   ├── WordDao.kt           # Room DAO
│   │   └── WordRepository.kt    # 単語帳リポジトリ
│   └── preferences/
│       └── AppPreferences.kt    # DataStore 設定永続化
├── speech/
│   └── TextToSpeechManager.kt   # Android標準 TextToSpeech マネージャー
└── ui/
    ├── MainApp.kt               # ボトムナビゲーション & Scaffold
    ├── theme/                   # Material 3 テーマ・カラー・タイポグラフィ
    ├── home/                    # 例文生成画面 & ViewModel
    ├── vocabulary/              # 単語帳・復習画面 & ViewModel
    └── settings/                # 設定画面 & ViewModel
```

---

## OllamaをローカルPCで公開して接続する手順（参考）
同一Wi-Fi上の実機スマホからPCのOllamaに接続する場合、PC側のOllamaで外部接続を許可します：

```bash
# Linux / macOS
OLLAMA_HOST=0.0.0.0:11434 ollama serve

# Windows (環境変数 OLLAMA_HOST に 0.0.0.0:11434 を追加して再起動)
```
アプリの設定画面で `http://<PCのローカルIPアドレス>:11434` を指定し、「接続テスト」を行ってください。

