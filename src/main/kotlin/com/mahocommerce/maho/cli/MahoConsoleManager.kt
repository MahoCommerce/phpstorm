package com.mahocommerce.maho.cli

import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.execution.impl.ConsoleViewImpl

@Service(Service.Level.PROJECT)
class MahoConsoleManager(private val project: Project) : Disposable {

    private var consoleView: ConsoleView? = null

    fun getConsoleView(): ConsoleView {
        return consoleView ?: createConsoleView().also { consoleView = it }
    }

    private fun createConsoleView(): ConsoleView {
        val console = ConsoleViewImpl(project, true)
        console.print("Maho CLI\n\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        return console
    }

    override fun dispose() {
        consoleView?.dispose()
        consoleView = null
    }

    companion object {
        fun getInstance(project: Project): MahoConsoleManager =
            project.getService(MahoConsoleManager::class.java)
    }
}
