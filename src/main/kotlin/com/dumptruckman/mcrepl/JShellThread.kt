package com.dumptruckman.mcrepl

import jdk.jshell.tool.JavaShellToolBuilder
import org.bukkit.command.CommandSender

internal class JShellThread(private val mcRepl: MCRepl, private val user: CommandSender) : Thread() {

    val messageInputStream = ConversationInputStream()

    init {
        isDaemon = true
    }

    override fun run() {
        JavaShellToolBuilder.builder()
                .`in`(messageInputStream, null)
                .out(MessagePrintStream(user))
                .run("-execution", "local")
        messageInputStream.close()
        mcRepl.endRepl(user)
    }
}
