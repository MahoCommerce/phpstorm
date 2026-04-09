package com.mahocommerce.maho.cli

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class MahoCommandAction(
    private val command: MahoCommand,
) : AnAction(command.label) {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        val basePath = project.basePath
        e.presentation.isEnabledAndVisible = basePath != null && java.io.File(basePath, "maho").isFile
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if (command.parameters.isEmpty()) {
            MahoCommandRunner.getInstance(project).run(command)
            return
        }

        val dialog = MahoCommandDialog(project, command)
        if (dialog.showAndGet()) {
            MahoCommandRunner.getInstance(project).run(command, dialog.getParamValues())
        }
    }
}
