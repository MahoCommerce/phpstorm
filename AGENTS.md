# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PhpStorm/IntelliJ plugin for [Maho Commerce](https://mahocommerce.com) providing developer tooling for Maho projects (detected by a `maho` file in the project root). It bundles four feature areas:

- **Code intelligence (LSP)** — launches `maho dev:lsp:start` via PHP for autocomplete, go-to-definition, hover docs, and diagnostics on PHP/XML/PHTML files.
- **AI integration (MCP)** — auto-configures the Maho MCP server (`maho dev:mcp:start`) for Junie / AI Assistant by writing `.junie/mcp/mcp.json`.
- **Database** — auto-configures a Database Explorer data source from `app/etc/local.xml` (MySQL, PostgreSQL, SQLite).
- **CLI** — runs `maho` commands from a dedicated tool window and the Tools menu.

## Build Commands

- **Build plugin:** `./gradlew buildPlugin` (output in `build/distributions/`)
- **Run IDE with plugin:** `./gradlew runIde`
- **Verify plugin:** `./gradlew verifyPlugin`
- **Clean:** `./gradlew clean`

## Tech Stack

- Kotlin on JVM 23, using IntelliJ Platform SDK (`org.jetbrains.intellij.platform` plugin v2.13.1)
- Target IDE: PhpStorm (`platformType = PS`), depends on the `com.jetbrains.php` and `com.intellij.database` bundled plugins
- Uses IntelliJ's `platform.lsp` API (`ProjectWideLspServerDescriptor`, `LspServerSupportProvider`)
- Gradle with Kotlin DSL, configuration cache enabled

## Architecture

Source lives under `src/main/kotlin/com/mahocommerce/maho/`, split by feature area:

**`lsp/`**
- **MahoLspServerSupportProvider** — Entry point registered in `plugin.xml`. On file open, checks if the file is PHP/XML/PHTML and a `maho` CLI exists in the project root, then starts the LSP server.
- **MahoLspServerDescriptor** — Builds the command line (`php maho dev:lsp:start`) to launch the LSP process. Reads the configurable PHP command from settings.
- **MahoSettings / MahoSettingsState** — Project-level persistent state (`maho.xml`) storing the PHP command path and the `autoConfigureDatabase` / `autoConfigureMcp` toggles.
- **MahoConfigurable** — Settings UI panel under Tools > Maho (LSP / Database / MCP groups). On apply, restarts the LSP server and re-runs the database and MCP configurators.

**`mcp/`**
- **MahoMcpConfigurator** — `postStartupActivity` that writes/refreshes `.junie/mcp/mcp.json` pointing at `php maho dev:mcp:start`. Reuses the configured PHP command, merges into existing config (only manages the `maho` key), and is idempotent.

**`database/`**
- **MahoDatabaseConfigurator** — `postStartupActivity` that creates a Database Explorer data source from `app/etc/local.xml`, kept in sync by a VFS listener.
- **MahoLocalXmlParser / MahoDbConfig** — Parse connection details from `local.xml`.

**`cli/`**
- Tool window + actions for running `maho` commands (`MahoToolWindowFactory`, `MahoCommandRunner`, `MahoCommandActionGroup`, etc.).

Plugin descriptor: `src/main/resources/META-INF/plugin.xml`

## Release Process

Tag a version (`v*`) and push — GitHub Actions builds the plugin and uploads the `.zip` to the GitHub release.
