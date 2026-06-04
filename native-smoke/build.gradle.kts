plugins {
    application
    alias(libs.plugins.graalvm.native)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":football"))
}

application {
    mainClass = "io.github.miro93.sportmonks.smoke.NativeSmoke"
}

graalvmNative {
    // We ship our own reachability metadata in the jars (auto-detected from
    // META-INF/native-image); the community metadata repository is not needed and
    // its newer schema is incompatible with the installed GraalVM 25.0.0.
    metadataRepository {
        enabled = false
    }
    binaries.named("main") {
        imageName = "native-smoke"
        buildArgs.add("--no-fallback")
    }
}
