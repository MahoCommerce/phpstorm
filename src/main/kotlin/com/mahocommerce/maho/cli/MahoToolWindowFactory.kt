package com.mahocommerce.maho.cli

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class MahoToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val consoleView = MahoConsoleManager.getInstance(project).getConsoleView()
        val content = ContentFactory.getInstance().createContent(consoleView.component, "Output", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        val basePath = project.basePath ?: return false
        return java.io.File(basePath, "maho").isFile
    }
}
