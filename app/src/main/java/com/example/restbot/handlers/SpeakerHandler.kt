package com.example.restbot.handlers

import android.speech.tts.TextToSpeech
import com.example.restbot.MainActivity

/**
 * Object designed to act as a speaker, playing out loud a message in the context received when instantiated.
 */
object SpeakerHandler : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null

    fun setContext(context: MainActivity) {
        textToSpeech = TextToSpeech(context, this)
    }

    fun play(textToSpeak: String?) {
        textToSpeech?.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null, null)
    }

    override fun onInit(status: Int) {}
}