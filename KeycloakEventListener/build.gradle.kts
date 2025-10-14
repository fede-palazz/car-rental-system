plugins {
    kotlin("jvm") version "2.1.0" // Updated to support Java 23
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.rentalcarsystem"
version = "0.0.1-SNAPSHOT"
description = "Keycloak User Events Listener"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(23)
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

	implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.kafka:spring-kafka")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Create a fat JAR with dependencies
//tasks.jar {
//    archiveClassifier.set("")
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//
//    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
//        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
//    }
//}

// Configure main class for Spring Boot
springBoot {
    mainClass.set("com.rentalcarsystem.KeycloakEventListenerApplication")
}
