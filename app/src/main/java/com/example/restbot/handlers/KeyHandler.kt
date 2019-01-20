package com.example.restbot.handlers

import ai.api.util.IOUtils
import com.example.restbot.MainActivity
import com.example.restbot.R
import org.json.JSONObject

/**
 * Object that read the necessary credentials from a secure place in the system
 * and make those values available for the rest of classes.
 */
object KeyHandler {

    lateinit var DEVELOPER_ACCESS_TOKEN: String
    lateinit var ACCESS_TOKEN: String

    fun setContext(mainActivity: MainActivity) {
        val jsonString = IOUtils.readAll(mainActivity.resources.openRawResource(R.raw.credentials))
        val jsonObject = JSONObject(jsonString)
        DEVELOPER_ACCESS_TOKEN = jsonObject.getString("DEVELOPER_ACCESS_TOKEN")
        ACCESS_TOKEN = jsonObject.getString("ACCESS_TOKEN")
    }

}