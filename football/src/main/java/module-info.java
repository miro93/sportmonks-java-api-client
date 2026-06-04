module io.github.miro93.sportmonks.football {
    requires transitive io.github.miro93.sportmonks.core;
    requires java.net.http;

    exports io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.football.endpoint;
    exports io.github.miro93.sportmonks.football.model;

    opens io.github.miro93.sportmonks.football.model to tools.jackson.databind;
}
