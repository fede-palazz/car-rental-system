plugins {
    kotlin("jvm") version "2.1.0" // Updated to support Java 23
    kotlin("plugin.spring") version "2.1.0"
}

group = "com.rentalcarsystem"
version = "0.0.1-SNAPSHOT"
description = "Keycloak User Events Listener"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val keycloakVersion = "26.2.4"

dependencies {
    // Keycloak dependencies (provided scope)
    compileOnly("org.keycloak:keycloak-core:${keycloakVersion}")
    compileOnly("org.keycloak:keycloak-server-spi:${keycloakVersion}")
    compileOnly("org.keycloak:keycloak-server-spi-private:${keycloakVersion}")
    compileOnly("org.keycloak:keycloak-services:${keycloakVersion}")

    // Kafka Client
    implementation("org.apache.kafka:kafka-clients:3.6.0") {
        exclude(group = "org.slf4j")
    }

    // Jackson for JSON serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
//	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
//	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Logging (provided by Keycloak)
//    compileOnly("org.jboss.logging:jboss-logging:3.5.3.Final")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

// Exclude SLF4J and other libraries provided by Keycloak
    val excludePatterns = listOf(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/MANIFEST.MF",
        "**/module-info.class"
    )

    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    }) {
        exclude(excludePatterns)
        // Exclude SLF4J classes since Keycloak provides them
        exclude("org/slf4j/**")
        exclude("org/apache/logging/slf4j/**")
    }

    from(sourceSets.main.get().output)
}
