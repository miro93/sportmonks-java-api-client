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
    useJUnitPlatform()
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
