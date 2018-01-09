package com.dumptruckman.mcrepl

import java.io.InputStream
import java.io.Reader
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.locks.ReentrantLock

internal class MessageReader : Reader() {

    private val messageCharsQueue: Queue<Char> = LinkedList()

    private @Volatile var open = true

    private val streamLock = ReentrantLock()
    private val restLock = ReentrantLock()
    private val restStop = java.lang.Object()

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        if (off < 0 || off > cbuf.size || len < 0 ||
                off + len > cbuf.size || off + len < 0) {
            throw IndexOutOfBoundsException()
        } else if (len == 0) {
            return 0
        }
        val ret = read()
        if (ret < 0) {
            return ret;
        }
        cbuf[off] = ret.toChar()
        return 1
    }

    override fun read(): Int {
        streamLock.lock()
        if (messageCharsQueue.isNotEmpty()) {
            streamLock.unlock()
            return immediateRead()
        } else {
            streamLock.unlock()
            println("Waiting on user input")
            synchronized(restStop) {
                restStop.wait()
            }
            println("User input should be received")
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
        val char = messageCharsQueue.poll()
        val result = char.toInt()
        println("Read char '$char' as '$result'")
        streamLock.unlock()
        return result;
    }

    fun addMessage(message: String) {
        println("Adding user input: $message")
        restLock.lock()
        synchronized(restStop) {
            restStop.notify()
        }
        streamLock.lock()
        restLock.unlock()
        (message + System.getProperty("line.separator")).toCharArray().forEach { messageCharsQueue.add(it) }
        println("Added user input")
        streamLock.unlock()
    }

    override fun close() {
        println("Closing inputstream")
        restLock.lock()
        synchronized(restStop) {
            restStop.notify()
        }
        open = false
        restLock.unlock()
    }
}