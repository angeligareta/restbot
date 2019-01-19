package com.example.restbot.asynctasks

import ai.api.AIServiceContext
import ai.api.android.AIDataService
import ai.api.model.AIRequest
import ai.api.model.AIResponse
import android.app.Activity
import android.os.AsyncTask
import com.example.restbot.MainActivity

/**
 * Class designed to handle the communication with the chatbot in an asynchronous way.
 */
class AIRequestTask(private val activity: Activity, private val aiDataService : AIDataService, private val customAIServiceContext : AIServiceContext) :
        AsyncTask<AIRequest, Void, AIResponse>() {

    /**
     * It uses the AI Data Service member to make a request.
      */
    override fun doInBackground(vararg aiRequests: AIRequest?): AIResponse {
        val request = aiRequests[0]
        return aiDataService.request(request, customAIServiceContext)
    }

    /**
     * Once the request is satisfied, the response is sent to the main activity, that handles it.
     */
    override fun onPostExecute(aiResponse: AIResponse?) {
        (activity as MainActivity).requestCallback(aiResponse)
    }

}
