package com.mahocommerce.maho.database

import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

object MahoLocalXmlParser {

    fun parse(localXmlFile: File): MahoDbConfig? {
        if (!localXmlFile.isFile) return null

        return try {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(localXmlFile)
            val connection = findDefaultConnection(doc.documentElement) ?: return null

            val type = connection.childText("engine") ?: connection.childText("type") ?: "pdo_mysql"
            val hostRaw = connection.childText("host") ?: "localhost"
            val username = connection.childText("username") ?: ""
            val password = connection.childText("password") ?: ""
            val dbname = connection.childText("dbname") ?: ""

            val (host, port) = parseHostPort(hostRaw)

            MahoDbConfig(type, host, port, dbname, username, password)
        } catch (_: Exception) {
            null
        }
    }

    private fun findDefaultConnection(root: Element): Element? {
        val global = root.firstChild("global") ?: return null
        val resources = global.firstChild("resources") ?: return null
        val defaultSetup = resources.firstChild("default_setup") ?: return null
        return defaultSetup.firstChild("connection")
    }

    private fun parseHostPort(hostRaw: String): Pair<String, Int?> {
        if (":" !in hostRaw) return hostRaw to null
        val parts = hostRaw.split(":", limit = 2)
        return parts[0] to parts[1].toIntOrNull()
    }

    private fun Element.firstChild(tagName: String): Element? {
        val children = childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node is Element && node.tagName == tagName) return node
        }
        return null
    }

    private fun Element.childText(tagName: String): String? {
        return firstChild(tagName)?.textContent?.trim()?.ifEmpty { null }
    }
}
