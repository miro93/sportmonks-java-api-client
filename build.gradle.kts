import groovy.json.JsonOutput
import groovy.json.JsonSlurper

// Build-time only: the CycloneDX plugin drags Jackson 2.22.0, which carries four high
// advisories (GHSA-7hhh-6rmp-j9qf, GHSA-p6pp-m3f8-5c89, GHSA-cxp5-3px4-pw24, GHSA-wv8q-qhhj-9h54)
// that Dependabot reports on this classpath. Nothing here reaches the published jars.
buildscript {
    dependencies {
        constraints {
            classpath("com.fasterxml.jackson.core:jackson-core:2.22.4")
            classpath("com.fasterxml.jackson.core:jackson-databind:2.22.4")
        }
    }
}

// Declare the GraalVM native-build-tools plugin once at the root (apply false) so it
// is loaded by a single classloader and shared across subprojects. Applying it
// independently per subproject otherwise causes a GraalVMReachabilityMetadataService
// classloader-cast failure during :native-smoke:nativeCompile.
plugins {
    alias(libs.plugins.graalvm.native) apply false
    alias(libs.plugins.cyclonedx.bom) apply false
}

// GraalVM reachability metadata, generated from the compiled classes instead of the
// tracing agent: Helidon's service registry resolves every `*__GeneratedConverter__ServiceDescriptor`
// (one per @Json.Entity) reflectively via its INSTANCE field, and needs a fixed set of its
// own types (gradle/helidon-reachability-metadata.json, captured once from the tracing
// agent on Helidon 4.5.4) because Helidon ships no reflect-config for them. The file lands in the jar
// under META-INF/native-image/<group>/<module>/ where native-image auto-detects it; the
// native-smoke job proves it is sufficient.
configure(listOf(project(":core"), project(":football"), project(":motorsport"))) {
    // javac's "module" lint cannot be satisfied here: the group name carries digits (miro93)
    // and core's qualified exports name the sport modules, which are compiled after it.
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-Xlint:-module")
    }
    plugins.withId("java-library") {
        val sourceSets = extensions.getByType<SourceSetContainer>()
        val helidonFragment = rootProject.layout.projectDirectory.file("gradle/helidon-reachability-metadata.json")
        val generateReachabilityMetadata = tasks.register("generateReachabilityMetadata") {
            val classesDirs = sourceSets["main"].output.classesDirs
            val outputDir = layout.buildDirectory.dir("generated/native-metadata")
            // the Maven group comes from gradle.properties (GROUP), as the publishing plugin reads it
            val groupId = providers.gradleProperty("GROUP")
            val moduleName = project.name
            inputs.files(classesDirs)
            inputs.property("group", groupId)
            inputs.file(helidonFragment)
            outputs.dir(outputDir)
            doLast {
                val descriptors = classesDirs.flatMap { dir ->
                    dir.walkTopDown()
                        .filter { it.isFile && it.name.endsWith("__GeneratedConverter__ServiceDescriptor.class") }
                        .map { it.relativeTo(dir).path.removeSuffix(".class").replace(File.separatorChar, '.') }
                        .toList()
                }.map { mapOf("type" to it, "fields" to listOf(mapOf("name" to "INSTANCE"))) }
                @Suppress("UNCHECKED_CAST")
                val helidon = JsonSlurper().parse(helidonFragment.asFile) as List<Map<String, Any>>
                val reflection = (descriptors + helidon).sortedBy { it["type"] as String }
                val target = outputDir.get()
                        .file("META-INF/native-image/${groupId.get()}/$moduleName/reachability-metadata.json").asFile
                target.parentFile.mkdirs()
                target.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(mapOf("reflection" to reflection))) + "\n")
            }
        }
        sourceSets["main"].resources.srcDir(generateReachabilityMetadata)
    }
}

// CycloneDX SBOM per published module (build/reports/cyclonedx-direct/bom.json), attached to
// the GitHub Release by release.yml. Only the runtime classpath: test and annotation-processor
// dependencies never reach consumers.
configure(listOf(project(":core"), project(":football"), project(":motorsport"))) {
    // the plugin reads group/version eagerly, before the publishing plugin sets them
    group = providers.gradleProperty("GROUP").get()
    version = providers.gradleProperty("VERSION_NAME").get()
    apply(plugin = "org.cyclonedx.bom")
    tasks.named<org.cyclonedx.gradle.CyclonedxDirectTask>("cyclonedxDirectBom") {
        includeConfigs = listOf("runtimeClasspath")
        // published artifactId, not the Gradle project name
        // ponytail: the football/motorsport BOMs still reference core by project name
        // (`core`), the plugin derives inner project components from the Gradle graph;
        // renaming the projects in settings.gradle.kts would fix it (~170 references).
        componentName = project.property("POM_ARTIFACT_ID") as String
    }
}
