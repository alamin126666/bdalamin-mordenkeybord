package com.modernkey.keyboard.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

enum class VoiceState { IDLE, LISTENING, PROCESSING, ERROR }

class VoiceInputManager(private val context: Context) {

    interface Callback {
        fun onStateChanged(state: VoiceState)
        fun onPartialResult(text: String)
        fun onResult(text: String)
        fun onError(errorCode: Int)
    }

    private var speechRecognizer: SpeechRecognizer? = null
    var callback: Callback? = null
    var currentState: VoiceState = VoiceState.IDLE
        private set

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(language: String = "en-US") {
        if (!isAvailable()) {
            callback?.onError(SpeechRecognizer.ERROR_CLIENT)
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(recognitionListener)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (language == "auto") "en-US" else language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        updateState(VoiceState.LISTENING)
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        updateState(VoiceState.PROCESSING)
    }

    fun cancel() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        updateState(VoiceState.IDLE)
    }

    private fun updateState(state: VoiceState) {
        currentState = state
        callback?.onStateChanged(state)
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            updateState(VoiceState.LISTENING)
        }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            updateState(VoiceState.PROCESSING)
        }
        override fun onError(error: Int) {
            updateState(VoiceState.ERROR)
            callback?.onError(error)
            updateState(VoiceState.IDLE)
        }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull() ?: ""
            if (text.isNotEmpty()) callback?.onResult(text)
            updateState(VoiceState.IDLE)
        }
        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            partial?.firstOrNull()?.let { callback?.onPartialResult(it) }
        }
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
