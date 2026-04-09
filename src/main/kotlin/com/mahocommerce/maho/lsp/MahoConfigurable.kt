package com.mahocommerce.maho.lsp

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.AlignX
import com.mahocommerce.maho.database.MahoDatabaseConfigurator

class MahoConfigurable(private val project: Project) : BoundConfigurable("Maho") {

    private val settings get() = MahoSettings.getInstance(project)

    private var phpCommand: String
        get() = settings.state.phpCommand ?: "php"
        set(value) { settings.state.phpCommand = value }

    private var autoConfigureDatabase: Boolean
        get() = settings.state.autoConfigureDatabase
        set(value) { settings.state.autoConfigureDatabase = value }

    override fun createPanel() = panel {
        group("LSP Server") {
            row("PHP command:") {
                textField()
                    .align(AlignX.FILL)
                    .bindText(::phpCommand)
            }
            row("") {
                text("How to invoke PHP, e.g. <code>php</code>, <code>/usr/local/bin/php</code>, or <code>docker exec mycontainer php</code>")
            }
        }
        group("Database") {
            row {
                checkBox("Auto-configure database in Database Explorer")
                    .bindSelected(::autoConfigureDatabase)
            }
            row("") {
                text("Reads connection details from <code>app/etc/local.xml</code> and creates a data source automatically")
            }
        }
    }

    override fun apply() {
        super.apply()
        LspServerManager.getInstance(project)
            .stopAndRestartIfNeeded(MahoLspServerSupportProvider::class.java)

        val basePath = project.basePath
        if (basePath != null && settings.state.autoConfigureDatabase) {
            MahoDatabaseConfigurator.configureDatabase(project, basePath)
        }
    }
}
