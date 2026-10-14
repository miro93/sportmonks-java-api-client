package io.github.miro93.sportmonks.football.model;

    import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
    import org.junit.jupiter.api.Test;

    import java.util.List;

    import static org.assertj.core.api.Assertions.assertThat;

    class TransferRumourDecodingTest {

        private final HelidonJsonCodec codec = new HelidonJsonCodec();

    @Test
    void decodesDocumentedSampleWithStatusInclude() {
        String json = """
                { "data": [ {
                    "id": 5, "sport_id": 1, "player_id": 34053, "position_id": 27, "from_team_id": 20, "to_team_id": 8,
                    "from_league_id": 8, "to_league_id": 8, "transfer_fee_id": 78365, "status_id": 90319,
                    "probability": "HIGH", "source_name": "The Guardian",
                    "source_url": "https://www.theguardian.com/football/2025/sep/01/liverpool-agree-125m-deal-to-sign-alexander-isak",
                    "amount": 144000000, "currency": "EUR", "date": "2025-06-23", "type_id": 219,
                    "status": { "id": 90319, "name": "Confirmed Rumour", "code": "rumour-confirmed",
                                "developer_name": "RUMOUR_CONFIRMED", "model_type": "rumour-status", "stat_group": null },
                    "player": { "id": 34053, "display_name": "A. Isak" },
                    "fromTeam": { "id": 20, "name": "Newcastle United" },
                    "toTeam": { "id": 8, "name": "Liverpool" }
                } ] }
                """;

        TransferRumour rumour = codec.decode(json, codec.listType(TransferRumour.class)).data().getFirst();

        assertThat(rumour.id()).isEqualTo(5L);
        assertThat(rumour.probability()).isEqualTo("HIGH");
        assertThat(rumour.amount()).isEqualTo(144_000_000L);
        assertThat(rumour.currency()).isEqualTo("EUR");
        assertThat(rumour.typeId()).isEqualTo(219L);
        assertThat(rumour.status().developerName()).isEqualTo("RUMOUR_CONFIRMED");
        assertThat(rumour.player().displayName()).isEqualTo("A. Isak");
        assertThat(rumour.fromTeam().name()).isEqualTo("Newcastle United");
        assertThat(rumour.toTeam().name()).isEqualTo("Liverpool");
        assertThat(rumour.type()).isNull();
    }
}
