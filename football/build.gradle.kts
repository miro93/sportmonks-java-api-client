import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    `java-library`
    alias(libs.plugins.vanniktech.mavenPublish)
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
    api(project(":core"))
    annotationProcessor(libs.helidon.codegen.apt)
    annotationProcessor(libs.helidon.json.codegen)
    annotationProcessor(libs.helidon.service.codegen)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.wiremock)
    testAnnotationProcessor(libs.helidon.codegen.apt)
    testAnnotationProcessor(libs.helidon.json.codegen)
    testAnnotationProcessor(libs.helidon.service.codegen)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform {
        excludeTags("live")
    }
}

// Smoke calls against the real API; needs SPORTMONKS_API_TOKEN (skipped otherwise).
val liveTest by tasks.registering(Test::class) {
    description = "Runs the live SportMonks API smoke tests (requires SPORTMONKS_API_TOKEN)."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("live")
    }
    outputs.upToDateWhen { false }
}

// Drop doclint's "missing" group: records are documented in prose, not with one
// @param per component (~100 "no @param" warnings otherwise).
tasks.named<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:all,-missing", true)
}

// Use the agent's "standard" mode so the generated reachability metadata is
// unconditional (correct for a library that ships metadata for its own types).
graalvmNative {
    agent {
        defaultMode = "standard"
    }
}

mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.Javadoc(), sourcesJar = SourcesJar.Sources()))
}
