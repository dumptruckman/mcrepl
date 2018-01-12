package com.dumptruckman.mcrepl

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.conversations.Conversable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap

class MCRepl : JavaPlugin(), Listener {

    private val activeShells: MutableMap<Conversable, ReplSession> = ConcurrentHashMap()

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Conversable) {
            if (activeShells.containsKey(sender)) {
                sender.sendMessage("You already have an active REPL running.")
            } else {
                startRepl(sender)
            }
        } else {
            sender.sendMessage("Only players and console may start a REPL instance.")
        }
        return true
    }

    fun startRepl(user: Conversable) {
        if (activeShells.containsKey(user)) return

        activeShells[user] = ReplSession.startSession(this, user)

        logger.info("Started REPL for $user")
    }

    internal fun endRepl(user: Conversable) {
        val session = activeShells.remove(user)
        if (session != null) {
            session.endSession()
            logger.info("Ended REPL for $user")
        }
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        endRepl(event.player)
    }
}