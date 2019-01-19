package com.example.restbot.handlers

import ai.api.util.IOUtils
import android.os.Environment
import com.example.restbot.MainActivity
import com.example.restbot.R
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream


object KeyHandler {

    private val CREDENTIALS_FILE_NAME = "app/credentials.json"
    lateinit var DEVELOPER_ACCESS_TOKEN : String
    lateinit var ACCESS_TOKEN : String

    fun setContext(mainActivity: MainActivity) {
        val jsonString = IOUtils.readAll(mainActivity.resources.openRawResource(R.raw.credentials))
        val jsonObject = JSONObject(jsonString)
        DEVELOPER_ACCESS_TOKEN = jsonObject.getString("DEVELOPER_ACCESS_TOKEN")
        ACCESS_TOKEN = jsonObject.getString("ACCESS_TOKEN")
    }

}