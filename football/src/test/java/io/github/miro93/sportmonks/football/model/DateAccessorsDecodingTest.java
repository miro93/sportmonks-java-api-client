package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateAccessorsDecodingTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private <T> T decode(String json, Class<T> type) {
        return codec.decode("{ \"data\": " + json + " }", codec.type(type)).data();
    }

    @Test
    void fixtureExposesDateTimeAndInstant() {
        Fixture fixture = decode("""
                { "id": 1, "starting_at": "2026-06-28 19:00:00", "starting_at_timestamp": 1782673200 }
                """, Fixture.class);

        assertThat(fixture.startingAtDateTime()).isEqualTo(LocalDateTime.of(2026, 6, 28, 19, 0));
        assertThat(fixture.startingAtInstant()).isEqualTo(Instant.ofEpochSecond(1782673200L));
    }

    @Test
    void absentFieldsYieldNull() {
        Fixture fixture = decode("{ \"id\": 1 }", Fixture.class);
        Player player = decode("{ \"id\": 1 }", Player.class);

        assertThat(fixture.startingAtDateTime()).isNull();
        assertThat(fixture.startingAtInstant()).isNull();
        assertThat(player.dateOfBirthDate()).isNull();
    }

    @Test
    void dateOnlyAccessors() {
        assertThat(decode("{ \"id\": 1, \"date_of_birth\": \"2000-07-18\" }", Player.class).dateOfBirthDate())
                .isEqualTo(LocalDate.of(2000, 7, 18));
        assertThat(decode("{ \"id\": 1, \"date_of_birth\": \"1980-01-02\" }", Coach.class).dateOfBirthDate())
                .isEqualTo(LocalDate.of(1980, 1, 2));
        assertThat(decode("{ \"id\": 1, \"date_of_birth\": \"1975-03-04\" }", Referee.class).dateOfBirthDate())
                .isEqualTo(LocalDate.of(1975, 3, 4));
        Season season = decode("{ \"id\": 1, \"starting_at\": \"2023-07-29\", \"ending_at\": \"2024-05-19\" }", Season.class);
        assertThat(season.startingAtDate()).isEqualTo(LocalDate.of(2023, 7, 29));
        assertThat(season.endingAtDate()).isEqualTo(LocalDate.of(2024, 5, 19));
        assertThat(decode("{ \"id\": 1, \"starting_at\": \"2023-07-29\" }", Round.class).startingAtDate())
                .isEqualTo(LocalDate.of(2023, 7, 29));
        assertThat(decode("{ \"id\": 1, \"ending_at\": \"2023-08-06\" }", Stage.class).endingAtDate())
                .isEqualTo(LocalDate.of(2023, 8, 6));
        assertThat(decode("{ \"id\": 1, \"date\": \"2024-01-15\" }", Transfer.class).localDate())
                .isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(decode("{ \"id\": 1, \"date\": \"2025-06-23\" }", TransferRumour.class).localDate())
                .isEqualTo(LocalDate.of(2025, 6, 23));
        assertThat(decode("{ \"id\": 1, \"date\": \"2005-07-02\" }", TeamRanking.class).localDate())
                .isEqualTo(LocalDate.of(2005, 7, 2));
    }

    @Test
    void lastPlayedAtAccessors() {
        LocalDateTime expected = LocalDateTime.of(2024, 5, 19, 15, 0);
        assertThat(decode("{ \"id\": 1, \"last_played_at\": \"2024-05-19 15:00:00\" }", Team.class).lastPlayedAtDateTime())
                .isEqualTo(expected);
        assertThat(decode("{ \"id\": 1, \"last_played_at\": \"2024-05-19 15:00:00\" }", Participant.class).lastPlayedAtDateTime())
                .isEqualTo(expected);
        assertThat(decode("{ \"id\": 1, \"last_played_at\": \"2024-05-19 15:00:00\" }", League.class).lastPlayedAtDateTime())
                .isEqualTo(expected);
    }

    @Test
    void dateTimeParserAcceptsDateOnlyAndRejectsGarbage() {
        assertThat(ApiDates.dateTime("2024-05-19")).isEqualTo(LocalDate.of(2024, 5, 19).atStartOfDay());
        assertThatThrownBy(() -> ApiDates.dateTime("19/05/2024")).isInstanceOf(DateTimeParseException.class);
        assertThatThrownBy(() -> ApiDates.date("2024-05-19 15:00:00")).isInstanceOf(DateTimeParseException.class);
    }
}
