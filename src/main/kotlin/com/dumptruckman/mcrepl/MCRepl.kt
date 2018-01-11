package com.dumptruckman.mcrepl

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.conversations.Conversable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap

class MCRepl : JavaPlugin(), Listener {

    private val activeShells: MutableMap<CommandSender, ReplSession<*>> = ConcurrentHashMap()

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (activeShells.containsKey(sender)) {
            sender.sendMessage("You already have an active REPL running.")
        } else {
            if (sender is Player) {
                startRepl(sender)
            } else if (sender is ConsoleCommandSender) {
                startRepl(sender)
            } else {
                sender.sendMessage("Only in game players and console may start a REPL instance.")
            }
        }
        return true
    }

    fun <User>startRepl(user: User) where User : CommandSender, User : Conversable {
        if (activeShells.containsKey(user)) return

        activeShells[user] = ReplSession.startSession(this, user)

        logger.info("Started REPL for $user")
    }

    internal fun endRepl(user: CommandSender) {
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