package com.dumptruckman.mcrepl

import org.bukkit.command.CommandSender
import java.io.OutputStream
import java.io.PrintStream

class MessagePrintStream(private val receiver: CommandSender) : PrintStream(object : OutputStream() {
    override fun write(b: Int) { }
}) {
    override fun print(s: String) {
        kotlin.io.println("sending: " + s)
        receiver.sendMessage(cleanupMessage(s))
    }

    override fun println() {
        kotlin.io.println("sending: \\n")
        receiver.sendMessage("")
    }

    override fun printf(format: String, vararg args: Any): PrintStream {
        val message = String.format(format, *args);
        kotlin.io.println("sending: " + message)
        receiver.sendMessage(cleanupMessage(message))
        return this
    }

    private fun cleanupMessage(message: String): String {
        return message.replace(System.getProperty("line.separator"), "\n")
    }
}
