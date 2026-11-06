package com.mahocommerce.maho.cli

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.platform.lsp.api.LspClientManager
import com.mahocommerce.maho.lsp.MahoDependencyCheck
import com.mahocommerce.maho.lsp.MahoLspIntegrationProvider
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

        var title = "maho ${command.cliCommand}"
        if (paramValues.isNotEmpty()) {
            title += " " + paramValues.entries.joinToString(" ") { (k, v) ->
                if (command.parameters.find { it.name == k }?.isPassword == true) "$k=***" else "$k=$v"
            }
        }

        runInConsole(title, GeneralCommandLine(args).withWorkDirectory(basePath), stdinValues) {
            if (command.refreshVfs) {
                LocalFileSystem.getInstance().refresh(true)
            }
        }
    }

    /** Runs `composer require --dev react/stream` in the Maho tool window, then starts the LSP server. */
    fun installReactStream() {
        val basePath = project.basePath ?: return
        val commandLine = GeneralCommandLine(MahoDependencyCheck.INSTALL_COMMAND.split(" "))
            .withWorkDirectory(basePath)

        runInConsole(MahoDependencyCheck.INSTALL_COMMAND, commandLine) { exitCode ->
            LocalFileSystem.getInstance().refresh(true)
            if (exitCode == 0 && MahoDependencyCheck.isReactStreamInstalled(basePath)) {
                ApplicationManager.getApplication().invokeLater {
                    if (!project.isDisposed) {
                        LspClientManager.getInstance(project)
                            .startClientsIfNeeded(MahoLspIntegrationProvider::class.java)
                    }
                }
            }
        }
    }

    private fun runInConsole(
        title: String,
        commandLine: GeneralCommandLine,
        stdinValues: List<String> = emptyList(),
        onTerminated: (exitCode: Int) -> Unit = {},
    ) {
        commandLine.withCharset(Charsets.UTF_8)

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Maho") ?: return
        toolWindow.activate {
            val consoleView = MahoConsoleManager.getInstance(project).getConsoleView()

            consoleView.clear()
            consoleView.print("$ $title\n\n", ConsoleViewContentType.SYSTEM_OUTPUT)

            val handler = try {
                OSProcessHandler(commandLine)
            } catch (e: Exception) {
                consoleView.print("${e.message}\n", ConsoleViewContentType.ERROR_OUTPUT)
                return@activate
            }

            handler.addProcessListener(object : ProcessListener {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    consoleView.print(event.text, ConsoleViewContentType.NORMAL_OUTPUT)
                }

                override fun processTerminated(event: ProcessEvent) {
                    val exitCode = event.exitCode
                    val type = if (exitCode == 0) ConsoleViewContentType.SYSTEM_OUTPUT
                               else ConsoleViewContentType.ERROR_OUTPUT
                    consoleView.print("\nProcess finished with exit code $exitCode\n", type)
                    onTerminated(exitCode)
                }
            })

            handler.startNotify()

            // Feed stdin values for interactive commands
            if (stdinValues.isNotEmpty()) {
                val outputStream = handler.process.outputStream
                for (value in stdinValues) {
                    outputStream.write("$value\n".toByteArray())
                    outputStream.flush()
                }
            }
        }
    }

    companion object {
        fun getInstance(project: Project): MahoCommandRunner =
            project.getService(MahoCommandRunner::class.java)
    }
}
