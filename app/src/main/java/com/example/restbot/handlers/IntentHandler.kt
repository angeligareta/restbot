package com.example.restbot.handlers


import android.util.Log
import com.example.restbot.MainActivity
import com.example.restbot.asynctasks.EntityName
import com.google.gson.JsonElement
import java.util.HashMap
import kotlin.collections.ArrayList

/**
 * Object that handles the complex dialog flow intents.
 */
object IntentHandler {

    private val TAG = "IntentHandler"
    private var currentOrder: ArrayList<DishOrdered> = ArrayList()
    private lateinit var activity: MainActivity

    /**
     * Data structure that represent an ordered dish.
     */
    class DishOrdered(val name: String, val quantity: Int = 1, val price: Double)

    /**
     * Method that handle the intent received by parameter.
     * It works by looking for a method in IntentHandler that has the name of the intent.
     * If it exists, it would be called with the intentParameters.
     */
    fun handleIntent(activity: MainActivity, intentName: String?, intentParameters: HashMap<String, JsonElement>) {
        try {
            IntentHandler.activity = activity
            val method = this.javaClass.getMethod(intentName, intentParameters.javaClass)
            method.invoke(this, intentParameters)
        } catch (error: Exception) {
            // Could notify for further intent implementations
            Log.d(TAG, "Intent $intentName does not have a method")
        }
    }

    /** METHODS FOR EACH COMPLEX DIALOGFLOW INTENT */
    // TODO: Change that to Dialogflow fulfillment
    fun showChooseFood(intentParameters: HashMap<String, JsonElement>) {
        resetOrder()
        activity.sendMessage("MENÚ DE COMIDA: \n" + LocalDatabaseHandler.formatMenu(EntityName.FOOD), true)
        activity.sendMessage("¿Qué te apetece de comer? \n", true, 2)
    }

    fun showChooseDrink(intentParameters: HashMap<String, JsonElement>) {
        activity.sendMessage("CARTA DE BEBIDAS: \n" + LocalDatabaseHandler.formatMenu(EntityName.DRINK), true)
        activity.sendMessage("¿Qué te apetece de beber? \n", true, 2)
    }

    fun wantDessertQuestion(intentParameters: HashMap<String, JsonElement>) {
        activity.sendMessage("CARTA DE POSTRES: \n" + LocalDatabaseHandler.formatMenu(EntityName.DESSERT), true, 1)
    }

    fun askForGeneric(entityName : EntityName, intentParameters: HashMap<String, JsonElement>) {
        if (intentParameters.containsKey("quantity") && intentParameters.containsKey(entityName.entityName)) {
            var dishOrderedQuantity = intentParameters["quantity"]!!.asInt // We made sure it contains it
            var dishOrderedName = intentParameters[entityName.entityName]!!.asString!!.capitalize()
            var realDish = LocalDatabaseHandler.getDish(dishOrderedName, entityName)

            Log.d(TAG, "ASKED FOR $dishOrderedQuantity of $dishOrderedName -> " +
                    "[ realName: ${realDish.name}, price: ${realDish.price}")

            currentOrder.add(DishOrdered(realDish.name, dishOrderedQuantity, realDish.price))
        }
        else {
            Log.d(TAG, "SOMETHING WENT WRONG ASKING FOR $entityName")
        }
    }

    fun askForFood(intentParameters: HashMap<String, JsonElement>) {
        askForGeneric(EntityName.FOOD, intentParameters)
    }

    fun askForDrinks(intentParameters: HashMap<String, JsonElement>) {
        askForGeneric(EntityName.DRINK, intentParameters)
    }

    fun askForDessert(intentParameters: HashMap<String, JsonElement>) {
        askForGeneric(EntityName.DESSERT, intentParameters)
    }

    fun checkOrder(intentParameters: HashMap<String, JsonElement>) {
        var currentOrderFormatted = "PEDIDO ACTUAL: \n" + LocalDatabaseHandler.SEPARATOR.repeat(27) + "\n"
        currentOrder.forEach { orderedDish ->
            currentOrderFormatted += "\t ► " + orderedDish.quantity.toString() + " de " + orderedDish.name +
                    " %.2f".format(orderedDish.price * orderedDish.quantity) + " €\n"
        }
        activity.sendMessage(currentOrderFormatted, true)
        activity.sendMessage("¿Está todo correcto? \n", true, 2)
    }

    fun checkOrder2(intentParameters: HashMap<String, JsonElement>) {
        checkOrder(intentParameters)
    }

    fun resetOrder() {
        currentOrder.clear()
    }

    fun getBill(intentParameters: HashMap<String, JsonElement>) {
        Log.d(TAG, "GET BILL REACHED $intentParameters")
    }

}