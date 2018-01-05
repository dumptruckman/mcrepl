package com.dumptruckman.mcrepl

import java.io.InputStream
import java.util.LinkedList
import java.util.Queue

class ConversationInputStream : InputStream() {

    private val messageCharsQueue: Queue<Char> = LinkedList()

    override fun read(): Int = if (messageCharsQueue.isNotEmpty()) {
        synchronized(this) {
            messageCharsQueue.poll().toInt()
        }
    } else {
        -1
    }

    fun addMessage(message: String) {
        synchronized(this) {
            message.toCharArray().forEach { messageCharsQueue.add(it) }
        }
    }
}