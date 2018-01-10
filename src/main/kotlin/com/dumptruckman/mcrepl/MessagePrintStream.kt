package com.dumptruckman.mcrepl

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.io.OutputStream
import java.io.PrintStream

class MessagePrintStream(private val receiver: CommandSender) : PrintStream(NullOutputStream) {

    override fun print(s: String) {
        sendMessage(cleanupMessage(s))
    }

    override fun println() {
        sendMessage("")
    }

    override fun printf(format: String, vararg args: Any): PrintStream {
        val message = String.format(format, *args);
        sendMessage(cleanupMessage(message))
        return this
    }

    private fun cleanupMessage(message: String): String {
        return message
                .replace(System.getProperty("line.separator"), "\n")
                .replace("\t", "     ")
    }

    private fun sendMessage(message: String) {
        receiver.sendMessage("${ChatColor.GRAY}$message")
    }

    object NullOutputStream : OutputStream() {
        override fun write(b: Int) { }

        override fun write(b: ByteArray?) { }

        override fun write(b: ByteArray?, off: Int, len: Int) { }
    }
}
