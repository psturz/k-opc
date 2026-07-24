rootProject.name = "k-opc"

include(":client", ":server", ":ui", ":desktop", ":web")

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
