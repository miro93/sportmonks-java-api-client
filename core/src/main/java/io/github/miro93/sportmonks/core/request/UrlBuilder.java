package io.github.miro93.sportmonks.core.request;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/// Turns a base URL + {@link RequestSpec} into a SportMonks-style {@link URI}.
/// Structural separators ({@code ;} {@code ,} {@code :}) are kept literal; only the atomic values
/// are percent-encoded.
public final class UrlBuilder {

    private UrlBuilder() {
    }

    public static URI build(String baseUrl, RequestSpec spec) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        StringBuilder url = new StringBuilder(base).append('/').append(spec.path());

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
}
