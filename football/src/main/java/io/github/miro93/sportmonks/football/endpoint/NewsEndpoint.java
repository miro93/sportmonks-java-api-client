package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.football.model.News;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /news} endpoints. Pre-match articles are available
/// at least 48 hours before kick-off; post-match articles right after full time.
public final class NewsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<News>> list;

    /// Creates the endpoint, building the {@link News} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public NewsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(News.class);
    }

    /// Requests every pre-match article, paginated.
    ///
    /// @return a collection request for all pre-match news
    public CollectionRequest<News> preMatch() {
        return collection("news/pre-match");
    }

    /// Requests the pre-match articles of a given season.
    ///
    /// @param seasonId the season id
    /// @return a collection request for that season's pre-match news
    public CollectionRequest<News> preMatchBySeason(long seasonId) {
        return collection("news/pre-match/seasons/" + seasonId);
    }

    /// Requests the pre-match articles for upcoming fixtures.
    ///
    /// @return a collection request for upcoming fixtures' pre-match news
    public CollectionRequest<News> preMatchUpcoming() {
        return collection("news/pre-match/upcoming");
    }

    /// Requests every post-match article, paginated.
    ///
    /// @return a collection request for all post-match news
    public CollectionRequest<News> postMatch() {
        return collection("news/post-match");
    }

    /// Requests the post-match articles of a given season.
    ///
    /// @param seasonId the season id
    /// @return a collection request for that season's post-match news
    public CollectionRequest<News> postMatchBySeason(long seasonId) {
        return collection("news/post-match/seasons/" + seasonId);
    }

    private CollectionRequest<News> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
