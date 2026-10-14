module io.github.miro93.sportmonks.motorsport {
    requires transitive io.github.miro93.sportmonks.core;
    requires io.helidon.common.buffers;
    requires io.helidon.service.registry;
    requires transitive java.net.http;

    exports io.github.miro93.sportmonks.motorsport;
    exports io.github.miro93.sportmonks.motorsport.endpoint;
    exports io.github.miro93.sportmonks.motorsport.model;
}
