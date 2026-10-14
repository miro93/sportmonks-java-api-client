package io.github.miro93.sportmonks.smoke;

import com.sun.net.httpserver.HttpServer;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.City;
import io.github.miro93.sportmonks.core.coreapi.model.Continent;
import io.github.miro93.sportmonks.core.coreapi.model.Country;
import io.github.miro93.sportmonks.core.coreapi.model.EntityTypes;
import io.github.miro93.sportmonks.core.coreapi.model.Region;
import io.github.miro93.sportmonks.core.coreapi.model.Type;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.response.Pagination;
import io.github.miro93.sportmonks.core.response.RateLimit;
import io.github.miro93.sportmonks.football.FootballClient;
import io.github.miro93.sportmonks.football.model.Bookmaker;
import io.github.miro93.sportmonks.football.model.BookmakerMapping;
import io.github.miro93.sportmonks.football.model.Bracket;
import io.github.miro93.sportmonks.football.model.BracketEdge;
import io.github.miro93.sportmonks.football.model.BracketStage;
import io.github.miro93.sportmonks.football.model.Coach;
import io.github.miro93.sportmonks.football.model.Commentary;
import io.github.miro93.sportmonks.football.model.Event;
import io.github.miro93.sportmonks.football.model.Expected;
import io.github.miro93.sportmonks.football.model.ExpectedLineup;
import io.github.miro93.sportmonks.football.model.Fixture;
import io.github.miro93.sportmonks.football.model.HistoricalOdd;
import io.github.miro93.sportmonks.football.model.League;
import io.github.miro93.sportmonks.football.model.Market;
import io.github.miro93.sportmonks.football.model.MatchFact;
import io.github.miro93.sportmonks.football.model.News;
import io.github.miro93.sportmonks.football.model.Odd;
import io.github.miro93.sportmonks.football.model.Participant;
import io.github.miro93.sportmonks.football.model.ParticipantMeta;
import io.github.miro93.sportmonks.football.model.Player;
import io.github.miro93.sportmonks.football.model.Predictability;
import io.github.miro93.sportmonks.football.model.Prediction;
import io.github.miro93.sportmonks.football.model.PremiumOdd;
import io.github.miro93.sportmonks.football.model.Referee;
import io.github.miro93.sportmonks.football.model.Rival;
import io.github.miro93.sportmonks.football.model.Round;
import io.github.miro93.sportmonks.football.model.Score;
import io.github.miro93.sportmonks.football.model.ScoreDetail;
import io.github.miro93.sportmonks.football.model.Season;
import io.github.miro93.sportmonks.football.model.Squad;
import io.github.miro93.sportmonks.football.model.Stage;
import io.github.miro93.sportmonks.football.model.Standing;
import io.github.miro93.sportmonks.football.model.StandingDetail;
import io.github.miro93.sportmonks.football.model.State;
import io.github.miro93.sportmonks.football.model.Statistic;
import io.github.miro93.sportmonks.football.model.StatisticDetail;
import io.github.miro93.sportmonks.football.model.Team;
import io.github.miro93.sportmonks.football.model.TeamOfTheWeekEntry;
import io.github.miro93.sportmonks.football.model.TeamRanking;
import io.github.miro93.sportmonks.football.model.Topscorer;
import io.github.miro93.sportmonks.football.model.Transfer;
import io.github.miro93.sportmonks.football.model.TransferRumour;
import io.github.miro93.sportmonks.football.model.TransferRumourConfig;
import io.github.miro93.sportmonks.football.model.TvStation;
import io.github.miro93.sportmonks.football.model.ValueBet;
import io.github.miro93.sportmonks.football.model.ValueBetPrediction;
import io.github.miro93.sportmonks.football.model.Venue;
import io.github.miro93.sportmonks.motorsport.model.Driver;
import io.github.miro93.sportmonks.motorsport.model.Lap;
import io.github.miro93.sportmonks.motorsport.model.Lineup;
import io.github.miro93.sportmonks.motorsport.model.LineupDetail;
import io.github.miro93.sportmonks.motorsport.model.Pitstop;
import io.github.miro93.sportmonks.motorsport.model.Stint;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/// Self-contained native-image smoke test. Serves a canned SportMonks envelope from an
/// in-process JDK HTTP server, fetches and decodes it through {@link FootballClient}
/// (proves JDK HttpClient + Helidon JSON in a native image), then decodes a minimal
/// sample of every public {@code @Json.Entity} (proves each generated converter is
/// reachable with the metadata shipped by the library). Exits 0 on success, 1 on failure.
///
/// The {@code checkSmokeEntities} Gradle task fails the build when a converter exists for
/// an entity missing from {@link #ENTITIES}.
public final class NativeSmoke {

