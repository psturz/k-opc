plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose)
}

group = "dev.psturz"
version = "1.0.0-SNAPSHOT"

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(project(":ui"))
    implementation(compose.desktop.currentOs)
    runtimeOnly(libs.logback.classic)
}

compose.desktop {
    application {
        mainClass = "dev.psturz.kopc.desktop.MainKt"
    }
}
