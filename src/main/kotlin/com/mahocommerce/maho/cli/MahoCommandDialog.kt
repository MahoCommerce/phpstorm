package com.mahocommerce.maho.cli

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JPasswordField
import javax.swing.JTextField

class MahoCommandDialog(
    project: Project,
    private val command: MahoCommand,
) : DialogWrapper(project, true) {

    private val fields = mutableMapOf<String, JTextField>()

    init {
        title = "Maho: ${command.label}"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        for (param in command.parameters) {
            row(param.label + ":") {
                val field = if (param.isPassword) JPasswordField(30) else JTextField(30)
                field.text = param.default ?: ""
                fields[param.name] = field
                cell(field).align(AlignX.FILL)
            }
        }
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return fields.values.firstOrNull()
    }

    override fun doValidate(): ValidationInfo? {
        for (param in command.parameters) {
            if (param.required) {
                val field = fields[param.name] ?: continue
                if (field.text.isNullOrBlank()) {
                    return ValidationInfo("${param.label} is required", field)
                }
            }
        }
        return null
    }

    fun getParamValues(): Map<String, String> {
        return fields.mapValues { it.value.text.trim() }
            .filterValues { it.isNotEmpty() }
    }
}
