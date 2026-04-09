plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.13.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(23)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
        bundledPlugins("com.jetbrains.php")
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        val version = providers.gradleProperty("pluginVersion").get()
        val changelogFile = layout.projectDirectory.file("CHANGELOG.md").asFile
        val changelogHtml = if (changelogFile.exists()) {
            val text = changelogFile.readText()
            val section = Regex("""## \[$version]\s*\n([\s\S]*?)(?=\n## |\z)""")
                .find(text)?.groupValues?.get(1)?.trim() ?: ""
            section.lines().joinToString("<br>\n") { line ->
                when {
                    line.startsWith("### ") -> "<b>${line.removePrefix("### ")}</b>"
                    line.startsWith("- ") -> "&bull; ${line.removePrefix("- ")}"
                    line.startsWith("  - ") -> "&nbsp;&nbsp;&bull; ${line.removePrefix("  - ")}"
                    else -> line
                }
            }
        } else ""
        changeNotes = provider { changelogHtml }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}
