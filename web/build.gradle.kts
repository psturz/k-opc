@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose)
}

group = "dev.psturz"
version = "1.0.0-SNAPSHOT"

kotlin {
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":ui"))
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5")
        }
    }
}
