package com.yuji.ankisupporter

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.yuji.ankisupporter.ui.theme.AnkiSupporterTheme

class MainActivity : ComponentActivity() {

    private val _appTAG = "Main Activity"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.v(_appTAG, "Permission request has been granted!")
        } else {
            finish()
        }
    }

    private fun startRecordAudioPermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(_appTAG, "AnkiSupporter Start")

        startRecordAudioPermissionRequest()

        setContent {
            AnkiSupporterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AnkiSupporterApp()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        WordManager.onStop()
        WordRecognizer.stop()
    }
}