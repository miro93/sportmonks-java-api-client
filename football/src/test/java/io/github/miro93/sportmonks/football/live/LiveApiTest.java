package io.github.miro93.sportmonks.football.live;

import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.SportmonksException;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import io.github.miro93.sportmonks.football.FootballClient;
import io.github.miro93.sportmonks.football.model.Fixture;
import io.github.miro93.sportmonks.football.model.League;
import io.github.miro93.sportmonks.football.model.Team;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.abort;

/// Smoke calls against the live SportMonks API, one per endpoint group, asserting that
/// the real payloads decode (not their values). Runs only with {@code SPORTMONKS_API_TOKEN}
/// set, via {@code ./gradlew :football:liveTest} or the manual "Live API" workflow; the
/// regular {@code test} task excludes the {@code live} tag. Endpoints outside the
/// subscription (402/403) are reported as skipped, not failed, so a free plan covering
/// the Danish Superliga (league 271) and Scottish Premiership (501) is enough.
@Tag("live")
@EnabledIfEnvironmentVariable(named = "SPORTMONKS_API_TOKEN", matches = ".+")
class LiveApiTest {

    private static final long DANISH_SUPERLIGA = 271L;

    private static final FootballClient CLIENT = FootballClient.builder()
            .apiToken(ApiToken.of(System.getenv("SPORTMONKS_API_TOKEN")))
            .build();

    static Stream<Arguments> endpointGroups() {
        return Stream.of(
                group("core countries", c -> c.core().countries().all().get()),
                group("core types by entity", c -> c.core().types().byEntity().get()),
                group("fixtures latest", c -> c.fixtures().latest().get()),
                group("livescores", c -> c.livescores().all().get()),
                group("leagues", c -> c.leagues().all().get()),
                group("seasons", c -> c.seasons().all().get()),
                group("stages", c -> c.stages().all().get()),
                group("rounds", c -> c.rounds().all().get()),
                group("standings", c -> c.standings().all().get()),
                group("teams", c -> c.teams().all().get()),
                group("players", c -> c.players().all().get()),
                group("coaches", c -> c.coaches().all().get()),
                group("referees", c -> c.referees().all().get()),
                group("venues", c -> c.venues().all().get()),
                group("states", c -> c.states().all().get()),
                group("tv stations", c -> c.tvStations().all().get()),
                group("transfers", c -> c.transfers().all().get()),
                group("commentaries", c -> c.commentaries().all().get()),
                group("bookmakers", c -> c.bookmakers().all().get()),
                group("markets", c -> c.markets().all().get()),
                group("pre-match odds", c -> c.preMatchOdds().all().get()),
                group("inplay odds", c -> c.inplayOdds().all().get()),
                group("premium odds", c -> c.premiumOdds().all().get()),
                group("premium bookmakers (odds base URL)", c -> c.premiumBookmakers().all().get()),
                group("expected (xG)", c -> c.expected().fixtures().get()),
                group("predictions probabilities", c -> c.predictions().probabilities().get()),
                group("predictions live", c -> c.predictions().liveProbabilities().get()),
                group("rivals", c -> c.rivals().all().get()),
                group("news pre-match", c -> c.news().preMatch().get()),
                group("team rankings", c -> c.teamRankings().all().get()),
                group("team of the week", c -> c.teamOfTheWeek().all().get()),
                group("match facts", c -> c.matchFacts().all().get()),
                group("transfer rumours", c -> c.transferRumours().all().get()),
                group("transfer rumours config", c -> c.transferRumours().config().get()));
    }

    private static Arguments group(String name, Function<FootballClient, ApiResponse<?>> call) {
        return Arguments.of(name, call);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpointGroups")
    void endpointGroupDecodes(String name, Function<FootballClient, ApiResponse<?>> call) {
        ApiResponse<?> response = callOrSkip(() -> call.apply(CLIENT));

        assertThat(response.data()).as(name).isNotNull();
    }

    @Test
    void fixtureWithIncludesAndTypedDates() {
        LocalDate today = LocalDate.now();
        List<Fixture> fixtures = callOrSkip(() -> CLIENT.fixtures()
                .byDateRange(today.minusDays(14), today)
                .include("participants", "scores", "state")
                .filter("fixtureLeagues", String.valueOf(DANISH_SUPERLIGA))
                .perPage(5)
                .get()).data();

        assertThat(fixtures).isNotEmpty();
        Fixture first = fixtures.getFirst();
        assertThat(first.startingAtDateTime()).isNotNull();
        assertThat(first.participants()).hasSize(2);
        assertThat(first.state()).isNotNull();
    }

    @Test
    void leagueByIdWithSortByAndOrder() {
        League league = callOrSkip(() -> CLIENT.leagues().byId(DANISH_SUPERLIGA).get()).data();
        List<League> sorted = callOrSkip(() -> CLIENT.leagues().all().sortBy("name").perPage(50).get()).data();

        assertThat(league.id()).isEqualTo(DANISH_SUPERLIGA);
        assertThat(sorted).extracting(League::name).isSorted();
    }

    @Test
    void streamFollowsCursorPagination() {
        List<Team> teams = callOrSkip(() -> CLIENT.teams().all().perPage(10).stream().limit(25).toList());

        assertThat(teams).isNotEmpty();
    }

    /// Runs the call; aborts the test (reported as skipped) when the subscription does not
    /// cover the endpoint.
    private static <T> T callOrSkip(java.util.function.Supplier<T> call) {
        try {
            return call.get();
        } catch (SportmonksException e) {
            if (e.statusCode() == 402 || e.statusCode() == 403) {
                return abort("not in subscription: " + e.getMessage());
            }
            throw e;
        }
    }
}
