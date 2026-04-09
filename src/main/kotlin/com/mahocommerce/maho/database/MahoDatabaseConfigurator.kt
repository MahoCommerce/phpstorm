package com.mahocommerce.maho.database

import com.intellij.database.dataSource.DatabaseDriverManager
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.mahocommerce.maho.lsp.MahoSettings
import java.io.File

class MahoDatabaseConfigurator : ProjectActivity {

    override suspend fun execute(project: Project) {
        val basePath = project.basePath ?: return
        if (!File(basePath, "maho").isFile) return

        val settings = MahoSettings.getInstance(project)
        if (!settings.state.autoConfigureDatabase) return

        configureDatabase(project, basePath)

        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                if (!settings.state.autoConfigureDatabase) return
                val localXmlPath = "$basePath/app/etc/local.xml"
                val relevant = events.any { event ->
                    (event is VFileContentChangeEvent || event is VFileCreateEvent) &&
                        event.path == localXmlPath
                }
                if (relevant) {
                    configureDatabase(project, basePath)
                }
            }
        })
    }

    companion object {
        private const val DATA_SOURCE_NAME = "Maho"
        private val LOG = logger<MahoDatabaseConfigurator>()

        fun configureDatabase(project: Project, basePath: String) {
            val localXmlFile = File(basePath, "app/etc/local.xml")
            val config = MahoLocalXmlParser.parse(localXmlFile)
            if (config == null) {
                LOG.info("No valid database configuration found in local.xml")
                return
            }

            val jdbcUrl = buildJdbcUrl(config, basePath) ?: return
            val driverId = mapDriverId(config.type) ?: return
            val driver = DatabaseDriverManager.getInstance().getDriver(driverId)

            ApplicationManager.getApplication().invokeLater {
                val manager = LocalDataSourceManager.getInstance(project)
                val existing = manager.dataSources.find { it.name == DATA_SOURCE_NAME }

                if (existing != null) {
                    manager.removeDataSource(existing)
                }
                val ds = createDataSource(jdbcUrl, config, driver)
                manager.addDataSource(ds)
                LOG.info("Configured Maho data source: $jdbcUrl")
            }
        }

        private fun createDataSource(
            jdbcUrl: String,
            config: MahoDbConfig,
            driver: com.intellij.database.dataSource.DatabaseDriver?,
        ): LocalDataSource {
            val ds = LocalDataSource()
            ds.name = DATA_SOURCE_NAME
            updateDataSource(ds, jdbcUrl, config, driver)
            return ds
        }

        private fun updateDataSource(
            ds: LocalDataSource,
            jdbcUrl: String,
            config: MahoDbConfig,
            driver: com.intellij.database.dataSource.DatabaseDriver?,
        ) {
            ds.url = jdbcUrl
            ds.username = config.username
            if (driver != null) {
                ds.databaseDriver = driver
            }
            val isSqlite = config.type.lowercase().contains("sqlite")
            ds.authProviderId = if (isSqlite) "no_auth" else "credentials"
        }

        private fun buildJdbcUrl(config: MahoDbConfig, basePath: String): String? {
            val type = config.type.lowercase()
            return when {
                "mysql" in type -> {
                    val port = config.port ?: 3306
                    "jdbc:mysql://${config.host}:$port/${config.dbname}"
                }
                "pgsql" in type || "postgres" in type -> {
                    val port = config.port ?: 5432
                    "jdbc:postgresql://${config.host}:$port/${config.dbname}"
                }
                "sqlite" in type -> {
                    val dbPath = if (config.dbname.startsWith("/")) config.dbname
                                 else "$basePath/var/db/${config.dbname}"
                    "jdbc:sqlite:$dbPath"
                }
                else -> null
            }
        }

        private fun mapDriverId(type: String): String? {
            val t = type.lowercase()
            return when {
                "mysql" in t -> "mysql.8"
                "pgsql" in t || "postgres" in t -> "postgresql"
                "sqlite" in t -> "sqlite.xerial"
                else -> null
            }
        }
    }
}
