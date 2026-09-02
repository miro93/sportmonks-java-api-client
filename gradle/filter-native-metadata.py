#!/usr/bin/env python3
"""Reduce an agent-generated reachability-metadata.json to this library's own
types, producing stable, publishable metadata.

The GraalVM tracing agent runs inside a JUnit/Gradle JVM, so its raw output also
records reflection on test classes, JUnit/AssertJ/slf4j service resources, and JDK
internals. None of that belongs in the published jar. This keeps only reflection
entries for `io.github.miro93.sportmonks.*` types that are NOT test classes,
plus `io.helidon.*` entries: Helidon 4.5.4's service registry resolves its
service.loader / service-registry descriptors via runtime Class.forName, but no
Helidon jar ships the reflect-config for it — without these entries the native
image dies at JsonBinding.create() with ClassNotFoundException (e.g.
io.helidon.config.spi.ConfigParser). Helidon's own native-image configs cover
only resources (manifests, service-registry.json, service.loader) — and even
there miss `META-INF/helidon/**/feature-registry.json`, which every manifest
lists and MetadataDiscovery hard-fails on; core ships a one-pattern
`resource-config.json` (core-helidon-feature-registry-workaround/) closing that
gap. Drop both workarounds when Helidon fixes its packaging.
JDK/test-framework internals are still safe to drop: they aren't ours to ship. This keeps the file scoped to
what only this library's own decode path requires — confirmed by the
`native-smoke` CI job (.github/workflows/native.yml), which compiles a native
image against this filtered metadata and runs it. This also drops the `resources`
section entirely, and sorts everything for deterministic output (so the CI drift
check is a meaningful `git diff`).

Usage: filter-native-metadata.py <reachability-metadata.json>
"""
import json
import sys

PREFIX = "io.github.miro93.sportmonks"


def keep(entry: dict) -> bool:
    t = entry.get("type")
    if not isinstance(t, str):
        return False
    # Helidon runtime service discovery needs its own types reflectively (see module doc).
    if t.startswith("io.helidon."):
        return True
    # Match the package boundary so sibling namespaces (e.g. io.github.miro93.sportmonksX)
    # are not over-included.
    if t != PREFIX and not t.startswith(PREFIX + "."):
        return False
    # Drop test classes (e.g. *DecodingTest, *EndpointTest, FootballClient*Test)
    # and their nested helper types (e.g. ApiExecutorTest$Team).
    return not (t.endswith("Test") or "Test$" in t)


def sort_entry(entry: dict) -> dict:
    out = dict(entry)
    methods = out.get("methods")
    if isinstance(methods, list):
        out["methods"] = sorted(
            methods, key=lambda m: (m.get("name", ""), json.dumps(m.get("parameterTypes", [])))
        )
    return out


def main(path: str) -> None:
    with open(path) as f:
        data = json.load(f)

    reflection = [sort_entry(e) for e in data.get("reflection", []) if keep(e)]
    reflection.sort(key=lambda e: e["type"])

    filtered = {"reflection": reflection}
    with open(path, "w") as f:
        json.dump(filtered, f, indent=2)
        f.write("\n")

    print(f"filtered {path}: kept {len(reflection)} reflection entries for {PREFIX}.* + io.helidon.* (dropped resources + non-library + test types)")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)
    main(sys.argv[1])
