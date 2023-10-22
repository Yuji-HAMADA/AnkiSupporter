package com.yuji.ankisupporter

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yuji.ankisupporter.ui.theme.AnkiSupporterTheme
import com.yuji.ankisupporter.utility.WordManager
import com.yuji.ankisupporter.utility.WordRecognizer

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

    private fun startInternetPermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.INTERNET)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(_appTAG, "AnkiSupporter Start")

        startRecordAudioPermissionRequest()
        startInternetPermissionRequest()

        setContent {
            AnkiSupporterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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