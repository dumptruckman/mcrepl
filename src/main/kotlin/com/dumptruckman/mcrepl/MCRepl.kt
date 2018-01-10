package com.dumptruckman.mcrepl

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap

class MCRepl : JavaPlugin(), Listener {

    private val activeShells: MutableMap<CommandSender, JShellThread> = ConcurrentHashMap()

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (activeShells.containsKey(sender)) {
            sender.sendMessage("You already have an active REPL running.")
        } else {
            if (sender is Player) {
                startRepl(sender)
            } else {
                sender.sendMessage("Only in game players may start a REPL instance.")
            }
        }
        return true
    }

    fun startRepl(user: CommandSender) {
        if (activeShells.containsKey(user)) return

        val shellThread = JShellThread(this, user)
        activeShells[user] = shellThread

        user.sendMessage("You will not see chat messages while using the REPL.\n" +
                "All REPL commands should start with # instead of /.\n" +
                "Type #exit to quit the REPL at any time.")
        shellThread.start()
        logger.info("Started REPL for $user")
    }

    internal fun endRepl(user: CommandSender) {
        val shellThread = activeShells[user]
        if (shellThread == null) return

        activeShells.remove(user)
        logger.info("Ended REPL for $user")
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val shellThread = activeShells[event.player] ?: return

        shellThread.stop()
        shellThread.messageInputStream.close()
        endRepl(event.player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerChatLowest(event: AsyncPlayerChatEvent) {
        val shellThread = activeShells[event.player]
        if (shellThread == null) return

        var message = event.message
        if (message.startsWith("#")) {
            message = message.replaceFirst('#', '/')
        }
        shellThread.messageInputStream.addMessage(message)
        event.isCancelled = true
        event.player.sendMessage("${ChatColor.AQUA}mcrepl> ${ChatColor.RESET}$message")
//        if (message.startsWith("#exit", true)) {
//            endRepl(event.player)
//        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun onPlayerChatHighest(event: AsyncPlayerChatEvent) {
        // Remove chat messages from being sent to players that have a REPL open
        // Unless it is the sending player
        val recipients = event.recipients.iterator()
        while (recipients.hasNext()) {
            val recipient = recipients.next()
            if (activeShells.containsKey(recipient)) {
                recipients.remove()
            }
        }
    }
}