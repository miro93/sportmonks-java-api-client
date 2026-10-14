package io.github.miro93.sportmonks.core.json;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/// Locates the byte ranges of the members of a top-level JSON object without building a
/// tree: a structural scan that only tracks strings (with escapes) and nesting depth.
/// Multi-byte UTF-8 sequences never contain ASCII structural bytes, so scanning raw bytes
/// is safe. Used to hand each envelope member to the binding exactly once.
final class JsonTopLevel {

    /// Half-open byte range {@code [start, end)} of a member value.
    record Range(int start, int end) {
    }

    private JsonTopLevel() {
    }

    /// @return the value ranges of the top-level members, keyed by member name
    /// @throws IllegalArgumentException if the document is not a JSON object
    static Map<String, Range> scan(byte[] json) {
        int i = skipWs(json, 0);
        if (i >= json.length || json[i] != '{') {
            throw new IllegalArgumentException("Expected a JSON object at the top level");
        }
        Map<String, Range> members = new HashMap<>();
        i = skipWs(json, i + 1);
        if (i < json.length && json[i] == '}') {
            return members;
        }
        while (i < json.length) {
            if (json[i] != '"') {
                throw new IllegalArgumentException("Expected a member name at byte " + i);
            }
            int keyEnd = skipString(json, i);
            String key = new String(json, i + 1, keyEnd - i - 2, StandardCharsets.UTF_8);
            i = skipWs(json, keyEnd);
            if (i >= json.length || json[i] != ':') {
                throw new IllegalArgumentException("Expected ':' at byte " + i);
            }
            int valueStart = skipWs(json, i + 1);
            int valueEnd = skipValue(json, valueStart);
            members.put(key, new Range(valueStart, valueEnd));
            i = skipWs(json, valueEnd);
            if (i >= json.length) {
                break;
            }
            if (json[i] == ',') {
                i = skipWs(json, i + 1);
            } else if (json[i] == '}') {
                return members;
            } else {
                throw new IllegalArgumentException("Expected ',' or '}' at byte " + i);
            }
        }
        throw new IllegalArgumentException("Unterminated JSON object");
    }

    private static int skipWs(byte[] json, int i) {
        while (i < json.length && (json[i] == ' ' || json[i] == '\n' || json[i] == '\r' || json[i] == '\t')) {
            i++;
        }
        return i;
    }

    /// {@code i} points at the opening quote; returns the index after the closing quote.
    private static int skipString(byte[] json, int i) {
        i++;
        while (i < json.length) {
            byte b = json[i];
            if (b == '\\') {
                i += 2;
            } else if (b == '"') {
                return i + 1;
            } else {
                i++;
            }
        }
        throw new IllegalArgumentException("Unterminated string");
    }

    private static int skipValue(byte[] json, int i) {
        if (i >= json.length) {
            throw new IllegalArgumentException("Missing value");
        }
        byte b = json[i];
        if (b == '"') {
            return skipString(json, i);
        }
        if (b == '{' || b == '[') {
            int depth = 0;
            while (i < json.length) {
                byte c = json[i];
                if (c == '"') {
                    i = skipString(json, i);
                    continue;
                }
                if (c == '{' || c == '[') {
                    depth++;
                } else if (c == '}' || c == ']') {
                    depth--;
                    if (depth == 0) {
                        return i + 1;
                    }
                }
                i++;
            }
            throw new IllegalArgumentException("Unterminated object or array");
        }
        // number, true, false, null: runs until a structural byte or whitespace
        while (i < json.length) {
            byte c = json[i];
            if (c == ',' || c == '}' || c == ']' || c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                break;
            }
            i++;
        }
        return i;
    }
}
