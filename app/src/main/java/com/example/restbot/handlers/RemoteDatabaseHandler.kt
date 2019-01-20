package com.example.restbot.handlers

import android.util.Log
import com.example.restbot.asynctasks.EntityManagementTask
import com.example.restbot.asynctasks.EntityName
import com.example.restbot.asynctasks.EntityQuery
import com.example.restbot.asynctasks.EntityQueryType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
            val jsonObject = getRawJSONFromChildren(entity.children)
            var entityName = jsonObject["name"].toString()
            var entityJSON = jsonObject.toString()

            // Uploading firebase database to dialogflow database
            EntityManagementTask().execute(EntityQuery(EntityQueryType.PUT_ENTRIES,
                    EntityName.valueOf(entityName.toUpperCase()), null, entityJSON))

            Log.d(TAG, "Updated entity $entityName")
        }
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
                                subentriesJsonArray.put(subEntry.value)
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

}