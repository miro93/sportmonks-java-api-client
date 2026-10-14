package io.github.miro93.sportmonks.football.endpoint;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class IncludesTest {

    @Test
    void constantsAreTheDocumentedCamelCaseNames() {
        assertThat(Includes.Fixtures.PARTICIPANTS).isEqualTo("participants");
        assertThat(Includes.Fixtures.TV_STATIONS).isEqualTo("tvStations");
        assertThat(Includes.Fixtures.XG_FIXTURE).isEqualTo("xGFixture");
        assertThat(Includes.Fixtures.AI_OVERVIEWS).isEqualTo("AIOverviews");
        assertThat(Includes.Teams.ACTIVE_SEASONS).isEqualTo("activeSeasons");
        assertThat(Includes.TransferRumours.FROM_TEAM).isEqualTo("fromTeam");
    }

    @Test
    void nestedJoinsWithDots() {
        assertThat(Includes.nested(Includes.Fixtures.EVENTS, "player")).isEqualTo("events.player");
        assertThat(Includes.nested(Includes.Fixtures.LINEUPS, "details", "type")).isEqualTo("lineups.details.type");
    }

    @Test
    void everyConstantIsAPlainIncludeName() {
        for (Class<?> group : Includes.class.getDeclaredClasses()) {
            for (Field field : group.getDeclaredFields()) {
                assertThat(Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
                        .as(group.getSimpleName() + "." + field.getName()).isTrue();
                String value = Arrays.stream(new Field[] {field}).map(f -> {
                    try {
                        return (String) f.get(null);
                    } catch (IllegalAccessException e) {
                        throw new AssertionError(e);
                    }
                }).findFirst().orElseThrow();
                assertThat(value).as(group.getSimpleName() + "." + field.getName()).matches("[A-Za-z]+");
            }
        }
    }
}
