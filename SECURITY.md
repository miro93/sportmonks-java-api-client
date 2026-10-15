# Security Policy

## Supported versions

Only the latest release on Maven Central receives security fixes.

## Software bill of materials

Every GitHub Release carries a CycloneDX SBOM per published module
(`sportmonks-<module>-<version>-cyclonedx.json`), generated from the runtime classpath by
`./gradlew cyclonedxDirectBom`.

## Reporting a vulnerability

Please do not open a public issue for security problems.

Report them privately through GitHub's private vulnerability reporting:
<https://github.com/miro93/sportmonks-java-api-client/security/advisories/new>

You will get an acknowledgement within 7 days. Fixes are published as a new release
and documented in the release notes.
