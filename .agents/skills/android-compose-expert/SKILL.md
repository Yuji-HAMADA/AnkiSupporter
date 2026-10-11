---
name: android-compose-expert
description: Androidアプリ開発におけるKotlin、Jetpack Compose、MVVMアーキテクチャ、およびGoogle公式の推奨ベストプラクティスに関する専門知識を提供し、高品質なコードの生成やリファクタリングを支援します。
---

# Android / Jetpack Compose / MVVM ベストプラクティスガイドライン

このプロジェクトでAndroidアプリのコードを記述、修正、リファクタリングする際は、Google公式の最新アーキテクチャ推奨事項および以下の原則を厳守すること。

## 1. アーキテクチャとレイヤー分離 (Modern App Architecture)
- **関心の分離 (Separation of Concerns):** アプリは少なくとも「UI層」と「データ層」に明確に分離する。
- **UI層 (UI Layer):** 画面の描画とユーザーインタラクションの処理を担当する。UI構築には必ず **Jetpack Compose** を使用する。
- **データ層 (Data Layer):** アプリケーションデータとビジネスロジックをカプセル化する。データソース（Room、Retrofit、Firebaseなど）に直接UIコンポーネントがアクセスしてはならない。必ず **Repositoryパターン** を介してデータを公開すること。

## 2. 状態管理とMVVMパターン
- **ViewModelの活用:** 画面の状態（UI State）保持、ビジネスロジックの実行、画面回転などの設定変更（Configuration Changes）を跨いだデータ保持には `androidx.lifecycle.ViewModel` を使用する。
- **単方向データフロー (UDF: Unidirectional Data Flow):**
  - **Stateの公開:** ViewModelは、画面の状態を不変（Immutable）なデータクラス（例: `data class MyUiState(...)`）として、`StateFlow<MyUiState>`（または `uiState`）でUI側に公開する。
  - **イベントの伝達:** UI側からのユーザーアクション（クリック等）は、ViewModelの関数（イベントハンドラ）を呼び出すことで通知し、状態の変更はすべてViewModel内で行う。
- **ライフサイクルに配慮した状態収集:**
  - Compose側でViewModelのStateFlowを安全に収集するため、`collectAsStateWithLifecycle()` を使用する（`collectAsState()` は原則避ける）。

## 3. 非同期処理とKotlin
- **Coroutines & Flow:** 非同期処理やストリームの伝播には、LiveDataではなく **Kotlin Coroutines** と **Flow** を使用する。
- **スコープ管理:** ViewModel内では `viewModelScope` を使用し、ライフサイクルに応じた適切なキャンセルとメモリリーク防止を徹底する。

## 4. コーディング規約とUI実装の注意点
- **プレビューの提供:** Composable関数には、必ず `@Preview` を付与したプレビュー用関数を併記し、UIの視覚的確認をしやすくする。
- **状態のホイスティング (State Hoisting):** ステートレスなComposableとステートフルなComposableを適切に分離し、テスト容易性を高める。
- **プロパティ・命名規則:** Kotlinの標準的なコーディング規約（CamelCase, snake_case等）に従い、可読性の高いコードを書く。