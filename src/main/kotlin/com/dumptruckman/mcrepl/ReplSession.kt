package com.dumptruckman.mcrepl

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.conversations.Conversable
import org.bukkit.conversations.Conversation
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt

class ReplSession<User> private constructor (private val plugin: MCRepl, val user: User)
        where User : CommandSender, User : Conversable {

    private val shell = JShellEvaluator()
    private val conversation: Conversation

    init {
        conversation = ConversationFactory(plugin).withFirstPrompt(
                ReplPrompt("${ChatColor.GRAY}|  You will not see chat messages while using the REPL.\n" +
                        "|  Type #exit to quit the REPL at any time."))
                .withModality(user.hasPermission("mcrepl.modal"))
                .withLocalEcho(false)
                .buildConversation(user)
        conversation.begin()
    }

    fun endSession() {
        conversation.abandon()
        shell.close()
        user.sendMessage("${ChatColor.GRAY}|  Goodbye!")
    }

    companion object {

        @JvmStatic
        fun <User>startSession(plugin: MCRepl, user: User): ReplSession<User> where User : CommandSender, User : Conversable {
            return ReplSession(plugin, user)
        }
    }

    inner class ReplPrompt(private val promptText: String) : StringPrompt() {

        override fun getPromptText(context: ConversationContext): String = promptText

        override fun acceptInput(context: ConversationContext, input: String): Prompt? {
            if (input == "#exit") {
                plugin.endRepl(user)
                return Prompt.END_OF_CONVERSATION
            }

            val result = shell.eval(input)

            val nextPromptText = StringBuilder()

            if (user is ConsoleCommandSender) {
                nextPromptText.append("\n")
            }

            if (shell.isHoldingIncompleteScript()) {
                nextPromptText.append("${ChatColor.AQUA}...> ${ChatColor.RESET}$input")
            } else {
                nextPromptText.append("${ChatColor.AQUA}mcrepl> ${ChatColor.RESET}$input")
            }

            if (result != null) {
                nextPromptText.append("\n${ChatColor.GRAY}$result")
            }

            return ReplPrompt(nextPromptText.toString())
        }
    }
}