module io.github.miro93.sportmonks.football {
    requires transitive io.github.miro93.sportmonks.core;
    requires io.helidon.common.buffers;
    requires io.helidon.service.registry;
    requires java.net.http;

    exports io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.football.endpoint;
    exports io.github.miro93.sportmonks.football.model;
}
