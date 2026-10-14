package io.github.miro93.sportmonks.core.http;

import java.net.URI;
import java.util.Set;

/// Validates client base URLs: {@code https} always, {@code http} only on loopback hosts
/// (local stubs and proxies), so an API token is never sent in clear text by mistake.
public final class BaseUrls {

    private static final Set<String> LOOPBACK = Set.of("localhost", "127.0.0.1", "::1", "[::1]");

    private BaseUrls() {
    }

    /// @param baseUrl the URL to check
    /// @return {@code baseUrl}, for chaining
    /// @throws IllegalArgumentException if the URL is not https and its host is not loopback
    public static String requireSecure(String baseUrl) {
        URI uri;
        try {
            uri = URI.create(baseUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base URL: " + baseUrl, e);
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        if (scheme.equals("https")) {
            return baseUrl;
        }
        if (scheme.equals("http") && uri.getHost() != null && LOOPBACK.contains(uri.getHost().toLowerCase())) {
            return baseUrl;
        }
        throw new IllegalArgumentException("Base URL must use https (plain http is only allowed on localhost): " + baseUrl);
    }
}
