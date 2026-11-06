package com.mahocommerce.maho.lsp

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.mahocommerce.maho.cli.MahoCommandRunner
import java.awt.datatransfer.StringSelection
import java.io.File

/**
 * `maho dev:lsp:start` and `maho dev:mcp:start` need `react/stream`, an optional dev dependency of Maho.
 * A store project made from maho-starter does not have it, so the plugin checks for it before it starts
 * the LSP server, and shows one notification per project session when it is missing.
 */
internal object MahoDependencyCheck {

    const val INSTALL_COMMAND = "composer require --dev react/stream"

    private val NOTIFIED = Key.create<Boolean>("maho.reactStream.notified")
    private val VENDOR_DIR = Regex("\"vendor-dir\"\\s*:\\s*\"([^\"]+)\"")

    fun isReactStreamInstalled(basePath: String): Boolean =
        File(vendorDir(basePath), "react/stream/composer.json").isFile

    /** Returns true when react/stream is installed. Otherwise notifies (once per project) and returns false. */
    fun checkOrNotify(project: Project, basePath: String): Boolean {
        if (isReactStreamInstalled(basePath)) {
            project.putUserData(NOTIFIED, null)
            return true
        }
        if (project.getUserData(NOTIFIED) == true) return false
        project.putUserData(NOTIFIED, true)
        thisLogger().info("react/stream not found in ${vendorDir(basePath)}: the Maho LSP server is not started")

        NotificationGroupManager.getInstance()
            .getNotificationGroup("Maho")
            .createNotification(
                "Maho code intelligence needs react/stream",
                "The Maho LSP and MCP servers need <code>react/stream</code>, an optional dev dependency of Maho. " +
                    "Run <code>$INSTALL_COMMAND</code> in the project root.",
                NotificationType.WARNING,
            )
            .addAction(NotificationAction.createSimpleExpiring("Install with Composer") {
                MahoCommandRunner.getInstance(project).installReactStream()
            })
            .addAction(NotificationAction.createSimple("Copy command") {
                CopyPasteManager.getInstance().setContents(StringSelection(INSTALL_COMMAND))
            })
            .notify(project)
        return false
    }

    private fun vendorDir(basePath: String): File {
        val composerJson = File(basePath, "composer.json")
        val configured = if (composerJson.isFile) {
            VENDOR_DIR.find(composerJson.readText())?.groupValues?.get(1)
        } else null
        val dir = File(configured ?: "vendor")
        return if (dir.isAbsolute) dir else File(basePath, dir.path)
    }
}
