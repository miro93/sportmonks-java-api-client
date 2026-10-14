package io.github.miro93.sportmonks.football.endpoint;

/// The include names each endpoint group documents, as constants for {@code include(...)}.
/// Values are the exact strings the API expects (camelCase); they were extracted from the
/// "Include options" section of every endpoint page on 2026-10-14. Nest with
/// {@link #nested(String...)}: {@code nested(Fixtures.EVENTS, "player")} is {@code "events.player"}.
public final class Includes {

    private Includes() {
    }

    /// Joins include segments with {@code .} for a nested include.
    ///
    /// @param segments the path of relations, outermost first
    /// @return the dotted include name
    public static String nested(String... segments) {
        return String.join(".", segments);
    }

    /// Includes documented on the {@code /fixtures and /livescores} endpoints.
    public static final class Fixtures {

        private Fixtures() {
        }

        public static final String AI_OVERVIEWS = "AIOverviews";
        public static final String AGGREGATE = "aggregate";
        public static final String BALL_COORDINATES = "ballCoordinates";
        public static final String COACHES = "coaches";
        public static final String COMMENTS = "comments";
        public static final String CURRENT_PERIOD = "currentPeriod";
        public static final String EVENTS = "events";
        public static final String EXPECTED_LINEUPS = "expectedLineups";
        public static final String FORMATIONS = "formations";
        public static final String GROUP = "group";
        public static final String INPLAY_ODDS = "inplayOdds";
        public static final String LEAGUE = "league";
        public static final String LINEUPS = "lineups";
        public static final String MATCHFACTS = "matchfacts";
        public static final String METADATA = "metadata";
        public static final String ODDS = "odds";
        public static final String PARTICIPANTS = "participants";
        public static final String PERIODS = "periods";
        public static final String POSTMATCH_NEWS = "postmatchNews";
        public static final String PREDICTED_LINEUPS = "predictedLineups";
        public static final String PREDICTIONS = "predictions";
        public static final String PREMATCH_NEWS = "prematchNews";
        public static final String PREMIUM_ODDS = "premiumOdds";
        public static final String PRESSURE = "pressure";
        public static final String REFEREES = "referees";
        public static final String ROUND = "round";
        public static final String SCORES = "scores";
        public static final String SEASON = "season";
        public static final String SIDELINED = "sidelined";
        public static final String SPORT = "sport";
        public static final String STAGE = "stage";
        public static final String STATE = "state";
        public static final String STATISTICS = "statistics";
        public static final String TIMELINE = "timeline";
        public static final String TRENDS = "trends";
        public static final String TV_STATIONS = "tvStations";
        public static final String VENUE = "venue";
        public static final String WEATHER_REPORT = "weatherReport";
        public static final String XG_FIXTURE = "xGFixture";
    }

    /// Includes documented on the {@code /leagues} endpoints.
    public static final class Leagues {

        private Leagues() {
        }

        public static final String COUNTRY = "country";
        public static final String CURRENT_SEASON = "currentSeason";
        public static final String INPLAY = "inplay";
        public static final String LATEST = "latest";
        public static final String SEASONS = "seasons";
        public static final String SPORT = "sport";
        public static final String STAGES = "stages";
        public static final String TODAY = "today";
        public static final String UPCOMING = "upcoming";
    }

    /// Includes documented on the {@code /seasons} endpoints.
    public static final class Seasons {

        private Seasons() {
        }

        public static final String CURRENT_STAGE = "currentStage";
        public static final String FIXTURES = "fixtures";
        public static final String GROUPS = "groups";
        public static final String LEAGUE = "league";
        public static final String SPORT = "sport";
        public static final String STAGES = "stages";
        public static final String STATISTICS = "statistics";
        public static final String TEAMS = "teams";
        public static final String TOPSCORERS = "topscorers";
    }

    /// Includes documented on the {@code /stages} endpoints.
    public static final class Stages {

        private Stages() {
        }

        public static final String AGGREGATES = "aggregates";
        public static final String CURRENT_ROUND = "currentRound";
        public static final String FIXTURES = "fixtures";
        public static final String GROUPS = "groups";
        public static final String LEAGUE = "league";
        public static final String ROUNDS = "rounds";
        public static final String SEASON = "season";
        public static final String SPORT = "sport";
        public static final String STATISTICS = "statistics";
        public static final String TOPSCORERS = "topscorers";
        public static final String TYPE = "type";
    }

