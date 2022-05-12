package com.example.core.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import timber.log.Timber
import java.util.*

class AIService : Service() {

    private val speechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(this) }

    private var message = ""

    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(this) }

    private val isGrantedPermission
        get() = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private val speechRecognizerIntent by lazy {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000)
        }
    }

    private var lastRmsMax = 0L

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).e("onCreate")
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Timber.tag(TAG).e("onReadyForSpeech")
                localBroadcastManager.sendBroadcast(Intent(ResultAction.BEGIN.value))
                lastRmsMax = 0L
            }

            override fun onBeginningOfSpeech() {
                Timber.tag(TAG).e("onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                Timber.tag(TAG).e("onRmsChanged $rmsdB")
                if (rmsdB >= 9f) {
                    lastRmsMax = System.currentTimeMillis()
                } else {
                    if (lastRmsMax != 0L && System.currentTimeMillis() - lastRmsMax > RECORD_TIME_OUT) {
                        handleStopRecord()
                    }
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Timber.tag(TAG).e("onBufferReceived")
            }

            override fun onEndOfSpeech() {
                Timber.tag(TAG).e("onEndOfSpeech")
            }

            override fun onError(error: Int) {
                Timber.tag(TAG).e("onError $error")
                if (error != 8) {
                    localBroadcastManager.sendBroadcast(Intent(ResultAction.ERROR.value).apply {
                        putExtra(ARG_DATA, "$error")
                    })
                }
            }

            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)?.let {
                    if (it.isNotBlank()) {
                        message = it
                    }
                }
                Timber.tag(TAG).e("onResults $message")
                message = message.capitalize(Locale.getDefault())
                localBroadcastManager.sendBroadcast(Intent(ResultAction.NEW_MESSAGE.value).apply {
                    putExtra(ARG_DATA, message)
                })
                message = ""
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)?.let {
                    if (it.isNotBlank()) {
                        message = it
                    }
                }
                Timber.tag(TAG).e("partialResults $message")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Timber.tag(TAG).e("onEvent")
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(TAG).e("onStartCommand ${intent?.action}")
        when (intent?.action) {

            Action.START_RECORD.value -> {
                handleStartRecord()
            }

            Action.STOP_RECORD.value -> {
                handleStopRecord()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        super.onDestroy()
    }

    private fun handleStartRecord() {
        if (isGrantedPermission) {
            speechRecognizer.startListening(speechRecognizerIntent)
        }
    }

    private fun handleStopRecord() {
        speechRecognizer.stopListening()
    }

    enum class Action(val value: String) {
        START_RECORD("START_RECORD"),
        STOP_RECORD("STOP_RECORD")
    }

    enum class ResultAction(val value: String) {
        ERROR("ACTION_RECORD_ERROR"),
        BEGIN("ACTION_BEGIN_RECORD"),
        NEW_MESSAGE("ACTION_NEW_RECORD_MESSAGE")
    }

    companion object {
        const val TAG = "AIService"
        const val ARG_DATA = "ARG_DATA"

        const val RECORD_TIME_OUT = 1000L
    }
}