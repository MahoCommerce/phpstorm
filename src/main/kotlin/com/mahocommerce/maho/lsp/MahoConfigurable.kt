package com.mahocommerce.maho.lsp

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class MahoConfigurable(private val project: Project) : BoundConfigurable("Maho") {

    private val settings get() = MahoSettings.getInstance(project)

    override fun createPanel() = panel {
        group("LSP Server") {
            row("PHP command:") {
                textField()
                    .bindText(settings.state::phpCommand)
                    .comment("How to invoke PHP (e.g. <code>php</code>, <code>/usr/local/bin/php</code>, <code>docker exec mycontainer php</code>)")
            }
        }
    }

    override fun apply() {
        super.apply()
        LspServerManager.getInstance(project)
            .stopAndRestartIfNeeded(MahoLspServerSupportProvider::class.java)
    }
}