    /// Every public entity with a generated converter: core, football, then motorsport.
    static final List<Class<?>> ENTITIES = List.of(
            City.class, Continent.class, Country.class, EntityTypes.class, Region.class, Type.class,
            Pagination.class, RateLimit.class,
            Bookmaker.class, BookmakerMapping.class, Bracket.class, BracketEdge.class, BracketStage.class,
            Coach.class, Commentary.class, Event.class, Expected.class, ExpectedLineup.class, Fixture.class,
            HistoricalOdd.class, League.class, Market.class, MatchFact.class, News.class, Odd.class,
            Participant.class, ParticipantMeta.class, Player.class, Predictability.class, Prediction.class,
            PremiumOdd.class, Referee.class, Rival.class, Round.class, Score.class, ScoreDetail.class,
            Season.class, Squad.class, Stage.class, Standing.class, StandingDetail.class, State.class,
            Statistic.class, StatisticDetail.class, Team.class, TeamOfTheWeekEntry.class, TeamRanking.class,
            Topscorer.class, Transfer.class, TransferRumour.class, TransferRumourConfig.class,
            TvStation.class, ValueBet.class, ValueBetPrediction.class, Venue.class,
            // motorsport (names clashing with football models are fully qualified)
            Driver.class, io.github.miro93.sportmonks.motorsport.model.Fixture.class, Lap.class,
            io.github.miro93.sportmonks.motorsport.model.League.class, Lineup.class, LineupDetail.class, Pitstop.class,
            io.github.miro93.sportmonks.motorsport.model.Season.class, io.github.miro93.sportmonks.motorsport.model.Stage.class,
            io.github.miro93.sportmonks.motorsport.model.Standing.class, io.github.miro93.sportmonks.motorsport.model.State.class,
            Stint.class, io.github.miro93.sportmonks.motorsport.model.Team.class,
            io.github.miro93.sportmonks.motorsport.model.Venue.class);

    public static void main(String[] args) throws Exception {
        smokeHttpAndDecode();
        smokeEveryEntity();
        System.out.println("NATIVE SMOKE OK");
    }

    private static void smokeHttpAndDecode() throws Exception {
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
                fail("unexpected decode result: " + fixture);
            }
        } finally {
            server.stop(0);
        }
    }

    /// Decodes an empty object as each entity (every member is optional or defaults, so the
    /// sample fits every record) and the entity-keyed map of {@code types/entities}.
    private static void smokeEveryEntity() {
        HelidonJsonCodec codec = new HelidonJsonCodec();
        for (Class<?> entity : ENTITIES) {
            Object decoded;
            try {
                decoded = codec.decode("{ \"data\": { } }", codec.type(entity)).data();
            } catch (RuntimeException e) {
                fail("cannot decode " + entity.getSimpleName() + ": " + e);
                return;
            }
            if (decoded == null || !entity.isInstance(decoded)) {
                fail("decoded " + entity.getSimpleName() + " as " + decoded);
            }
        }
        Map<String, EntityTypes> byEntity = codec.decode(
                "{ \"data\": { \"Event\": { \"updated_at\": \"x\", \"types\": [ { \"id\": 1 } ] } } }",
                codec.mapType(EntityTypes.class)).data();
        if (byEntity == null || byEntity.get("Event") == null || byEntity.get("Event").types().size() != 1) {
            fail("entity-keyed map decoded as " + byEntity);
        }
    }

    private static void fail(String message) {
        System.err.println("NATIVE SMOKE FAILED: " + message);
        System.exit(1);
    }
}
