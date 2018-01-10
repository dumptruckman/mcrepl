package com.dumptruckman.mcrepl

import java.util.Scanner

class ReplMain {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val shell = JShellEvaluator()

            println("Type #exit to quit")

            Scanner(System.`in`).use { scanner ->
                while(true) {
                    if (shell.isHoldingIncompleteScript()) {
                        print(" ...> ")
                    } else {
                        print("repl> ")
                    }
                    val input = scanner.nextLine()
                    if ("#exit" == input) {
                        break;
                    }

                    try {
                        val res = shell.eval(input)
                        if (res != null) {
                            println(res)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            shell.close()
        }
    }
}
