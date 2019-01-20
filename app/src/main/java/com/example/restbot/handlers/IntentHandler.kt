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
    private var totalPrice: Double = 0.0
    private val dailyRecommendation = "Medallón de salmón noruego con papas cocidas y salsa bearnesa"
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
        activity.sendMessage("La recomendación del día es: $dailyRecommendation.\n", true)
        activity.sendMessage("¿Qué te apetece de comer? \n", true, 3)
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

            makeSuggestion(entityName, realDish)

            currentOrder.add(DishOrdered(realDish.name, dishOrderedQuantity, realDish.price))
        }
        else {
            Log.d(TAG, "SOMETHING WENT WRONG ASKING FOR $entityName")
        }
    }

    private fun makeSuggestion(entityName: EntityName, realDish: LocalDatabaseHandler.Dish) {
        if (entityName == EntityName.FOOD) {
            if (realDish.category.toLowerCase() == "carne") {
                activity.sendMessage("Con ésto creo que le iría muy bien un vino tinto. ¡Luego te lo enseño en la carta de bebidas!", true)
            }
            else if (realDish.category.toLowerCase() == "pescados") {
                activity.sendMessage("Con ésto creo que le iría muy bien un vino blanco. ¡Luego te lo enseño en la carta de bebidas!", true)
            }
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
            currentOrderFormatted += "\t ► " + orderedDish.quantity.toString() + " de " + orderedDish.name + ".\n"
        }
        activity.sendMessage(currentOrderFormatted, true)
        activity.sendMessage("¿Está todo correcto? \n", true, 2)
    }

    fun checkOrder2(intentParameters: HashMap<String, JsonElement>) {
        checkOrder(intentParameters)
    }

    fun resetOrder() {
        currentOrder.clear()
        totalPrice = 0.0
    }

    fun orderCorrect(intentParameters: HashMap<String, JsonElement>) {
        RemoteDatabaseHandler.addNewOrder(currentOrder)
    }

    fun askForBill(intentParameters: HashMap<String, JsonElement>) {
        if (currentOrder.size != 0) {
            var currentOrderFormatted = "LA CUENTA ES: \n" + LocalDatabaseHandler.SEPARATOR.repeat(27) + "\n"
            totalPrice = 0.0
            currentOrder.forEach { orderedDish ->
                currentOrderFormatted += "\t ► " + orderedDish.quantity.toString() + " de " + orderedDish.name +
                        ". %.2f".format(orderedDish.price * orderedDish.quantity) + "€\n"
                totalPrice += (orderedDish.price * orderedDish.quantity)
            }
            currentOrderFormatted += LocalDatabaseHandler.SEPARATOR.repeat(27) + "\n" + "PRECIO TOTAL: " +
                "%.2f".format(totalPrice) + "€\n"

            activity.sendMessage(currentOrderFormatted, true)
            activity.sendMessage("¿Quieren que os la divida?", true, 3)
        }
        else {
            activity.sendMessage("¿La cuenta? Vamos a empezar por pedir... Escribe 'empezar'", true, 1)
        }
    }

    fun adjustBill(intentParameters: HashMap<String, JsonElement>) {
        var quantity = intentParameters["number"]!!.asInt
        activity.sendMessage("Pues serían ${"%.2f".format(totalPrice.div(quantity))}€ por persona. ¿Cómo deseas pagar, tarjeta o efectivo?", true)
    }



}