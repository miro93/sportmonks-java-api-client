module io.github.miro93.sportmonks.core {
    requires transitive io.helidon.json.binding;
    requires io.helidon.common;
    requires io.helidon.common.buffers;
    requires io.helidon.service.registry;
    requires java.net.http;

    // Public API
    exports io.github.miro93.sportmonks.core.auth;
    exports io.github.miro93.sportmonks.core.error;
    exports io.github.miro93.sportmonks.core.paging;
    exports io.github.miro93.sportmonks.core.request;
    exports io.github.miro93.sportmonks.core.response;
    exports io.github.miro93.sportmonks.core.retry;
    exports io.github.miro93.sportmonks.core.coreapi;
    exports io.github.miro93.sportmonks.core.coreapi.endpoint;
    exports io.github.miro93.sportmonks.core.coreapi.model;

    // Internal plumbing — visible only to the football module
    exports io.github.miro93.sportmonks.core to io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.core.http to io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.core.json to io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.core.request.internal to io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.core.retry.internal to io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.core.error.internal to io.github.miro93.sportmonks.football;

    // helidon-service-codegen reflects into the generated *__ServiceDescriptor classes to
    // register @Json.Entity converters at JsonBinding.create() time.
    opens io.github.miro93.sportmonks.core.json to io.helidon.service.registry;
}
