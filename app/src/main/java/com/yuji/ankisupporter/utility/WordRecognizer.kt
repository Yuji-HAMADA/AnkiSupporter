package com.yuji.ankisupporter.utility

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.yuji.ankisupporter.ui.item.ItemEditViewModel
import com.yuji.ankisupporter.ui.item.ItemEntryViewModel

object WordRecognizer : RecognitionListener {
    private lateinit var speechRecognizer : SpeechRecognizer
    private const val wrTAG = "WordRecognizer"

    private var mItemEntryViewModel: ItemEntryViewModel? = null
    private var mItemEditViewModel: ItemEditViewModel? = null

    fun init(context: Context) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer.setRecognitionListener(this)
    }

    override fun onResults(results: Bundle?) {
        val recData = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (recData!!.size > 0) {
            Log.v(wrTAG, recData[0])
            mItemEntryViewModel?.updateUiState(mItemEntryViewModel?.itemUiState!!.copy(name = recData[0]))
            mItemEditViewModel?.updateUiState(mItemEditViewModel?.itemUiState!!.copy(name = recData[0]))
            WordSpeaker.speak(recData[0])
        }
    }

    override fun onBeginningOfSpeech() {
        Log.v(wrTAG, "Begin Speech")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.v(wrTAG, "Buffer Received")
    }

    override fun onEndOfSpeech() {
        Log.v(wrTAG, "End Of Speech")
    }

    override fun onError(error: Int) {
        Log.v(wrTAG, "Error")
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.v(wrTAG, "Ready for Speech")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.v(wrTAG, "Event happened")
    }

    override fun onPartialResults(partialResults: Bundle?) {
        Log.v(wrTAG, "Partial Results")
    }

    override fun onRmsChanged(rmsdB: Float) {
    }

    fun startRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizer.startListening(intent)
    }

    fun stop() {
        speechRecognizer.destroy()
    }

    fun setItemEntryViewModel(itemEntryViewModel: ItemEntryViewModel) {
        mItemEntryViewModel = itemEntryViewModel
    }

    fun setItemEditViewModel(itemEdiViewModel: ItemEditViewModel) {
        mItemEditViewModel = itemEdiViewModel
    }
}