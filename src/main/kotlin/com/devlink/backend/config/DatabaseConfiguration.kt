package com.devlink.backend.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.net.URI
import javax.sql.DataSource

@Configuration
class DatabaseConfiguration(
    @Value("\${spring.datasource.url:}") private val configuredUrl: String,
    @Value("\${spring.datasource.username:}") private val configuredUsername: String,
    @Value("\${spring.datasource.password:}") private val configuredPassword: String
) {
    private val log = LoggerFactory.getLogger(DatabaseConfiguration::class.java)

    @Primary
    @Bean
    fun dataSource(): DataSource {
        val envDatabaseUrl = System.getenv("DATABASE_URL")?.trim().orEmpty()
        val rawUrl = if (envDatabaseUrl.isNotBlank()) envDatabaseUrl else configuredUrl.trim()

        val hikariConfig = HikariConfig()

        if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
            log.info("Parsing standard postgres URL from environment for Render/Cloud database")
            try {
                // Format: postgres://username:password@host:port/database
                val uri = URI(rawUrl)
                val userInfo = uri.userInfo?.split(":", limit = 2)
                val username = userInfo?.getOrNull(0) ?: configuredUsername
                val password = userInfo?.getOrNull(1) ?: configuredPassword
                val host = uri.host
                val port = if (uri.port != -1) uri.port else 5432
                val dbName = uri.path.removePrefix("/")
                val query = uri.query

                val jdbcUrl = buildString {
                    append("jdbc:postgresql://").append(host).append(":").append(port).append("/").append(dbName)
                    if (!query.isNullOrBlank()) {
                        append("?").append(query)
                    }
                }

                hikariConfig.jdbcUrl = jdbcUrl
                hikariConfig.username = username
                hikariConfig.password = password
                hikariConfig.driverClassName = "org.postgresql.Driver"
                log.info("Configured PostgreSQL JDBC URL: {}", jdbcUrl)
            } catch (e: Exception) {
                log.error("Failed to parse postgres URL '{}', falling back to raw configuration: {}", rawUrl, e.message)
                hikariConfig.jdbcUrl = rawUrl
                hikariConfig.username = configuredUsername
                hikariConfig.password = configuredPassword
                hikariConfig.driverClassName = "org.postgresql.Driver"
            }
        } else {
            hikariConfig.jdbcUrl = rawUrl
            hikariConfig.username = configuredUsername
            hikariConfig.password = configuredPassword
            hikariConfig.driverClassName = "org.postgresql.Driver"
        }

        hikariConfig.maximumPoolSize = 10
        hikariConfig.minimumIdle = 2
        hikariConfig.idleTimeout = 30000
        hikariConfig.connectionTimeout = 20000

        return HikariDataSource(hikariConfig)
    }
}
