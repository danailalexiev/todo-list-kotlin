import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "bg.dalexiev.todo"
version = "1.0.0"
application {
    mainClass.set("bg.dalexiev.todo.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(projects.shared)
    
    implementation(libs.logback)
    
    implementation(libs.bundles.ktor.server)
    
    implementation(libs.hikari)
    implementation(libs.postgres)
    implementation(libs.bundles.exposed)
    
    implementation(libs.jbcrypt)
    
    implementation(libs.konform.jvm)
    
    testImplementation(libs.mockk)
    testImplementation(libs.bundles.kotest.server)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.testcontainers.postgresql)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}