    /// Includes documented on the {@code /rounds} endpoints.
    public static final class Rounds {

        private Rounds() {
        }

        public static final String FIXTURES = "fixtures";
        public static final String LEAGUE = "league";
        public static final String SEASON = "season";
        public static final String SPORT = "sport";
        public static final String STAGE = "stage";
        public static final String STATISTICS = "statistics";
    }

    /// Includes documented on the {@code /standings} endpoints.
    public static final class Standings {

        private Standings() {
        }

        public static final String DETAILS = "details";
        public static final String FORM = "form";
        public static final String GROUP = "group";
        public static final String LEAGUE = "league";
        public static final String PARTICIPANT = "participant";
        public static final String ROUND = "round";
        public static final String RULE = "rule";
        public static final String SEASON = "season";
        public static final String SPORT = "sport";
        public static final String STAGE = "stage";
    }

    /// Includes documented on the {@code /teams} endpoints.
    public static final class Teams {

        private Teams() {
        }

        public static final String ACTIVE_SEASONS = "activeSeasons";
        public static final String COACHES = "coaches";
        public static final String COUNTRY = "country";
        public static final String LATEST = "latest";
        public static final String PLAYERS = "players";
        public static final String RANKINGS = "rankings";
        public static final String RIVALS = "rivals";
        public static final String SEASONS = "seasons";
        public static final String SIDELINED = "sidelined";
        public static final String SIDELINED_HISTORY = "sidelinedHistory";
        public static final String SOCIALS = "socials";
        public static final String SPORT = "sport";
        public static final String STATISTICS = "statistics";
        public static final String TROPHIES = "trophies";
        public static final String UPCOMING = "upcoming";
        public static final String VENUE = "venue";
    }

    /// Includes documented on the {@code /players} endpoints.
    public static final class Players {

        private Players() {
        }

        public static final String CITY = "city";
        public static final String COUNTRY = "country";
        public static final String DETAILED_POSITION = "detailedPosition";
        public static final String LATEST = "latest";
        public static final String LINEUPS = "lineups";
        public static final String METADATA = "metadata";
        public static final String NATIONALITY = "nationality";
        public static final String PENDING_TRANSFERS = "pendingTransfers";
        public static final String POSITION = "position";
        public static final String SPORT = "sport";
        public static final String STATISTICS = "statistics";
        public static final String TEAMS = "teams";
        public static final String TRANSFERS = "transfers";
        public static final String TROPHIES = "trophies";
    }

    /// Includes documented on the {@code /coaches} endpoints.
    public static final class Coaches {

        private Coaches() {
        }

        public static final String COUNTRY = "country";
        public static final String FIXTURES = "fixtures";
        public static final String LATEST = "latest";
        public static final String NATIONALITY = "nationality";
        public static final String PLAYER = "player";
        public static final String SPORT = "sport";
        public static final String STATISTICS = "statistics";
        public static final String TEAMS = "teams";
        public static final String TROPHIES = "trophies";
    }

    /// Includes documented on the {@code /referees} endpoints.
    public static final class Referees {

        private Referees() {
        }

        public static final String CITY = "city";
        public static final String COUNTRY = "country";
        public static final String LATEST = "latest";
        public static final String NATIONALITY = "nationality";
        public static final String SPORT = "sport";
        public static final String STATISTICS = "statistics";
    }

    /// Includes documented on the {@code /venues} endpoints.
    public static final class Venues {

        private Venues() {
        }

        public static final String CITY = "city";
        public static final String COUNTRY = "country";
        public static final String FIXTURES = "fixtures";
    }

    /// Includes documented on the {@code /tv-stations} endpoints.
    public static final class TvStations {

        private TvStations() {
        }

        public static final String COUNTRIES = "countries";
        public static final String FIXTURES = "fixtures";
    }

    /// Includes documented on the {@code /squads} endpoints.
    public static final class Squads {

        private Squads() {
        }

