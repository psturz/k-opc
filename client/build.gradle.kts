@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "dev.psturz"
version = "1.0.0-SNAPSHOT"

kotlin {
    jvmToolchain(25)

    jvm {
        mainRun {
            mainClass = "dev.psturz.kopc.MainKt"
        }

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()

            val podmanSocket = File(System.getProperty("user.home"), ".local/share/containers/podman/machine/podman.sock")
            if (System.getenv("DOCKER_HOST") == null && podmanSocket.exists()) {
                environment("DOCKER_HOST", "unix://${podmanSocket.absolutePath}")
                environment("TESTCONTAINERS_RYUK_DISABLED", "true")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
        }
        jvmMain.dependencies {
            implementation(libs.milo.sdk.client)
            implementation(libs.milo.sdk.server)
            implementation(libs.slf4j.api)
            implementation(libs.bouncycastle.provider)
            runtimeOnly(libs.logback.classic)
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
            implementation(libs.kotest.assertions.core)
            implementation(libs.testcontainers)
        }
    }
}
