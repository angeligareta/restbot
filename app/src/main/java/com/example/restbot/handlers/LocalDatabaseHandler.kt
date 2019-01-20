package com.example.restbot.handlers

import android.util.Log
import com.example.restbot.asynctasks.EntityManagementTask
import com.example.restbot.asynctasks.EntityName
import com.example.restbot.asynctasks.EntityQuery
import com.example.restbot.asynctasks.EntityQueryType
import org.json.JSONArray

/**
 * Object that download the entries from the Dialogflow database and assign the values of the
 * different menus to a local database.
 */
object LocalDatabaseHandler {

    const val SEPARATOR = "-"
    private var TAG = "LocalDatabaseHandler"

    // TODO: Change it to a Local DataBase
    private var foodMenu = LinkedHashMap<String, ArrayList<String>>()
    private var drinkMenu = LinkedHashMap<String, ArrayList<String>>()
    private var dessertMenu = LinkedHashMap<String, ArrayList<String>>()

    /**
     * When initialized, it request the entries for each entity in Dialogflow in an asynchronous way,
     * when those request are finished, they will call back the fillMenu method.
     */
    init {
        Log.d(TAG, "Getting entries")
        EntityManagementTask().execute(EntityQuery(EntityQueryType.GET_ENTRIES_OF_ENTITY, EntityName.FOOD, null))
        EntityManagementTask().execute(EntityQuery(EntityQueryType.GET_ENTRIES_OF_ENTITY, EntityName.DRINK, null))
        EntityManagementTask().execute(EntityQuery(EntityQueryType.GET_ENTRIES_OF_ENTITY, EntityName.DESSERT, null))
    }

    /**
     * When the asynchronous requests finish, this method fill the database
     * with the json data of the entity name received by argument.
     */
    fun fillMenu(entityName: EntityName, jsonRaw: String?) {
        when (entityName) {
            EntityName.FOOD -> fillSpecificMenu(foodMenu, jsonRaw)
            EntityName.DRINK -> fillSpecificMenu(drinkMenu, jsonRaw)
            EntityName.DESSERT -> fillSpecificMenu(dessertMenu, jsonRaw)
        }
    }

    /**
     * Method that takes an specific menu from the database,
     * beautify it, and return it as String.
     */
    fun formatMenu(entityName: EntityName): String {
        return when (entityName) {
            EntityName.FOOD -> formatSpecificMenu(foodMenu)
            EntityName.DRINK -> formatSpecificMenu(drinkMenu)
            EntityName.DESSERT -> formatSpecificMenu(dessertMenu)
        }
    }

    /**
     * Same functionality as fillMenu but for specific entity menu.
     */
    private fun fillSpecificMenu(menu: LinkedHashMap<String, ArrayList<String>>, jsonRaw: String?) {
        try {
            var jsonArray = JSONArray(jsonRaw)
            for (i in 0 until jsonArray.length()) {
                var jsonObject = jsonArray.getJSONObject(i)
                var menuCategoryName = jsonObject.getString("value")
                var dishJsonArray = jsonObject.getJSONArray("synonyms")

                var dishArray = ArrayList<String>()
                for (j in 0 until dishJsonArray.length()) {
                    var dishName = dishJsonArray.getString(j)
                    dishArray.add(dishName)
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
    private fun formatSpecificMenu(menu: LinkedHashMap<String, ArrayList<String>>): String {
        var formattedMenu = ""
        menu.forEach { dishCategory ->
            var dishCategorySeparator = "\n" + SEPARATOR.repeat(dishCategory.key.length * 2) + "\n"
            formattedMenu += dishCategorySeparator + dishCategory.key + dishCategorySeparator
            dishCategory.value.forEach { dishName ->
                formattedMenu += "\t ► ${dishName.capitalize()}. \n"
            }
        }
        return formattedMenu
    }
}