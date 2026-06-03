package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VenueRefereeDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesVenueWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 8909,
                    "country_id": 1161,
                    "city_id": "51663",
                    "name": "Celtic Park",
                    "address": "Kerrydale Street",
                    "zipcode": "G40 3RE",
                    "latitude": "55.849689",
                    "longitude": "-4.205518",
                    "capacity": 60411,
                    "image_path": "https://cdn.sportmonks.com/images/soccer/venues/8909.png",
                    "city_name": "Glasgow",
                    "surface": "grass",
                    "national_team": false
                  }
                }
                """;

        Venue venue = codec.decode(json, codec.type(Venue.class)).data();

        assertThat(venue.id()).isEqualTo(8909L);
        assertThat(venue.countryId()).isEqualTo(1161L);
        assertThat(venue.cityId()).isEqualTo("51663");
        assertThat(venue.name()).isEqualTo("Celtic Park");
        assertThat(venue.address()).isEqualTo("Kerrydale Street");
        assertThat(venue.zipcode()).isEqualTo("G40 3RE");
        assertThat(venue.latitude()).isEqualTo("55.849689");
        assertThat(venue.longitude()).isEqualTo("-4.205518");
        assertThat(venue.capacity()).isEqualTo(60411);
        assertThat(venue.imagePath()).contains("8909.png");
        assertThat(venue.cityName()).isEqualTo("Glasgow");
        assertThat(venue.surface()).isEqualTo("grass");
        assertThat(venue.nationalTeam()).isFalse();
    }

    @Test
    void decodesVenueWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 8909, "name": "Celtic Park" } }
                """;

        Venue venue = codec.decode(json, codec.type(Venue.class)).data();

        assertThat(venue.id()).isEqualTo(8909L);
        assertThat(venue.name()).isEqualTo("Celtic Park");
        assertThat(venue.countryId()).isNull();
        assertThat(venue.cityId()).isNull();
        assertThat(venue.address()).isNull();
        assertThat(venue.zipcode()).isNull();
        assertThat(venue.latitude()).isNull();
        assertThat(venue.longitude()).isNull();
        assertThat(venue.capacity()).isNull();
        assertThat(venue.imagePath()).isNull();
        assertThat(venue.cityName()).isNull();
        assertThat(venue.surface()).isNull();
        assertThat(venue.nationalTeam()).isNull();
    }

    @Test
    void decodesRefereeWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 14,
                    "sport_id": 1,
                    "country_id": 1161,
                    "city_id": "51663",
                    "common_name": "J. Beaton",
                    "firstname": "John",
                    "lastname": "Beaton",
                    "name": "John Beaton",
                    "display_name": "John Beaton",
                    "image_path": "https://cdn.sportmonks.com/images/soccer/referees/14.png",
                    "height": 180,
                    "weight": 75,
                    "date_of_birth": "1982-09-22",
                    "gender": "male"
                  }
                }
                """;

        Referee referee = codec.decode(json, codec.type(Referee.class)).data();

        assertThat(referee.id()).isEqualTo(14L);
        assertThat(referee.sportId()).isEqualTo(1L);
        assertThat(referee.countryId()).isEqualTo(1161L);
        assertThat(referee.cityId()).isEqualTo("51663");
        assertThat(referee.commonName()).isEqualTo("J. Beaton");
        assertThat(referee.firstname()).isEqualTo("John");
        assertThat(referee.lastname()).isEqualTo("Beaton");
        assertThat(referee.name()).isEqualTo("John Beaton");
        assertThat(referee.displayName()).isEqualTo("John Beaton");
        assertThat(referee.imagePath()).contains("14.png");
        assertThat(referee.height()).isEqualTo(180);
        assertThat(referee.weight()).isEqualTo(75);
        assertThat(referee.dateOfBirth()).isEqualTo("1982-09-22");
        assertThat(referee.gender()).isEqualTo("male");
    }

    @Test
    void decodesRefereeWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 14, "name": "John Beaton" } }
                """;

        Referee referee = codec.decode(json, codec.type(Referee.class)).data();

        assertThat(referee.id()).isEqualTo(14L);
        assertThat(referee.name()).isEqualTo("John Beaton");
        assertThat(referee.sportId()).isNull();
        assertThat(referee.countryId()).isNull();
        assertThat(referee.cityId()).isNull();
        assertThat(referee.commonName()).isNull();
        assertThat(referee.firstname()).isNull();
        assertThat(referee.lastname()).isNull();
        assertThat(referee.displayName()).isNull();
        assertThat(referee.imagePath()).isNull();
        assertThat(referee.height()).isNull();
        assertThat(referee.weight()).isNull();
        assertThat(referee.dateOfBirth()).isNull();
        assertThat(referee.gender()).isNull();
    }
}
