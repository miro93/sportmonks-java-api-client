// Declare the GraalVM native-build-tools plugin once at the root (apply false) so it
// is loaded by a single classloader and shared across subprojects. Applying it
// independently per subproject otherwise causes a GraalVMReachabilityMetadataService
// classloader-cast failure during :native-smoke:nativeCompile.
plugins {
    alias(libs.plugins.graalvm.native) apply false
}
