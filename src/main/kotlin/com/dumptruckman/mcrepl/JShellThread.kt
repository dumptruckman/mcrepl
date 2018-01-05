package com.dumptruckman.mcrepl

import jdk.jshell.tool.JavaShellToolBuilder
import org.bukkit.command.CommandSender
import java.io.PrintStream

internal class JShellThread(private val mcRepl: MCRepl, private val user: CommandSender) : Thread() {

    val messageInputStream = ConversationInputStream()

    init {
        isDaemon = true
    }

    override fun run() {
        JavaShellToolBuilder.builder().`in`(messageInputStream, null).out(MessagePrintStream(user)).run()
        messageInputStream.close()
        mcRepl.endRepl(user)
    }
}
