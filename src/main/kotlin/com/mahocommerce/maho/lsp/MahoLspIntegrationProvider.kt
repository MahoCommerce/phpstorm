package com.mahocommerce.maho.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspIntegrationProvider

internal class MahoLspIntegrationProvider : LspIntegrationProvider {

    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        clientStarter: LspIntegrationProvider.LspClientStarter,
    ) {
        val ext = file.extension?.lowercase() ?: return
        if (ext !in SUPPORTED_EXTENSIONS) return

        val basePath = project.basePath ?: return
        val mahoFile = java.io.File(basePath, "maho")
        if (!mahoFile.isFile) return
        if (!MahoDependencyCheck.checkOrNotify(project, basePath)) return

        clientStarter.ensureClientStarted(MahoLspClientDescriptor(project))
    }

    companion object {
        val SUPPORTED_EXTENSIONS = setOf("php", "xml", "phtml")
    }
}
