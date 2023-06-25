package com.yuji.ankisupporter.utility

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions


object Translator {
    private const val translatorTAG = "Translator"

    // Create an English-Japanese translator:
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.JAPANESE)
        .build()
    val englishJapaneseTranslator = Translation.getClient(options)

    fun init() {
        Log.v(translatorTAG, "Translator initialization")

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        englishJapaneseTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // Model downloaded successfully. Okay to start translating.
                // (Set a flag, unhide the translation UI, etc.)
                englishJapaneseTranslator.translate("book")
                    .addOnSuccessListener { translatedText ->
                        Log.v(translatorTAG, translatedText)
                    }
            }
            .addOnFailureListener { _ ->
                // Model could not be downloaded or other internal error.
                // ...
            }
    }
}
