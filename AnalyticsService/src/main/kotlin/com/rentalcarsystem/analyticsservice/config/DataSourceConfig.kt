package com.rentalcarsystem.analyticsservice.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

@Configuration
@Profile("!test")  // Exclude this config when 'test' profile is active
class DataSourceConfig {
    @Bean
    fun dataSource(
        @Value("\${spring.datasource.url}") dbUrl: String,
        @Value("\${spring.datasource.username}") dbUser: String,
        @Value("\${spring.datasource.password}") dbPassword: String
    ): DataSource {
        val dataSource = HikariDataSource()
        dataSource.jdbcUrl = dbUrl
        dataSource.username = dbUser
        dataSource.password = dbPassword
        return dataSource
    }
}