package com.example.chatbot

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

/**
 * Class that extends from MesageAdapter in order to display messages in ListView
 */
class MessageAdapter(private var context: Context) : BaseAdapter() {

    private var messages: ArrayList<Message> = ArrayList()

    /**
     * Add a message to the list of message and notifies the change,
     * in order to execute getView asynchronously, because of the BaseAdapter interface.
     */
    fun addMessage(message: Message) {
        this.messages.add(message)
        notifyDataSetChanged()
    }

    /**
     * It handles the creation of a single List view row,
     * i.e: inflate a message and add it to the list view.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val messageInflater: LayoutInflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val currentMessage = getItem(position)

        // Depending on the type of messages, it will inflate the incoming message template or the outgoing one, i.e:
        // It will be rendered by creating a view object in memory.
        val convertViewModified = if (currentMessage.incomingMessage)
            messageInflater.inflate(R.layout.incoming_message, null)
            else
            messageInflater.inflate(R.layout.outgoing_message, null)

        // Change the message body of the new inflated message.
        val messageBody = convertViewModified.findViewById(R.id.message_body) as TextView
        messageBody.text = currentMessage.text

        // Return the view where all the changes have been done.
        return convertViewModified
    }

    /**
     * Returns the item in the position.
     */
    override fun getItem(position: Int): Message {
        return messages[position]
    }

    /**
     * Returns the index of the element.
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Returns the current number of messages.
     */
    override fun getCount(): Int {
        return messages.size
    }
}