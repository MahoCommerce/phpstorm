package com.mahocommerce.maho.database

data class MahoDbConfig(
    val type: String,
    val host: String,
    val port: Int?,
    val dbname: String,
    val username: String,
    val password: String,
)
