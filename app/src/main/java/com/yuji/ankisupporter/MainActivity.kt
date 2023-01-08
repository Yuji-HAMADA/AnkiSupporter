package com.yuji.ankisupporter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.yuji.ankisupporter.ui.AppScreen
import com.yuji.ankisupporter.ui.theme.AnkiSupporterTheme

class MainActivity : AppCompatActivity() {

    private val _appTAG = "Main Activity"

    companion object {
        const val REQUEST_RECORD_AUDIO_PERMISSION = 1001
    }

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(_appTAG, "AnkiSupporter Start")

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        setContent {
            AnkiSupporterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AppScreen()
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

@Preview(name = "My Preview", showSystemUi = true)
@Composable
fun CardPreview() {
    AnkiSupporterTheme {
        Column {
            Button(onClick = {}) {
                Text(stringResource(R.string.record))
            }
            Text(text = "Hello Japanese friends")
        }
    }
}