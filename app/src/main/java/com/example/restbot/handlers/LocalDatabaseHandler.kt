package com.example.restbot.handlers

import ai.api.util.StringUtils
import android.util.Log
import com.example.restbot.asynctasks.EntityName
import org.json.JSONObject
import java.text.Normalizer

/**
 * Object that download the entries from the Dialogflow database and assign the values of the
 * different menus to a local database.
 */
object LocalDatabaseHandler {

    const val SEPARATOR = "-"
    private var TAG = "LocalDatabaseHandler"

    /**
     * Data structure that represent a dish.
     */
    class Dish(val name: String, val price: Double)

    // TODO: Change it to a Local DataBase
    private var foodMenu = LinkedHashMap<String, ArrayList<Dish>>()
    private var drinkMenu = LinkedHashMap<String, ArrayList<Dish>>()
    private var dessertMenu = LinkedHashMap<String, ArrayList<Dish>>()

    /**
     * When initialized, it request the entries for each entity in Dialogflow in an asynchronous way,
     * when those request are finished, they will call back the fillMenu method.
     */
    init {
//        Log.d(TAG, "Getting entries")
//        EntityManagementTask().execute(EntityQuery(EntityQueryType.GET_ENTRIES_OF_ENTITY, EntityName.FOOD, null))
//        EntityManagementTask().execute(EntityQuery(EntityQueryType.GET_ENTRIES_OF_ENTITY, EntityName.DRINK, null))
//        EntityManagementTask().execute(EntityQuery(EntityQueryType.GET_ENTRIES_OF_ENTITY, EntityName.DESSERT, null))
    }

    /**
     * Returns menu with the name of the one received.
     */
    fun getMenu(entityName: EntityName) : LinkedHashMap<String, ArrayList<Dish>> {
        return when (entityName) {
            EntityName.FOOD -> foodMenu
            EntityName.DRINK -> drinkMenu
            EntityName.DESSERT -> dessertMenu
        }
    }

    /**
     * When the asynchronous requests finish, this method fill the database
     * with the json data of the entity name received by argument.
     */
    fun fillMenu(entityName: EntityName, jsonRaw: String?) {
        fillSpecificMenu(getMenu(entityName), jsonRaw)
    }

    /**
     * Method that takes an specific menu from the database,
     * beautify it, and return it as String.
     */
    fun formatMenu(entityName: EntityName): String {
        return formatSpecificMenu(getMenu(entityName))
    }

    /**
     * Same functionality as fillMenu but for specific entity menu.
     */
    private fun fillSpecificMenu(menu: LinkedHashMap<String, ArrayList<Dish>>, jsonRaw: String?) {
        try {
            var jsonArray = JSONObject(jsonRaw).getJSONArray("entries")
            for (i in 0 until jsonArray.length()) {
                var jsonObject = jsonArray.getJSONObject(i)
                var menuCategoryName = jsonObject.getString("value")
                var dishJsonArray = jsonObject.getJSONArray("synonyms")

                var dishArray = ArrayList<Dish>()
                for (j in 0 until dishJsonArray.length()) {
                    var dish = dishJsonArray.getJSONObject(j)
                    var dishName = dish.getString("name")
                    var dishPrice = dish.getDouble("price")

                    dishArray.add(Dish(dishName, dishPrice))
                }
                menu.put(menuCategoryName, dishArray)
            }
        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage)
        }
    }

    /**
     * Same functionality as formatMenu but for specific entity menu.
     */
    private fun formatSpecificMenu(menu: LinkedHashMap<String, ArrayList<Dish>>): String {
        var formattedMenu = ""
        menu.forEach { dishCategory ->
            val dishCategorySeparator = "\n" + SEPARATOR.repeat(dishCategory.key.length * 2) + "\n"

            formattedMenu += dishCategorySeparator + dishCategory.key + dishCategorySeparator
            dishCategory.value.forEach { dish ->
                formattedMenu += "\t ► ${dish.name.capitalize()}. " +  "%.2f".format(dish.price) + " € \n"
            }
        }
        return formattedMenu
    }

    fun getDish(dishOrderedName: String, entityName: EntityName): Dish {
        val normalizedDishOrderedName = normalizeString(dishOrderedName)
        Log.d(TAG, "Normalizaded from $dishOrderedName to $normalizedDishOrderedName")

        getMenu(entityName).forEach { dishArray ->
            dishArray.value.forEach { dish ->
                var normalizedDishName = normalizeString(dish.name)
                if (normalizedDishName.contains(normalizedDishOrderedName)
                        || normalizedDishOrderedName.contains(normalizedDishName)) {
                    return dish
                }
            }
        }
        return Dish("", 0.0)
    }

    private fun normalizeString(text: String) : String {
        var normalizedText = text.toLowerCase()
        normalizedText = normalizedText.replace("á", "a")
        normalizedText = normalizedText.replace("é", "e")
        normalizedText = normalizedText.replace("í", "i")
        normalizedText = normalizedText.replace("ó", "o")
        normalizedText = normalizedText.replace("ú", "u")
        return normalizedText
    }
}