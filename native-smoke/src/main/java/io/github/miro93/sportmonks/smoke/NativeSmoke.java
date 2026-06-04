package io.github.miro93.sportmonks.smoke;

import com.sun.net.httpserver.HttpServer;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.football.FootballClient;
import io.github.miro93.sportmonks.football.model.Fixture;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/// Self-contained native-image smoke test: serves a canned SportMonks envelope
/// from an in-process JDK HTTP server, fetches and decodes it through
/// {@link FootballClient}, and asserts the result. Exits 0 on success, 1 on failure.
/// Proves that JDK HttpClient and Jackson decoding work in a native image with the
/// reachability metadata shipped by the library.
public final class NativeSmoke {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        byte[] body = """
                { "data": { "id": 18533878, "name": "Celtic vs Rangers" } }
                """.getBytes(StandardCharsets.UTF_8);
        server.createContext("/fixtures/18533878", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            FootballClient client = FootballClient.builder()
                    .apiToken(ApiToken.of("smoke"))
                    .baseUrl("http://127.0.0.1:" + port)
                    .build();

            Fixture fixture = client.fixtures().byId(18533878L).get().data();

            if (fixture == null || fixture.id() != 18533878L
                    || !"Celtic vs Rangers".equals(fixture.name())) {
                System.err.println("NATIVE SMOKE FAILED: unexpected decode result: " + fixture);
                System.exit(1);
            }
            System.out.println("NATIVE SMOKE OK");
        } finally {
            server.stop(0);
        }
    }
}
