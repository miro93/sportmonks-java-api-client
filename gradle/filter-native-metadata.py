#!/usr/bin/env python3
"""Reduce an agent-generated reachability-metadata.json to this library's own
types, producing stable, publishable metadata.

The GraalVM tracing agent runs inside a JUnit/Gradle JVM, so its raw output also
records reflection on test classes, JUnit/AssertJ/slf4j service resources, and JDK
internals. None of that belongs in the published jar. This keeps only reflection
entries for `io.github.miro93.sportmonks.*` types that are NOT test classes
(Jackson 3 ships its own metadata; the JDK/Jackson internals are covered there),
drops the `resources` section entirely, and sorts everything for deterministic
output (so the CI drift check is a meaningful `git diff`).

Usage: filter-native-metadata.py <reachability-metadata.json>
"""
import json
import sys

PREFIX = "io.github.miro93.sportmonks"


def keep(entry: dict) -> bool:
    t = entry.get("type")
    if not isinstance(t, str):
        return False
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

    print(f"filtered {path}: kept {len(reflection)} reflection entries for {PREFIX}.* (dropped resources + non-library + test types)")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)
    main(sys.argv[1])
