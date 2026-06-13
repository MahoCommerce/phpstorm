package com.mahocommerce.maho.mcp

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.LocalFileSystem
import com.mahocommerce.maho.lsp.MahoSettings
import java.io.File

/**
 * Writes a project-level `.junie/mcp/mcp.json` entry that points Junie / AI Assistant
 * at the Maho MCP server (`php maho dev:mcp:start`).
 *
 * Only the `maho` key under `mcpServers` is managed; any other servers the user has
 * configured are preserved.
 */
class MahoMcpConfigurator : ProjectActivity {

    override suspend fun execute(project: Project) {
        val basePath = project.basePath ?: return
        if (!File(basePath, "maho").isFile) return
        if (!MahoSettings.getInstance(project).state.autoConfigureMcp) return

        configureMcp(project, basePath)
    }

    companion object {
        private const val SERVER_NAME = "maho"
        private val LOG = logger<MahoMcpConfigurator>()
        private val GSON = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

        fun configureMcp(project: Project, basePath: String) {
            val phpCommand = MahoSettings.getInstance(project).state.phpCommand.orEmpty().ifEmpty { "php" }
            val parts = phpCommand.trim().split("\\s+".toRegex())
            val command = parts.first()
            val args = parts.drop(1) + listOf("$basePath/maho", "dev:mcp:start")

            val mahoEntry = JsonObject().apply {
                addProperty("command", command)
                add("args", JsonArray().apply { args.forEach { add(it) } })
                addProperty("workingDirectory", basePath)
            }

            val file = File(basePath, ".junie/mcp/mcp.json")
            val root = readRoot(file)
            val servers = root.getAsJsonObject("mcpServers")
                ?: JsonObject().also { root.add("mcpServers", it) }
            servers.add(SERVER_NAME, mahoEntry)

            val newContent = GSON.toJson(root) + "\n"
            if (file.exists() && file.readText() == newContent) return

            file.parentFile.mkdirs()
            file.writeText(newContent)
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
            LOG.info("Configured Maho MCP server in ${file.path}")
        }

        private fun readRoot(file: File): JsonObject {
            if (!file.isFile) return JsonObject()
            return try {
                JsonParser.parseString(file.readText()).asJsonObject
            } catch (e: Exception) {
                LOG.warn("Could not parse existing ${file.path}, recreating", e)
                JsonObject()
            }
        }
    }
}
