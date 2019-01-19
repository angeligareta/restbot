package com.example.restbot.asynctasks

import android.os.AsyncTask
import android.util.Log
import com.example.restbot.handlers.MenuHandler
import com.google.gson.JsonParser
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


// TODO: Save it in a secure place
const val DEVELOPER_ACCESS_TOKEN = "d9967b2ad29e49a490c9d46153443350"
const val TAG = "EntityManagementTask"

/**
 * Enumeration that represent the different types of Entity Queries, such as GET_ENTRIES.
 */
enum class EntityQueryType(var isGetRequest: Boolean) {
    GET_ENTRIES_OF_ENTITY(true),
    GET_ENTRIES_OF_SUBENTITY(true)
    //SET_ENTRIES()
}

/**
 * Enumeration that represent the possible names of an entity.
 */
enum class EntityName(val entityName : String) {
    FOOD("food"),
    DRINK("drink"),
    DESSERT("dessert")
}

/**
 * Class that represent the type of an Entity Query, and the name and values of the sub-entity is being queried.
 */
class EntityQuery(val entityQueryType: EntityQueryType, val entityName: EntityName, val subEntityName: String?)

/**
 * Data Structure for the result of an entity management task.
 */
class EntityManagementTaskResult(val entityQuery: EntityQuery, var jsonRaw: String?)

/**
 * AsyncTask that executes entity queries, establishing a connection depending on the query and resolving the request.
 */
class EntityManagementTask : AsyncTask<EntityQuery, Void, ArrayList<EntityManagementTaskResult>>() {

    override fun doInBackground(vararg entityQueries: EntityQuery?): ArrayList<EntityManagementTaskResult> {
        var queriesResponses = ArrayList<EntityManagementTaskResult>(entityQueries.size)

        for (entityQuery in entityQueries) {
            if (entityQuery != null) {
                var entityManagementTaskResult = EntityManagementTaskResult(entityQuery, null)
                Log.d(TAG, "Requesting...")

                if (entityQuery.entityQueryType == EntityQueryType.GET_ENTRIES_OF_SUBENTITY) {
                    // Establish get connection with the entity name
                    val connection = getGetConnection(entityQuery.entityName)
                    connection.connect()

                    // Get full JSON of the Entity
                    val responseReader = BufferedReader(InputStreamReader(connection.inputStream))
                    var jsonObjectLine = responseReader.readLine()

                    var jsonObjectString = "{ "
                    while (jsonObjectLine != null) {
                        jsonObjectLine = responseReader.readLine()
                        if (jsonObjectLine != null) {
                            jsonObjectString += jsonObjectLine
                        }
                    }

                    // For that entity, only take the dishes of a sub-entity
                    val jsonObject = JSONObject(JsonParser().parse(jsonObjectString).toString())
                    val subEntitiesJsonArray = jsonObject.getJSONArray("entries")
                    for (i in 0 until subEntitiesJsonArray.length()) {
                        var subEntity = subEntitiesJsonArray[i] as JSONObject

                        var subEntityName = subEntity.getString("value")
                        if (subEntityName == entityQuery.subEntityName) {
                            entityManagementTaskResult.jsonRaw = subEntity.getString("synonyms")
                        }
                    }
                }
                else if (entityQuery.entityQueryType == EntityQueryType.GET_ENTRIES_OF_ENTITY) {
                    // Establish get connection with the entity name
                    val connection = getGetConnection(entityQuery.entityName)
                    connection.connect()

                    // Get full JSON of the Entity
                    val responseReader = BufferedReader(InputStreamReader(connection.inputStream))
                    var jsonObjectLine = responseReader.readLine()

                    var jsonObjectString = "{ "
                    while (jsonObjectLine != null) {
                        jsonObjectLine = responseReader.readLine()
                        if (jsonObjectLine != null) {
                            jsonObjectString += jsonObjectLine
                        }
                    }

                    // For that entity, only take the entries
                    val jsonObject = JSONObject(JsonParser().parse(jsonObjectString).toString())
                    val subEntitiesJsonArray = jsonObject.getJSONArray("entries")
                    entityManagementTaskResult.jsonRaw = subEntitiesJsonArray.toString()
                }
                /*else if (entityQuery.entityType == EntityQueryType.SET_ENTRIES) {
                    val connection = getPostConnection()

                    val writer = OutputStreamWriter(connection.outputStream)
                    writer.write("[ " + entityQuery.subEntityValues.toString() + " ]")
                    writer.flush()
                    writer.close()

                    //TODO -> USE EntityManagementTask().execute(EntityQuery(EntityQueryType.GET_ENTRIES, "meat", null))

                    connection.connect()
                    queryResponse = connection.responseMessage
                }*/

                Log.d(TAG, "RESPONSE -> ${entityManagementTaskResult.jsonRaw}")
                queriesResponses.add(entityManagementTaskResult)
            }
        }

        return queriesResponses
    }

    /**
     * When the execution is finished, this method uses the MenuHandler object
     * to fill the menus if the petitions were of get type.
     */
    override fun onPostExecute(entityManagementTaskResults: ArrayList<EntityManagementTaskResult>?) {
        entityManagementTaskResults?.forEach {
            if (it.entityQuery.entityQueryType.isGetRequest) {
                MenuHandler.fillMenu(it.entityQuery.entityName, it.jsonRaw)
            }
        }
    }

    /**
     * Function that returns a connection for getting entries of an entity.
     */
    private fun getGetConnection(entityName: EntityName): HttpURLConnection {
        // TODO : Language can be changed
        var connection = URL("https://api.api.ai/v1/entities/${entityName.entityName}").
                openConnection() as HttpURLConnection

        connection.readTimeout = 15000
        connection.connectTimeout = 15000
        connection.setRequestProperty("Content-Type", "application/json")
        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Bearer $DEVELOPER_ACCESS_TOKEN")
        connection.doInput = true

        return connection
    }

    /**
     * Function that returns a connection for setting entries of an entity.
     */
    private fun getPostConnection() : HttpURLConnection {
        // TODO : Language can be changed
        var connection = URL("https://api.api.ai/v1/entities/food/entries").
                openConnection() as HttpURLConnection

        connection.readTimeout = 15000
        connection.connectTimeout = 15000
        connection.setRequestProperty("Content-Type", "application/json")
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $DEVELOPER_ACCESS_TOKEN")
        connection.doOutput = true

        return connection
    }

}
