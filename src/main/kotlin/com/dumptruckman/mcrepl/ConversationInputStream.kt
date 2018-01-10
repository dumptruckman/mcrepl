package com.dumptruckman.mcrepl

import java.io.InputStream
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.locks.ReentrantLock

internal class ConversationInputStream : InputStream() {

    private val messageCharsQueue: Queue<Char> = LinkedList()

    private @Volatile var open = true

    private val streamLock = ReentrantLock()
    private val restLock = ReentrantLock()
    private val restStop = java.lang.Object()

    override fun read(): Int {
        streamLock.lock()
        if (messageCharsQueue.isNotEmpty()) {
            streamLock.unlock()
            return immediateRead()
        } else {
            streamLock.unlock()
            synchronized(restStop) {
                restStop.wait()
            }
            restLock.lock()
            restLock.unlock()
            if (open) {
                return immediateRead()
            } else {
                return -1
            }
        }
    }

    private fun immediateRead(): Int {
        streamLock.lock()
        val char = messageCharsQueue.poll() ?: throw IllegalStateException("Message queue is empty!")
        val result = char.toInt()
        streamLock.unlock()
        return result;
    }

    fun addMessage(message: String) {
        restLock.lock()
        synchronized(restStop) {
            restStop.notify()
        }
        streamLock.lock()
        restLock.unlock()
        (message + System.getProperty("line.separator")).toCharArray().forEach { messageCharsQueue.add(it) }
        streamLock.unlock()
    }

    override fun close() {
        restLock.lock()
        synchronized(restStop) {
            restStop.notify()
        }
        open = false
        restLock.unlock()
    }
}