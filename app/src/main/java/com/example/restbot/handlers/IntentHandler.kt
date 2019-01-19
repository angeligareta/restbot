package com.example.restbot.handlers


import android.util.Log
import com.example.restbot.MainActivity
import com.example.restbot.asynctasks.EntityName
import com.google.gson.JsonElement
import java.util.HashMap

/**
 * Object that handles the complex dialog flow intents.
 */
object IntentHandler {

    private val TAG = "IntentHandler"
    private var currentOrder : ArrayList<String> = ArrayList()
    private lateinit var activity : MainActivity

    /**
     * Method that handle the intent received by parameter.
     * It works by looking for a method in IntentHandler that has the name of the intent.
     * If it exists, it would be called with the intentParameters.
     */
    fun handleIntent(activity: MainActivity, intentName: String?, intentParameters: HashMap<String, JsonElement>){
        try {
            IntentHandler.activity = activity
            val method = this.javaClass.getMethod(intentName, intentParameters.javaClass)
            method.invoke(this, intentParameters)
        }
        catch (error : Exception) {
            // Could notify for further intent implementations
            Log.d(TAG, "Intent $intentName does not have a method")
        }
    }

    /** METHODS FOR EACH COMPLEX DIALOGFLOW INTENT */
    // TODO: Change that to Dialogflow fulfillment
    fun showChooseFood(intentParameters : HashMap<String, JsonElement>) {
        activity.sendMessage("MENÚ DE COMIDA: \n" + LocalDatabaseHandler.formatMenu(EntityName.FOOD), true)
        activity.sendMessage("¿Qué te apetece de comer? \n", true, 2)
    }

    fun showChooseDrink(intentParameters : HashMap<String, JsonElement>) {
        activity.sendMessage("CARTA DE BEBIDAS: \n" + LocalDatabaseHandler.formatMenu(EntityName.DRINK), true)
        activity.sendMessage("¿Qué te apetece de beber? \n", true, 2)
    }

    fun wantDessertQuestion(intentParameters : HashMap<String, JsonElement>) {
        activity.sendMessage("CARTA DE POSTRES: \n" + LocalDatabaseHandler.formatMenu(EntityName.DESSERT), true, 1)
    }

    fun checkOrder(intentParameters : HashMap<String, JsonElement>) {
        val currentOrderFormatted : String = "PEDIDO ACTUAL: \n" + LocalDatabaseHandler.SEPARATOR.repeat(27) + "\n" +
                currentOrder.joinToString(separator = "\n\t ► ", prefix = "\t ► ")
        activity.sendMessage(currentOrderFormatted, true)
        activity.sendMessage("¿Está todo correcto? \n", true, 2)
    }

    fun checkOrder2(intentParameters : HashMap<String, JsonElement>) {
        checkOrder(intentParameters)
    }

    fun askForFood(intentParameters : HashMap<String, JsonElement>) {
        Log.d(TAG, "ASK FOR FOOD $intentParameters")
        if (intentParameters.containsKey("quantity")) {
            currentOrder.add(intentParameters["quantity"]?.asString?.capitalize()
                    + " de "
                    + intentParameters[EntityName.FOOD.entityName]?.asString?.capitalize()
            )
        }
    }

    fun askForDrinks(intentParameters : HashMap<String, JsonElement>) {
        Log.d(TAG, "ASK FOR FOOD $intentParameters")
        if (intentParameters.containsKey("quantity")) {
            currentOrder.add(intentParameters["quantity"]?.asString?.capitalize()
                    + " de "
                    + intentParameters[EntityName.DRINK.entityName]?.asString?.capitalize()
            )
        }
    }

    fun askForDessert(intentParameters : HashMap<String, JsonElement>) {
        Log.d(TAG, "ASK FOR FOOD $intentParameters")
        if (intentParameters.containsKey("quantity")) {
            currentOrder.add(intentParameters["quantity"]?.asString?.capitalize()
                    + " de "
                    + intentParameters[EntityName.DESSERT.entityName]?.asString?.capitalize()
            )
        }
    }

    fun getBill(intentParameters : HashMap<String, JsonElement>) {
        Log.d(TAG, "GET BILL REACHED $intentParameters")
    }

}