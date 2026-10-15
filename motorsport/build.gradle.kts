import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    `java-library`
    alias(libs.plugins.vanniktech.mavenPublish)
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


// Drop doclint's "missing" group: records are documented in prose, not with one
// @param per component (one "no @param" warning per record component otherwise).
tasks.named<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:all,-missing", true)
}


mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.Javadoc(), sourcesJar = SourcesJar.Sources()))
}
