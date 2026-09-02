plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "sportmonks-java-api-client"

include("core", "football", "native-smoke")
