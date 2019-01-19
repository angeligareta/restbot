package com.example.restbot

import android.os.AsyncTask
import android.util.Log
import com.google.gson.JsonParser
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


// TODO: Save it in a secure place
const val DEVELOPER_ACCESS_TOKEN = "d9967b2ad29e49a490c9d46153443350"
const val TAG = "EntityManagementTask"

/**
 * Enumeration that represent the different types of Entity Queries, such as GET_ENTRIES.
 */
enum class EntityQueryType(val entityType: String) {
    GET_ENTRIES("getEntries"),
    SET_ENTRIES("setEntries")
}

/**
 * Class that represent the type of an Entity Query, and the name and values of the sub-entity is being queried.
 */
class EntityQuery(val entityType: EntityQueryType, val subEntityName: String, val subEntityValues: JSONObject?)

/**
 * AsyncTask that executes entity queries, establishing a connection depending on the query and resolving the request.
 */
class EntityManagementTask : AsyncTask<EntityQuery, Void, ArrayList<String?>>() {

    override fun doInBackground(vararg entityQueries: EntityQuery?): ArrayList<String?> {
        var queriesResponses = ArrayList<String?>(entityQueries.size)

        for (entityQuery in entityQueries) {
            if (entityQuery != null) {
                var queryResponse: String? = null
                Log.d(TAG, "Requesting...")

                if (entityQuery.entityType == EntityQueryType.GET_ENTRIES) {
                    val connection = getGetConnection()
                    connection.connect()

                    val responseReader = BufferedReader(InputStreamReader(connection.inputStream))
                    var jsonObjectLine = responseReader.readLine()

                    var jsonObjectString = "{ "
                    while (jsonObjectLine != null) {
                        jsonObjectLine = responseReader.readLine()
                        if (jsonObjectLine != null) {
                            jsonObjectString += jsonObjectLine
                        }
                    }

                    val jsonObject = JSONObject(JsonParser().parse(jsonObjectString).toString())
                    val subEntitiesJsonArray = jsonObject.getJSONArray("entries")
                    for (i in 0 until subEntitiesJsonArray.length()) {
                        var subEntity = subEntitiesJsonArray[i] as JSONObject

                        var subEntityName = subEntity.getString("value")
                        if (subEntityName == entityQuery.subEntityName) {
                            queryResponse = subEntity.getString("synonyms")
                        }
                    }
                } else if (entityQuery.entityType == EntityQueryType.SET_ENTRIES) {
                    val connection = getPostConnection()

                    val writer = OutputStreamWriter(connection.outputStream)
                    writer.write("[ " + entityQuery.subEntityValues.toString() + " ]")
                    writer.flush()
                    writer.close()

                    //TODO -> USE EntityManagementTask().execute(EntityQuery(EntityQueryType.GET_ENTRIES, "meat", null))

                    connection.connect()
                    queryResponse = connection.responseMessage
                }
                Log.d(TAG, "RESPONSE -> $queryResponse")
                queriesResponses.add(queryResponse)
            }
        }

        return queriesResponses
    }

    /**
     * Function that returns a connection for getting entries of an entity.
     */
    private fun getGetConnection(): HttpURLConnection {
        // TODO : Language can be changed
        var connection = URL("https://api.api.ai/v1/entities/food").
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
