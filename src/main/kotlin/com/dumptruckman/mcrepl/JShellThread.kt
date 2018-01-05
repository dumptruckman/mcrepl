package com.dumptruckman.mcrepl

import jdk.jshell.tool.JavaShellToolBuilder
import org.bukkit.command.CommandSender
import java.io.PrintStream

internal class JShellThread(private val mcRepl: MCRepl, private val user: CommandSender) : Thread() {

    val messageInputStream = ConversationInputStream()
    val messageOutputStream = MessageOutputStream(user)

    init {
        isDaemon = true
    }

    override fun run() {
        val out = PrintStream(messageOutputStream, true)
        JavaShellToolBuilder.builder().`in`(messageInputStream, null).out(out).run()
        mcRepl.endRepl(user)
    }
}
