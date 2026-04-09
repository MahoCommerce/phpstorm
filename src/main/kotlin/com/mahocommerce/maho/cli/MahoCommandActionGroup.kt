package com.mahocommerce.maho.cli

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class MahoCommandActionGroup : ActionGroup() {

    private val actions: Array<AnAction> by lazy {
        MahoCommandRegistry.commands.map { MahoCommandAction(it) }.toTypedArray()
    }

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

    override fun getChildren(e: AnActionEvent?): Array<AnAction> = actions
}
