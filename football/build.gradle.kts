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

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.wiremock)
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
