plugins {
    java
    checkstyle
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependencyManagement) apply false
    alias(libs.plugins.openapi.generator) apply false
}

group = "com.cspot"
version = "0.0.1-SNAPSHOT"
description = "InsuraHub"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

checkstyle {
    toolVersion = "10.12.4"
    configFile = file("config/checkstyle/checkstyle.xml")
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
