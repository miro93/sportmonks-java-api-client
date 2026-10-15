plugins {
    application
    alias(libs.plugins.graalvm.native)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(27)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":football"))
    implementation(project(":motorsport"))
}

application {
    mainClass = "io.github.miro93.sportmonks.smoke.NativeSmoke"
}

graalvmNative {
    // We ship our own reachability metadata in the jars (auto-detected from
    // META-INF/native-image); the community metadata repository is not needed and
    // its schema has lagged behind the installed GraalVM before.
    metadataRepository {
        enabled = false
    }
    binaries.named("main") {
        imageName = "native-smoke"
        buildArgs.add("--no-fallback")
    }
}

// Fails when a `@Json.Entity` gained a generated converter but is missing from
// NativeSmoke.ENTITIES, so the native image keeps exercising every converter.
val checkSmokeEntities by tasks.registering {
    val classesDirs = listOf(":core", ":football", ":motorsport").map { p ->
        project(p).extensions.getByType<SourceSetContainer>()["main"].output.classesDirs
    }
    val smokeSource = layout.projectDirectory.file("src/main/java/io/github/miro93/sportmonks/smoke/NativeSmoke.java")
    inputs.files(classesDirs)
    inputs.file(smokeSource)
    dependsOn(":core:classes", ":football:classes", ":motorsport:classes")
    doLast {
        val internal = setOf("Envelope", "ApiResponse")
        val converters = classesDirs.flatMap { it.asFileTree.files }
            .map { it.name }
            .filter { it.endsWith("__GeneratedConverter.class") }
            .map { it.removeSuffix("__GeneratedConverter.class") }
            .filterNot { it in internal }
            .toSortedSet()
        val listed = Regex("""(\w+)\.class""").findAll(smokeSource.asFile.readText())
            .map { it.groupValues[1] }.toSet()
        val missing = converters - listed
        check(missing.isEmpty()) { "NativeSmoke.ENTITIES is missing: $missing" }
    }
}
tasks.named("check") { dependsOn(checkSmokeEntities) }
