package com.mahocommerce.maho.cli

object MahoCommandRegistry {

    val commands: List<MahoCommand> = listOf(
        MahoCommand(
            id = "dev.frontend.theme.create",
            cliCommand = "dev:frontend:theme:create",
            label = "Create Frontend Theme",
            refreshVfs = true,
            parameters = listOf(
                MahoCommandParam(name = "--package", label = "Package name", isOption = true),
                MahoCommandParam(name = "--theme", label = "Theme name", required = false, default = "default", isOption = true),
                MahoCommandParam(name = "--parent", label = "Parent theme", required = false, default = "base/default", isOption = true),
            ),
        ),
        MahoCommand(
            id = "dev.create.command",
            cliCommand = "dev:create-command",
            label = "Create Command",
            refreshVfs = true,
            parameters = listOf(
                MahoCommandParam(name = "name", label = "Command name (e.g. cache:clean)", isOption = false),
                MahoCommandParam(name = "description", label = "Description", required = false, default = "A new Maho CLI command", isOption = false),
            ),
        ),
        MahoCommand(
            id = "cache.flush",
            cliCommand = "cache:flush",
            label = "Flush Cache",
        ),
        MahoCommand(
            id = "cache.enable",
            cliCommand = "cache:enable",
            label = "Enable Cache",
        ),
        MahoCommand(
            id = "cache.disable",
            cliCommand = "cache:disable",
            label = "Disable Cache",
        ),
        MahoCommand(
            id = "index.reindex.all",
            cliCommand = "index:reindex:all",
            label = "Reindex All",
        ),
        MahoCommand(
            id = "index.reindex",
            cliCommand = "index:reindex",
            label = "Reindex",
            parameters = listOf(
                MahoCommandParam(name = "index_code", label = "Index code (e.g. catalog_product_price)", isOption = false),
            ),
        ),
        MahoCommand(
            id = "admin.user.create",
            cliCommand = "admin:user:create",
            label = "Create Admin User",
            parameters = listOf(
                MahoCommandParam(name = "username", label = "Username", isOption = false),
                MahoCommandParam(name = "password", label = "Password", isPassword = true, isOption = false),
                MahoCommandParam(name = "email", label = "Email", isOption = false),
                MahoCommandParam(name = "firstname", label = "First name", isOption = false),
                MahoCommandParam(name = "lastname", label = "Last name", isOption = false),
            ),
        ),
        MahoCommand(
            id = "customer.create",
            cliCommand = "customer:create",
            label = "Create Customer",
            parameters = listOf(
                MahoCommandParam(name = "email", label = "Email", isOption = false),
                MahoCommandParam(name = "password", label = "Password", isPassword = true, isOption = false),
                MahoCommandParam(name = "firstname", label = "First name", isOption = false),
                MahoCommandParam(name = "lastname", label = "Last name", isOption = false),
            ),
        ),
        MahoCommand(
            id = "health.check",
            cliCommand = "health-check",
            label = "Health Check",
        ),
    )
}
