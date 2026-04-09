package com.mahocommerce.maho.lsp

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(name = "MahoSettings", storages = [Storage("maho.xml")])
class MahoSettings : SimplePersistentStateComponent<MahoSettingsState>(MahoSettingsState()) {

    companion object {
        fun getInstance(project: Project): MahoSettings =
            project.getService(MahoSettings::class.java)
    }
}

class MahoSettingsState : BaseState() {
    var phpCommand by string("php")
    var autoConfigureDatabase by property(true)
}