        public static final String DETAILED_POSITION = "detailedPosition";
        public static final String PLAYER = "player";
        public static final String POSITION = "position";
        public static final String TEAM = "team";
        public static final String TRANSFER = "transfer";
    }

    /// Includes documented on the {@code /transfers} endpoints.
    public static final class Transfers {

        private Transfers() {
        }

        public static final String DETAILED_POSITION = "detailedPosition";
        public static final String FROM_TEAM = "fromTeam";
        public static final String PLAYER = "player";
        public static final String POSITION = "position";
        public static final String SPORT = "sport";
        public static final String TO_TEAM = "toTeam";
        public static final String TYPE = "type";
    }

    /// Includes documented on the {@code /transfer-rumours} endpoints.
    public static final class TransferRumours {

        private TransferRumours() {
        }

        public static final String DETAILED_POSITION = "detailedPosition";
        public static final String FROM_TEAM = "fromTeam";
        public static final String PLAYER = "player";
        public static final String POSITION = "position";
        public static final String SPORT = "sport";
        public static final String STATUS = "status";
        public static final String TO_TEAM = "toTeam";
        public static final String TYPE = "type";
    }

    /// Includes documented on the {@code /topscorers} endpoints.
    public static final class Topscorers {

        private Topscorers() {
        }

        public static final String PARTICIPANT = "participant";
        public static final String PLAYER = "player";
        public static final String SEASON = "season";
        public static final String STAGE = "stage";
        public static final String TEAM = "team";
        public static final String TYPE = "type";
    }

    /// Includes documented on the {@code /statistics} endpoints.
    public static final class Statistics {

        private Statistics() {
        }

        public static final String COACH = "coach";
        public static final String PLAYER = "player";
        public static final String POSITION = "position";
        public static final String REFEREE = "referee";
        public static final String SEASON = "season";
        public static final String TEAM = "team";
    }

    /// Includes documented on the {@code /predictions} endpoints.
    public static final class Predictions {

        private Predictions() {
        }

        public static final String FIXTURE = "fixture";
        public static final String TYPE = "type";
    }

    /// Includes documented on the {@code /expected} endpoints.
    public static final class Expected {

        private Expected() {
        }

        public static final String FIXTURE = "fixture";
        public static final String PARTICIPANT = "participant";
        public static final String PLAYER = "player";
        public static final String TEAM = "team";
        public static final String TYPE = "type";
    }

    /// Includes documented on the {@code /commentaries} endpoints.
    public static final class Commentaries {

        private Commentaries() {
        }

        public static final String FIXTURE = "fixture";
        public static final String PLAYER = "player";
        public static final String RELATED_PLAYER = "relatedPlayer";
    }

    /// Includes documented on the {@code /news} endpoints.
    public static final class News {

        private News() {
        }

        public static final String FIXTURE = "fixture";
        public static final String LEAGUE = "league";
        public static final String LINES = "lines";
    }

    /// Includes documented on the {@code /rivals} endpoints.
    public static final class Rivals {

        private Rivals() {
        }

        public static final String RIVAL = "rival";
        public static final String TEAM = "team";
    }

    /// Includes documented on the {@code /match-facts} endpoints.
    public static final class MatchFacts {

        private MatchFacts() {
        }

        public static final String FIXTURE = "fixture";
        public static final String SPORT = "sport";
        public static final String TYPE = "type";
    }

    /// Includes documented on the {@code /team-of-the-week} endpoints.
    public static final class TeamOfTheWeek {

        private TeamOfTheWeek() {
        }

        public static final String FIXTURE = "fixture";
        public static final String PLAYER = "player";
        public static final String ROUND = "round";
        public static final String TEAM = "team";
    }

    /// Includes documented on the {@code /team-rankings} endpoints.
    public static final class TeamRankings {

        private TeamRankings() {
        }

        public static final String TEAM = "team";
    }

    /// Includes documented on the {@code /odds (standard feed)} endpoints.
    public static final class Odds {

        private Odds() {
        }

        public static final String BOOKMAKER = "bookmaker";
        public static final String FIXTURE = "fixture";
        public static final String MARKET = "market";
    }

    /// Includes documented on the {@code /odds/premium} endpoints.
    public static final class PremiumOdds {

        private PremiumOdds() {
        }

        public static final String ODD = "odd";
    }
}
