package com.yuji.ankisupporter

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import java.io.IOException

object WordManager {

    private var fileName: String = ""
    private var player: MediaPlayer? = null
    private var recorder: MediaRecorder? = null

    private var mStartRecording : Boolean = true
    private var mStartPlaying : Boolean = true

    private const val LOG_TAG = "Word Manager"

    private fun startRecording(context: Context) {
        // Record to the external cache directory for visibility
        if (fileName == "") {
            fileName = "${context.externalCacheDir?.absolutePath}/audiorecordtest.3gp"
            Log.v(LOG_TAG, "filePath is set")
        }

        recorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    fun onRecord(context: Context) {
        if (mStartRecording) {
            Log.v(LOG_TAG, "start recording")
            startRecording(context)
        } else {
            Log.v(LOG_TAG, "stop recording")
            stopRecording()
        }
        mStartRecording = !mStartRecording
    }

    fun onPlay() {
        if (mStartPlaying) {
            Log.v(LOG_TAG, "start playing")
            startPlaying()
        } else {
            Log.v(LOG_TAG, "stop playing")
            stopPlaying()
        }
        mStartPlaying = !mStartPlaying
    }

    fun onStop() {
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }
}