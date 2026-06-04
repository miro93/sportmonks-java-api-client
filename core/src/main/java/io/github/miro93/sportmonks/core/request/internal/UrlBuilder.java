package io.github.miro93.sportmonks.core.request.internal;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/// Turns a base URL + {@link RequestSpec} into a SportMonks-style {@link URI}.
/// Path segments are percent-encoded individually (so a value such as a search term may
/// contain spaces or reserved characters), while the {@code /} separators between segments and
/// the {@code ,} used in comma-list segments (e.g. multi-id paths) are kept literal.
/// In the query string, structural separators ({@code ;} {@code ,} {@code :}) are likewise kept
/// literal; only the atomic values are percent-encoded.
public final class UrlBuilder {

    private UrlBuilder() {
    }

    public static URI build(String baseUrl, RequestSpec spec) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        StringBuilder url = new StringBuilder(base).append('/').append(encodePath(spec.path()));

        List<String> params = new ArrayList<>();

        if (!spec.includes().isEmpty()) {
            params.add("include=" + spec.includes().stream()
                    .map(UrlBuilder::encode)
                    .collect(Collectors.joining(";")));
        }
        if (!spec.filters().isEmpty()) {
            String filters = spec.filters().entrySet().stream()
                    .map(e -> encode(e.getKey()) + ":" + e.getValue().stream()
                            .map(UrlBuilder::encode)
                            .collect(Collectors.joining(",")))
                    .collect(Collectors.joining(";"));
            params.add("filters=" + filters);
        }
        if (!spec.select().isEmpty()) {
            params.add("select=" + spec.select().stream()
                    .map(UrlBuilder::encode)
                    .collect(Collectors.joining(",")));
        }
        if (!spec.sort().isEmpty()) {
            params.add("sort=" + spec.sort().stream()
                    .map(UrlBuilder::encode)
                    .collect(Collectors.joining(",")));
        }
        if (spec.page() != null) {
            params.add("page=" + spec.page());
        }

        if (!params.isEmpty()) {
            url.append('?').append(String.join("&", params));
        }
        return URI.create(url.toString());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /// Percent-encodes each {@code /}-separated path segment, preserving the slashes between
    /// segments and any literal commas within a segment (the multi-id / comma-list separator).
    private static String encodePath(String path) {
        return Arrays.stream(path.split("/", -1))
                .map(UrlBuilder::encodePathSegment)
                .collect(Collectors.joining("/"));
    }

    /// Percent-encodes a single path segment. Unlike form encoding, spaces become {@code %20}
    /// (not {@code +}), and the comma sub-delimiter is left literal so comma-separated id lists
    /// stay readable and RFC 3986-valid.
    private static String encodePathSegment(String segment) {
        return URLEncoder.encode(segment, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%2C", ",");
    }
}
