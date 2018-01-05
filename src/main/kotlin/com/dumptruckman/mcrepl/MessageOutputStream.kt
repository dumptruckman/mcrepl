package com.dumptruckman.mcrepl

import org.bukkit.command.CommandSender
import java.io.OutputStream

class MessageOutputStream(private val receiver: CommandSender) : OutputStream() {

    val bytes = mutableListOf<Byte>()

    override fun write(b: Int) {
        bytes.add(b.toByte());
    }

    override fun flush() {
        receiver.sendMessage(String(bytes.toByteArray()))
        bytes.clear()
    }
}