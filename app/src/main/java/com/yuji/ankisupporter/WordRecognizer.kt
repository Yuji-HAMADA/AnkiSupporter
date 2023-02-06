package com.yuji.ankisupporter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.yuji.ankisupporter.ui.AppViewModel

object WordRecognizer {
    private var speechRecognizer : SpeechRecognizer? = null
    private const val wrTAG = "WordRecognizer"

    fun init(
        context: Context,
        appViewModel: AppViewModel
    ) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val recData = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (recData!!.size > 0) {
                    Log.v(wrTAG, recData[0])
                    appViewModel.updateInputWord(recData[0])
                    WordSpeaker.speak(recData[0])
                }
            }

            override fun onBeginningOfSpeech() {
//                TODO("Not yet implemented")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                TODO("Not yet implemented")
            }

            override fun onEndOfSpeech() {
//                TODO("Not yet implemented")
                Log.v(wrTAG, "End Of Speech")
            }

            override fun onError(error: Int) {
//                TODO("Not yet implemented")
            }

            override fun onReadyForSpeech(params: Bundle?) {
//                TODO("Not yet implemented")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                TODO("Not yet implemented")
            }

            override fun onPartialResults(partialResults: Bundle?) {
                TODO("Not yet implemented")
            }

            override fun onRmsChanged(rmsdB: Float) {
//                TODO("Not yet implemented")
            }
        })
    }

    fun startRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        this.speechRecognizer?.startListening(intent)
    }

    fun stop() {
        speechRecognizer?.destroy()
    }

    /*
    private fun startRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        try {
            startActivityForResult(intent, Request_recognize_speech)
        }
     */

}