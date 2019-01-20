package com.example.restbot.handlers

import android.util.Log
import com.example.restbot.asynctasks.EntityManagementTask
import com.example.restbot.asynctasks.EntityName
import com.example.restbot.asynctasks.EntityQuery
import com.example.restbot.asynctasks.EntityQueryType
import com.google.firebase.database.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Object that connects Firebase with Dialogflow.
 * It updates dialog flow DB when any change in firebase is produced.
 */
object RemoteDatabaseHandler : ValueEventListener {

    private val TAG = "RemoteDatabaseHandler"

    private var firebaseDB = FirebaseDatabase.getInstance()
    private var firebaseDBReference = firebaseDB.reference

    init {
        firebaseDBReference.addValueEventListener(this)
    }

    /**
     * Method that updates dialogflow DB when any change in firebase is produced.
     */
    override fun onDataChange(dataSnapshot: DataSnapshot) {
        dataSnapshot.child("entities").children.forEach { entity ->
            val jsonObjectRaw = getRawJSONFromChildren(entity.children)
            val jsonObjectWithAttributes = getRawJSONFromChildrenWithAttributes(entity.children)
            var entityName = EntityName.valueOf(jsonObjectRaw["name"].toString().toUpperCase())

            Log.d(TAG, "UPLOADING ENTITY $entityName -> $jsonObjectWithAttributes")

            // Updating local database with firebase one
            LocalDatabaseHandler.fillMenu(entityName, jsonObjectWithAttributes.toString())

            // Updating dialogflow database with firebase one
            EntityManagementTask().execute(
                    EntityQuery(EntityQueryType.PUT_ENTRIES, entityName, null, jsonObjectRaw.toString())
            )

            Log.d(TAG, "UPDATED ENTITY $entityName")
        }
    }

    /**
     * Construct a json object from the different entities of the firebase database.
     * This is with the aim of uploading the json to dialog flow by using put http requests.
     */
    private fun getRawJSONFromChildrenWithAttributes(entities: Iterable<DataSnapshot>): JSONObject {
        var jsonObject = JSONObject()
        entities.forEach {
            if (it.key != "entries") {
                jsonObject.put(it.key, it.value.toString())
            } else { // Entries case
                var entriesJsonArray = JSONArray()
                it.children.forEach { fakeEntry ->
                    var entryJsonObject = JSONObject()
                    fakeEntry.children.forEach { entry ->
                        if (entry.key != "synonyms") {
                            entryJsonObject.put(entry.key, entry.value.toString())
                        } else {
                            var subentriesJsonArray = JSONArray()
                            entry.children.forEach { subEntry ->
                                var dishJsonObject = JSONObject()
                                dishJsonObject.put("name", subEntry.child("name").value)
                                dishJsonObject.put("price", subEntry.child("price").value)
                                subentriesJsonArray.put(dishJsonObject)
                            }
                            entryJsonObject.put(entry.key, subentriesJsonArray)
                        }
                    }
                    entriesJsonArray.put(entryJsonObject)
                }
                jsonObject.put(it.key, entriesJsonArray)
            }
        }
        return jsonObject
    }

    /**
     * Construct a json object from the different entities of the firebase database.
     * This is with the aim of uploading the json to dialog flow by using put http requests.
     */
    private fun getRawJSONFromChildren(entities: Iterable<DataSnapshot>): JSONObject {
        var jsonObject = JSONObject()
        entities.forEach {
            if (it.key != "entries") {
                jsonObject.put(it.key, it.value.toString())
            } else { // Entries case
                var entriesJsonArray = JSONArray()
                it.children.forEach { fakeEntry ->
                    var entryJsonObject = JSONObject()
                    fakeEntry.children.forEach { entry ->
                        if (entry.key != "synonyms") {
                            entryJsonObject.put(entry.key, entry.value.toString())
                        } else {
                            var subentriesJsonArray = JSONArray()
                            entry.children.forEach { subEntry ->
                                subentriesJsonArray.put(subEntry.child("name").value)
                            }
                            entryJsonObject.put(entry.key, subentriesJsonArray)
                        }
                    }
                    entriesJsonArray.put(entryJsonObject)
                }
                jsonObject.put(it.key, entriesJsonArray)
            }
        }
        return jsonObject
    }

    /**
     * Method of ValueEventListener not implemented.
     */
    override fun onCancelled(databaseError: DatabaseError) {
    }

    fun addNewOrder(currentOrder: ArrayList<IntentHandler.DishOrdered>) {
        var newOrderRef = firebaseDBReference.child("orders").push()
        newOrderRef.setValue(currentOrder)
    }

}