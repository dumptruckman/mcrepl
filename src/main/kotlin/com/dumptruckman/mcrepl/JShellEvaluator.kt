package com.dumptruckman.mcrepl

import jdk.jshell.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.regex.Pattern

internal class JShellEvaluator {

    companion object {
        private val linebreakPattern = Pattern.compile("\\R")
        private val lineRegex = "${System.getProperty("line.separator")}\t".toRegex()
    }

    private val shell = JShell.builder()
            .executionEngine(DirectExecutionControlProvider(), null)
            .build()

    private var buffer = StringBuilder()

    fun close() {
        shell.close()
    }

    fun isHoldingIncompleteScript(): Boolean = !buffer.isEmpty()

    /**
     * Evaluates the given script and returns a message describing the results.
     * Returns null if there is nothing notable to describe, such as when
     * entering incomplete code that is awaiting further code.
     */
    fun eval(script: String): String? {
        val events = evalAll(script)
        for (event in events) {
            if (event.exception() != null) {
                val writer = StringWriter()
                try {
                    throw event.exception()
                } catch (e: EvalException) {
                    convertJShellException(e).printStackTrace(PrintWriter(writer))
                } catch (e: Exception) {
                    e.printStackTrace(PrintWriter(writer))
                }

                return "|  ${writer.buffer.replace(lineRegex, "\n|        ")}"
            }

            if (event.status() == Snippet.Status.VALID) {
                val snippet = event.snippet()
                when (snippet) {
                    is VarSnippet -> return "${snippet.name()} ==> ${shell.varValue(snippet)}"
                    is MethodSnippet -> return "|  created method ${snippet.name()}()"
                    is TypeDeclSnippet -> return "|  created class ${snippet.name()}"
                    is ExpressionSnippet -> return ""
                }
                if (snippet is VarSnippet) {

                    //return executionControl.getActualVarValue(snippet)
                } else if (snippet is ExpressionSnippet) {
                    return ""
                    //return executionControl.getLastValue()
                }
            }

            if (event.status() == Snippet.Status.REJECTED) {
                val snippet = event.snippet()
                val diag = shell.diagnostics(snippet).findAny().get()
                val display = mutableListOf<String>()
                displayDiagnostics(snippet.source(), diag, display)
                val sb = StringBuilder().append("|  Error:")
                display.forEach({ sb.append("\n|  ").append(it) })
                return sb.toString()
            }
        }

        return null
    }

    private fun evalAll(script: String): List<SnippetEvent> {
        var fullScript = if (buffer.isEmpty()) script else buffer.toString() + script
        while (true) {
            val completionInfo = shell.sourceCodeAnalysis().analyzeCompletion(fullScript)
            if (!completionInfo.completeness().isComplete()) {
                buffer.append(script)
                return emptyList()
            }
            buffer = StringBuilder()

            val result = shell.eval(completionInfo.source())

            fullScript = completionInfo.remaining()

            if (fullScript.isEmpty()) {
                return result
            }
        }
    }

    private fun convertJShellException(original: EvalException): Exception {
        try {
            val exceptionClass = Class.forName(original.exceptionClassName)
            if (Exception::class.java.isAssignableFrom(exceptionClass)) {
                try {
                    // Try message and cause.
                    val constructor = exceptionClass.getConstructor(String::class.java, Throwable::class.java)
                    val exception = constructor.newInstance(original.message, original.cause) as Exception
                    exception.stackTrace = original.stackTrace
                    return exception
                } catch (e2: ReflectiveOperationException) {
                }

                try {
                    // Try message only.
                    val constructor = exceptionClass.getConstructor(String::class.java)
                    val exception = constructor.newInstance(original.message) as Exception
                    exception.stackTrace = original.stackTrace
                    return exception
                } catch (e2: ReflectiveOperationException) {
                }

                try {
                    // Try cause only.
                    val constructor = exceptionClass.getConstructor(Throwable::class.java)
                    val exception = constructor.newInstance(original.cause) as Exception
                    exception.stackTrace = original.stackTrace
                    return exception
                } catch (e2: ReflectiveOperationException) {
                }

                try {
                    // Try no arguments.
                    val constructor = exceptionClass.getConstructor()
                    val exception = constructor.newInstance() as Exception
                    exception.stackTrace = original.stackTrace
                    return exception
                } catch (e2: ReflectiveOperationException) {
                }

            }
        } catch (e2: ReflectiveOperationException) {
        }

        return original
    }

    private fun displayDiagnostics(source: String, diag: Diag, toDisplay: MutableList<String>) {
        for (line in diag.getMessage(null).split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) { // TODO: Internationalize
            if (!line.trim { it <= ' ' }.startsWith("location:")) {
                toDisplay.add(line)
            }
        }

        val pstart = diag.startPosition.toInt()
        val pend = diag.endPosition.toInt()
        val m = linebreakPattern.matcher(source)
        var pstartl = 0
        var pendl = -2
        while (m.find(pstartl)) {
            pendl = m.start()
            if (pendl >= pstart) {
                break
            } else {
                pstartl = m.end()
            }
        }
        if (pendl < pstart) {
            pendl = source.length
        }
        toDisplay.add(source.substring(pstartl, pendl))

        val sb = StringBuilder()
        val start = pstart - pstartl
        for (i in 0 until start) {
            sb.append(' ')
        }
        sb.append('^')
        val multiline = pend > pendl
        val end = (if (multiline) pendl else pend) - pstartl - 1
        if (end > start) {
            for (i in start + 1 until end) {
                sb.append('-')
            }
            if (multiline) {
                sb.append("-...")
            } else {
                sb.append('^')
            }
        }
        toDisplay.add(sb.toString())
    }
}
