package com.mahocommerce.maho.cli

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowManager
import com.mahocommerce.maho.lsp.MahoSettings

@Service(Service.Level.PROJECT)
class MahoCommandRunner(private val project: Project) {

    fun run(command: MahoCommand, paramValues: Map<String, String> = emptyMap()) {
        val basePath = project.basePath ?: return
        val phpCommand = MahoSettings.getInstance(project).state.phpCommand.orEmpty().ifEmpty { "php" }
        val parts = phpCommand.trim().split("\\s+".toRegex())

        val args = mutableListOf<String>()
        args.addAll(parts)
        args.add("$basePath/maho")
        args.add(command.cliCommand)

        // Add option-based params (--name=value)
        val stdinValues = mutableListOf<String>()
        for (param in command.parameters) {
            val value = paramValues[param.name] ?: param.default ?: continue
            if (value.isBlank() && !param.required) continue
            if (param.isOption) {
                args.add("${param.name}=$value")
            } else {
                stdinValues.add(value)
            }
        }

        val commandLine = GeneralCommandLine(args)
            .withWorkDirectory(basePath)
            .withCharset(Charsets.UTF_8)

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Maho") ?: return
        toolWindow.activate {
            val consoleView = MahoConsoleManager.getInstance(project).getConsoleView()

            consoleView.clear()
            consoleView.print("$ maho ${command.cliCommand}", ConsoleViewContentType.SYSTEM_OUTPUT)
            if (paramValues.isNotEmpty()) {
                val displayParams = paramValues.entries.joinToString(" ") { (k, v) ->
                    if (command.parameters.find { it.name == k }?.isPassword == true) "$k=***" else "$k=$v"
                }
                consoleView.print(" $displayParams", ConsoleViewContentType.SYSTEM_OUTPUT)
            }
            consoleView.print("\n\n", ConsoleViewContentType.SYSTEM_OUTPUT)

            val handler = OSProcessHandler(commandLine)

            handler.addProcessListener(object : ProcessListener {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    consoleView.print(event.text, ConsoleViewContentType.NORMAL_OUTPUT)
                }

                override fun processTerminated(event: ProcessEvent) {
                    val exitCode = event.exitCode
                    val type = if (exitCode == 0) ConsoleViewContentType.SYSTEM_OUTPUT
                               else ConsoleViewContentType.ERROR_OUTPUT
                    consoleView.print("\nProcess finished with exit code $exitCode\n", type)

                    if (command.refreshVfs) {
                        LocalFileSystem.getInstance().refresh(true)
                    }
                }
            })

            // Feed stdin values for interactive commands
            if (stdinValues.isNotEmpty()) {
                handler.startNotify()
                val outputStream = handler.process.outputStream
                for (value in stdinValues) {
                    outputStream.write("$value\n".toByteArray())
                    outputStream.flush()
                }
            } else {
                handler.startNotify()
            }
        }
    }

    companion object {
        fun getInstance(project: Project): MahoCommandRunner =
            project.getService(MahoCommandRunner::class.java)
    }
}
