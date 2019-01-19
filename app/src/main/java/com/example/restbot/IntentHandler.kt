package com.example.restbot


import android.util.Log
import com.google.gson.JsonElement
import java.util.HashMap

/**
 * Object that handles the complex dialog flow intents.
 */
object IntentHandler {

    private val TAG = "IntentHandler"
    private var dailyMenu : ArrayList<String> = ArrayList()

    /**
     * Method that handle the intent received by parameter.
     * It works by looking for a method in IntentHandler that has the name of the intent.
     * If it exists, it would be called with the intentParameters.
     */
    fun handleIntent(intentName: String?, intentParameters: HashMap<String, JsonElement>){
        try {
            val method = this.javaClass.getMethod(intentName, intentParameters.javaClass)
            method.invoke(this, intentParameters)
        }
        catch (error : Exception) {
            // Could notify for further intent implementations
            Log.d(TAG, "Intent $intentName does not have a method")
        }
    }

    fun welcome(intentParameters : HashMap<String, JsonElement>) {

    }

    /** METHODS FOR EACH COMPLEX DIALOGFLOW INTENT */
    fun getDailyMenu(intentParameters : HashMap<String, JsonElement>) {
        Speaker.play("El menu del dia es ")
        for (food in dailyMenu) {
            Speaker.play(food)
        }
        Log.d(TAG, "GET DAILY MENU REACHED $intentParameters")
    }

    fun setDailyMenu(intentParameters : HashMap<String, JsonElement>) {
        Log.d(TAG, "SET DAILY MENU REACHED $intentParameters")

        val foodList = intentParameters["food"]
        foodList!!.asJsonArray.forEach { food ->
            dailyMenu.add(food.asString)
        }
    }

    fun getBill(intentParameters : HashMap<String, JsonElement>) {
        Log.d(TAG, "GET BILL REACHED $intentParameters")
    }

    fun askForFood(intentParameters : HashMap<String, JsonElement>) {
        Log.d(TAG, "ASK FOR FOOD $intentParameters")
    }

}