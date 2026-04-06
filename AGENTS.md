# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PhpStorm/IntelliJ plugin for [Maho Commerce](https://mahocommerce.com) that integrates the Maho Intelligence LSP server. The plugin launches `maho dev:lsp:start` via PHP when a Maho project is detected (looks for a `maho` file in the project root), providing autocomplete, go-to-definition, hover docs, and diagnostics for PHP/XML/PHTML files.

## Build Commands

- **Build plugin:** `./gradlew buildPlugin` (output in `build/distributions/`)
- **Run IDE with plugin:** `./gradlew runIde`
- **Verify plugin:** `./gradlew verifyPlugin`
- **Clean:** `./gradlew clean`

## Tech Stack

- Kotlin on JVM 23, using IntelliJ Platform SDK (`org.jetbrains.intellij.platform` plugin v2.13.1)
- Target IDE: PhpStorm (`platformType = PS`), depends on the `com.jetbrains.php` bundled plugin
- Uses IntelliJ's `platform.lsp` API (`ProjectWideLspServerDescriptor`, `LspServerSupportProvider`)
- Gradle with Kotlin DSL, configuration cache enabled

## Architecture

All source is in `src/main/kotlin/com/mahocommerce/maho/lsp/`:

- **MahoLspServerSupportProvider** — Entry point registered in `plugin.xml`. On file open, checks if the file is PHP/XML/PHTML and a `maho` CLI exists in the project root, then starts the LSP server.
- **MahoLspServerDescriptor** — Builds the command line (`php maho dev:lsp:start`) to launch the LSP process. Reads the configurable PHP command from settings.
- **MahoSettings / MahoSettingsState** — Project-level persistent state (`maho.xml`) storing the PHP command path.
- **MahoConfigurable** — Settings UI panel under Tools > Maho. On apply, restarts the LSP server.

Plugin descriptor: `src/main/resources/META-INF/plugin.xml`

## Release Process

Tag a version (`v*`) and push — GitHub Actions builds the plugin and uploads the `.zip` to the GitHub release.
