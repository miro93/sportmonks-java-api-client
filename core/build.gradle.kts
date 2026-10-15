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
    api(libs.helidon.json.binding)
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

// Register JDK standard implementation/API note tags so Javadoc does not error, and
// drop doclint's "missing" group: records are documented in prose, not with one
// @param per component (~130 "no @param"/"no comment" warnings otherwise).
tasks.named<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).apply {
        addBooleanOption("Xdoclint:all,-missing", true)
        addStringOption("tag", "apiNote:a:API Note:")
        addStringOption("tag", "implSpec:a:Implementation Requirements:")
        addStringOption("tag", "implNote:a:Implementation Note:")
    }
}


mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.Javadoc(), sourcesJar = SourcesJar.Sources()))
}
