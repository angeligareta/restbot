package com.example.chatbot

import android.speech.tts.TextToSpeech
import java.util.*

/**
 * Object designed to act as a speaker, playing out loud a message in the context received when instantiated.
 */
object Speaker : TextToSpeech.OnInitListener {

    private var textToSpeech : TextToSpeech? = null

    fun setContext(context: MainActivity) {
        textToSpeech = TextToSpeech(context, this)
    }


    fun play(textToSpeak: String?) {
        textToSpeech?.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null, null)
    }

    override fun onInit(status: Int) {

    }
}