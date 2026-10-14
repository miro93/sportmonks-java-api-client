# Contributing

## Build and test

```bash
./gradlew build            # compiles, runs the WireMock tests, javadoc, and the native-smoke entity check
./gradlew :football:liveTest   # opt-in smoke calls against the real API (needs SPORTMONKS_API_TOKEN)
```

## Dependency verification

`gradle/verification-metadata.xml` pins a SHA-256 checksum for every dependency, plugin and
annotation processor. Gradle refuses any artifact whose checksum does not match, so a bumped
dependency (Dependabot included) fails the build until the file is refreshed:

```bash
./gradlew --write-verification-metadata sha256 build javadoc --refresh-dependencies
```

`--refresh-dependencies` matters: without it Gradle only records what it downloads, and
metadata files already in the local cache (BOM `.module` files, for example) are left out,
which then fails on a clean CI runner. One artifact is only resolved by the Native job
(`org.graalvm.buildtools:graalvm-reachability-metadata:<plugin version>`, `-repository.zip`
and `.pom`): without a local GraalVM the write command cannot see it, so after bumping the
`graalvm-native` plugin copy its SHA-256 from Maven Central (`<artifact url>.sha256`) into
the file by hand. Review the diff (only the bumped coordinates should
change) and commit it with the bump.

## Native-image metadata

The GraalVM reachability metadata is generated at build time from the compiled
`@Json.Entity` converters (root `build.gradle.kts`); nothing to maintain by hand. Adding an
entity only requires listing it in `NativeSmoke.ENTITIES`, which `./gradlew build` enforces.

## Adding an endpoint

1. Verify the exact path on its docs page (`https://docs.sportmonks.com/v3/.../<page>.md`
   shows the raw markdown; the group overview pages omit request URLs).
2. Add the method to the endpoint class, a WireMock path test, a decoding test for any new
   record, and a README table row.
