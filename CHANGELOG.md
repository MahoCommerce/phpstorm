<!-- Keep a Changelog: https://keepachangelog.com -->

## [0.9.0]
### Added
- **CLI Commands integration** — Run Maho CLI commands directly from the IDE via Tools > Maho menu
  - Create Frontend Theme (`dev:frontend:theme:create`)
  - Create Command (`dev:create-command`)
  - Cache management (flush, enable, disable)
  - Index management (reindex all, reindex single)
  - Create Admin User
  - Create Customer
  - Health Check
- Parameter dialogs for commands that require input
- Maho console tool window for command output
- Plugin icon

## [0.4.0]
### Added
- Configurable PHP command in settings (supports Docker, custom paths)

### Fixed
- Nullable binding in settings panel

## [0.3.0]
### Changed
- Bumped Kotlin to 2.3.0 to match PhpStorm 2026.1

## [0.2.0]
### Added
- GitHub Actions release workflow with JetBrains Marketplace publishing

## [0.1.0]
### Added
- Initial release
- Maho Intelligence LSP integration (autocomplete, go-to-definition, hover docs, diagnostics)
- Automatic LSP server launch when `maho` CLI is detected in project root
- Support for PHP, XML, and PHTML files
