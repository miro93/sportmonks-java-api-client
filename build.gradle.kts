import groovy.json.JsonOutput
import groovy.json.JsonSlurper

// Declare the GraalVM native-build-tools plugin once at the root (apply false) so it
// is loaded by a single classloader and shared across subprojects. Applying it
// independently per subproject otherwise causes a GraalVMReachabilityMetadataService
// classloader-cast failure during :native-smoke:nativeCompile.
plugins {
    alias(libs.plugins.graalvm.native) apply false
}

// GraalVM reachability metadata, generated from the compiled classes instead of the
// tracing agent: Helidon's service registry resolves every `*__GeneratedConverter__ServiceDescriptor`
// (one per @Json.Entity) reflectively via its INSTANCE field, and needs a fixed set of its
// own types (gradle/helidon-reachability-metadata.json, captured once from the tracing
// agent on Helidon 4.5.4) because Helidon ships no reflect-config for them. The file lands in the jar
// under META-INF/native-image/<group>/<module>/ where native-image auto-detects it; the
// native-smoke job proves it is sufficient.
configure(listOf(project(":core"), project(":football"))) {
    plugins.withId("java-library") {
        val sourceSets = extensions.getByType<SourceSetContainer>()
        val helidonFragment = rootProject.layout.projectDirectory.file("gradle/helidon-reachability-metadata.json")
        val generateReachabilityMetadata by tasks.registering {
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
