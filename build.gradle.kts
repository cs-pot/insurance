plugins {
    java
    checkstyle
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

group = "com.cspot"
version = "0.0.1-SNAPSHOT"
description = "InsuraHub"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

checkstyle {
    toolVersion = "10.12.4"
    configFile = file("config/checkstyle/checkstyle.xml")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.springframework.boot:spring-boot-h2console")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val stagedJavaFiles = providers.gradleProperty("checkstyleFiles")
    .orElse("")
    .map { value ->
        value
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { it.endsWith(".java") }
    }

tasks.register<Checkstyle>("checkstyleStaged") {
    group = "verification"
    description = "Runs Checkstyle only on staged Java files"

    val stagedFiles = files(
        stagedJavaFiles.map { paths ->
            paths.map { path -> file(path) }
        }
    )

    source(stagedFiles)
    classpath = files()

    configFile = file("config/checkstyle/checkstyle.xml")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    onlyIf {
        stagedFiles.files.isNotEmpty()
    }
}

tasks.register("setup") {
    group = "setup"
    description = "Initialize local project configuration"

    doLast {
        val isWindows = System.getProperty("os.name")
            .lowercase()
            .contains("windows")

        if (!isWindows) {
            rootProject.file(".githooks/pre-commit").setExecutable(true)
        }

        val result = ProcessBuilder("git", "config", "core.hooksPath", ".githooks")
            .directory(rootProject.projectDir)
            .inheritIO()
            .start()
            .waitFor()

        if (result != 0) {
            throw GradleException("Failed to configure Git hooks")
        }

        println("Project setup completed.")
    }
}