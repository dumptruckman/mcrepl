package com.dumptruckman.mcrepl

import jdk.jshell.execution.DirectExecutionControl
import jdk.jshell.spi.ExecutionControl
import jdk.jshell.spi.ExecutionControlProvider
import jdk.jshell.spi.ExecutionEnv

internal class DirectExecutionControlProvider : ExecutionControlProvider {

    override fun generate(env: ExecutionEnv?, parameters: MutableMap<String, String>?): ExecutionControl {
        return DirectExecutionControl()
    }

    override fun name(): String {
        return "direct"
    }
}