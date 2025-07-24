package com.example.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database


fun Application.configureDatabase() {
    val dbConfig = environment.config.config("database")

    val driverClassName = dbConfig.property("driverClassName").getString()
    val url = dbConfig.property("url").getString()
    val username = dbConfig.property("username").getString()
    val password = dbConfig.property("password").getString()
    val poolSize = dbConfig.property("poolSize").getString().toInt()

    val config = HikariConfig().apply {
        this.driverClassName = driverClassName
        this.jdbcUrl = url
        this.username = username
        this.password = password
        this.maximumPoolSize = poolSize

        isAutoCommit = false
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)
}