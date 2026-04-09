package com.mahocommerce.maho.cli

data class MahoCommandParam(
    val name: String,
    val label: String,
    val required: Boolean = true,
    val default: String? = null,
    val isPassword: Boolean = false,
    val isOption: Boolean = true,
)

data class MahoCommand(
    val id: String,
    val cliCommand: String,
    val label: String,
    val parameters: List<MahoCommandParam> = emptyList(),
    val refreshVfs: Boolean = false,
)
