package com.example.ankisupporter.data.ai

import com.example.ankisupporter.model.AiProvider
import com.example.ankisupporter.model.GeneratedCard

/**
 * AIによる学習カード生成の共通インターフェース
 */
interface AiClient {
    val provider: AiProvider

    /**
     * 入力された単語やフレーズから英語例文カードを生成する
     */
    suspend fun generateExample(wordOrPhrase: String): Result<GeneratedCard>

    /**
     * 接続確認テスト
     */
    suspend fun testConnection(): Result<String>
}

