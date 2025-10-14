plugins {
    kotlin("jvm") version "2.1.10"
    `java-library`
}

group = "com.rentalcarsystem"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val keycloakVersion = "26.2.4"

dependencies {
    testImplementation(kotlin("test"))

    // Keycloak dependencies (provided scope)
    compileOnly("org.keycloak:keycloak-core:$keycloakVersion")
    compileOnly("org.keycloak:keycloak-server-spi:$keycloakVersion")
    compileOnly("org.keycloak:keycloak-server-spi-private:$keycloakVersion")
    compileOnly("org.keycloak:keycloak-services:$keycloakVersion")

    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    // HTTP client
    implementation("org.apache.httpcomponents:httpclient:4.5.14")

    // Logging
    compileOnly("org.slf4j:slf4j-api:2.0.7")

    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

// Create a fat JAR with dependencies
tasks.jar {
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
}