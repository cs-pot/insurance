plugins {
    java
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.spring.dependencyManagement)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}")
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("jakarta.annotation:jakarta.annotation-api")

    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-context")

    implementation("jakarta.servlet:jakarta.servlet-api")
    implementation("jakarta.validation:jakarta.validation-api")

    implementation(libs.swagger.annotations)
    implementation(libs.swagger.models)

    implementation(libs.jackson.databind.nullable)
}

openApiGenerate {
    generatorName.set("spring")

    inputSpec.set(
        "$projectDir/openapi/openapi.yaml"
    )

    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)

    apiPackage.set("com.cspot.insurahub.api")
    modelPackage.set("com.cspot.insurahub.model")

    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "dateLibrary" to "java8",
            "useJakartaEe" to "true",
            "useSpringBoot4" to "true",
            "useTags" to "true",
            "useResponseEntity" to "false",
            "hideGenerationTimestamp" to "true"
        )
    )
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generated/src/main/java"))
    }
}

tasks.compileJava {
    dependsOn(tasks.openApiGenerate)
}
