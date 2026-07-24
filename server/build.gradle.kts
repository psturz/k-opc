plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

group = "dev.psturz"
version = "1.0.0-SNAPSHOT"

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("dev.psturz.kopc.server.ApplicationKt")
}

dependencies {
    implementation(project(":client"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    implementation(libs.kotlinx.serialization.json)
    runtimeOnly(libs.logback.classic)
}
