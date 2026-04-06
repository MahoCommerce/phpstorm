package com.mahocommerce.maho.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor

class MahoLspServerDescriptor(project: Project) :
    ProjectWideLspServerDescriptor(project, "Maho Intelligence") {

    override fun isSupportedFile(file: VirtualFile): Boolean =
        file.extension?.lowercase() in MahoLspServerSupportProvider.SUPPORTED_EXTENSIONS

    override fun createCommandLine(): GeneralCommandLine {
        val basePath = project.basePath ?: error("No project base path")
        val phpCommand = MahoSettings.getInstance(project).state.phpCommand.orEmpty().ifEmpty { "php" }
        val parts = phpCommand.trim().split("\\s+".toRegex())

        return GeneralCommandLine(parts + listOf("$basePath/maho", "dev:lsp:start"))
            .withWorkDirectory(basePath)
    }
}
