package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.coreapi.model.Type;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransferRumourConfigDecodingTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    @Test
    void decodesEnvelopeLessConfig() {
        String json = """
                { "fee": [ { "id": 78365, "name": "Confirmed Amount", "code": "confirmed-amount",
                             "developer_name": "CONFIRMED_AMOUNT", "model_type": "fee", "stat_group": null } ],
                  "transfers": [ { "id": 218, "name": "Loan", "code": "loan-transfer", "developer_name": "LOAN_TRANSFER" },
                                 { "id": 219, "name": "Transfer", "code": "transfer", "developer_name": "TRANSFER" } ],
                  "statuses": [ { "id": 90319, "name": "Confirmed Rumour", "code": "rumour-confirmed",
                                  "developer_name": "RUMOUR_CONFIRMED" } ] }
                """;

        TransferRumourConfig config = codec.decode(json, codec.rawType(TransferRumourConfig.class)).data();

        assertThat(config.fee()).extracting(Type::developerName).containsExactly("CONFIRMED_AMOUNT");
        assertThat(config.transfers()).extracting(Type::id).containsExactly(218L, 219L);
        assertThat(config.statuses().getFirst().code()).isEqualTo("rumour-confirmed");
    }
}
