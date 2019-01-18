package com.example.chatbot

import android.speech.tts.TextToSpeech

/**
 * Class designed to act as a speaker, playing out loud a message in the context received when instantiated.
 */
class Speaker(context: MainActivity) : TextToSpeech.OnInitListener {

    private var textToSpeech : TextToSpeech = TextToSpeech(context, this)

    fun play(textToSpeak: String?) {
        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {

    }
}