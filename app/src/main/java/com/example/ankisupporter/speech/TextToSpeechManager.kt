package com.example.ankisupporter.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Android標準のTextToSpeechを用いたローカル完結・完全無料の音声読み上げマネージャー
 */
class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private val tag = "TTSManager"
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var speechRate: Float = 1.0f

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                engine.language = Locale.US
                engine.setSpeechRate(speechRate)
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                        Log.e(tag, "TTS onError code: $errorCode for $utteranceId")
                    }
                })
                _isInitialized.value = true
                Log.d(tag, "TTS initialized successfully")
            }
        } else {
            Log.e(tag, "TTS initialization failed with code $status")
            _isInitialized.value = false
        }
    }

    /**
     * 英語テキストの読み上げ
     */
    fun speakEnglish(text: String, utteranceId: String = "en_${System.currentTimeMillis()}") {
        speak(text, Locale.US, utteranceId)
    }

    /**
     * 日本語テキストの読み上げ
     */
    fun speakJapanese(text: String, utteranceId: String = "ja_${System.currentTimeMillis()}") {
        speak(text, Locale.JAPANESE, utteranceId)
    }

    /**
     * 指定のロケールでテキストを読み上げ
     */
    fun speak(text: String, locale: Locale, utteranceId: String = "utt_${System.currentTimeMillis()}") {
        val engine = tts ?: return
        if (!_isInitialized.value || text.isBlank()) return

        engine.language = locale
        engine.setSpeechRate(speechRate)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * 読み上げの停止
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /**
     * 読み上げ速度の変更 (0.5f 〜 2.0f)
     */
    fun setSpeechRate(rate: Float) {
        speechRate = rate
        tts?.setSpeechRate(rate)
    }

    /**
     * リソース解放
     */
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e(tag, "Error shutting down TTS", e)
        }
    }
}

