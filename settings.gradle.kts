rootProject.name = "k-opc"

include(":client", ":server")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
