package com.dumptruckman.mcrepl

import bsh.Interpreter
import org.bukkit.command.CommandSender

internal class JShellThread(private val mcRepl: MCRepl, private val user: CommandSender) : Thread() {

    val messageInputStream = MessageReader()

    init {
        isDaemon = true
    }

    override fun run() {
        val out = MessagePrintStream(user)
        Interpreter(messageInputStream, out, out, true).run()
        messageInputStream.close()
        mcRepl.endRepl(user)
    }
}
