package com.rentalcarsystem.analyticsservice.config

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.MigrationVersion
import org.flywaydb.core.api.configuration.ClassicConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")  // Exclude this config when 'test' profile is active
class FlywayConfig {
    @Bean
    fun flyway(
        @Value("\${spring.flyway.url}") flywayUrl: String,
        @Value("\${spring.flyway.user}") flywayUser: String,
        @Value("\${spring.flyway.password}") flywayPassword: String,
        @Value("\${spring.flyway.schemas}") flywaySchemas: List<String>,
        @Value("\${spring.flyway.locations}") flywayLocations: List<String>,
        @Value("\${spring.flyway.target:latest}") targetVersionString: String
    ): Flyway {
        val configuration = ClassicConfiguration()

        // Configure Flyway with the correct properties
        configuration.setDataSource(flywayUrl, flywayUser, flywayPassword)

        // Convert List<String> to Array<String>
        val schemasArray = flywaySchemas.toTypedArray()
        configuration.schemas = schemasArray

        val locationsArray = flywayLocations.map { Location(it) }.toTypedArray()
        configuration.setLocations(*locationsArray)

        // Convert the target version string to MigrationVersion
        configuration.target = MigrationVersion.fromVersion(targetVersionString)

        // Create the Flyway instance
        val flyway = Flyway(configuration)

        // Run the migration (needed when defining a custom Flyway bean)
        flyway.migrate()

        return flyway
    }
}