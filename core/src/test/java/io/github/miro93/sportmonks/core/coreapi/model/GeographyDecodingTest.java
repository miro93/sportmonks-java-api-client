package io.github.miro93.sportmonks.core.coreapi.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeographyDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesCountryWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 320,
                    "continent_id": 1,
                    "name": "Scotland",
                    "official_name": "Scotland",
                    "fifa_name": "SCO",
                    "iso2": "GB",
                    "iso3": "GBR",
                    "latitude": "56.49067",
                    "longitude": "-4.20264",
                    "geonameid": 2638360,
                    "borders": "ENG",
                    "image_path": "https://cdn.sportmonks.com/images/countries/320.png"
                  }
                }
                """;

        Country country = codec.decode(json, codec.type(Country.class)).data();

        assertThat(country.id()).isEqualTo(320L);
        assertThat(country.continentId()).isEqualTo(1L);
        assertThat(country.name()).isEqualTo("Scotland");
        assertThat(country.officialName()).isEqualTo("Scotland");
        assertThat(country.fifaName()).isEqualTo("SCO");
        assertThat(country.iso2()).isEqualTo("GB");
        assertThat(country.iso3()).isEqualTo("GBR");
        assertThat(country.latitude()).isEqualTo("56.49067");
        assertThat(country.longitude()).isEqualTo("-4.20264");
        assertThat(country.geonameid()).isEqualTo(2638360L);
        assertThat(country.borders()).isEqualTo("ENG");
        assertThat(country.imagePath()).contains("320.png");
    }

    @Test
    void decodesCountryWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 320, "name": "Scotland" } }
                """;

        Country country = codec.decode(json, codec.type(Country.class)).data();

        assertThat(country.id()).isEqualTo(320L);
        assertThat(country.name()).isEqualTo("Scotland");
        assertThat(country.continentId()).isNull();
        assertThat(country.officialName()).isNull();
        assertThat(country.fifaName()).isNull();
        assertThat(country.iso2()).isNull();
        assertThat(country.iso3()).isNull();
        assertThat(country.latitude()).isNull();
        assertThat(country.longitude()).isNull();
        assertThat(country.geonameid()).isNull();
        assertThat(country.borders()).isNull();
        assertThat(country.imagePath()).isNull();
    }

    @Test
    void decodesContinent() {
        String json = """
                { "data": { "id": 1, "name": "Europe", "code": "EU" } }
                """;

        Continent continent = codec.decode(json, codec.type(Continent.class)).data();

        assertThat(continent.id()).isEqualTo(1L);
        assertThat(continent.name()).isEqualTo("Europe");
        assertThat(continent.code()).isEqualTo("EU");
    }

    @Test
    void decodesRegion() {
        String json = """
                { "data": { "id": 10, "country_id": 320, "name": "Glasgow" } }
                """;

        Region region = codec.decode(json, codec.type(Region.class)).data();

        assertThat(region.id()).isEqualTo(10L);
        assertThat(region.countryId()).isEqualTo(320L);
        assertThat(region.name()).isEqualTo("Glasgow");
    }

    @Test
    void decodesCityWithRegionAndCoordinates() {
        String json = """
                {
                  "data": {
                    "id": 100,
                    "country_id": 320,
                    "region": 10,
                    "name": "Glasgow",
                    "latitude": "55.86515",
                    "longitude": "-4.25763",
                    "geonameid": 2648579
                  }
                }
                """;

        City city = codec.decode(json, codec.type(City.class)).data();

        assertThat(city.id()).isEqualTo(100L);
        assertThat(city.countryId()).isEqualTo(320L);
        assertThat(city.region()).isEqualTo(10L);
        assertThat(city.name()).isEqualTo("Glasgow");
        assertThat(city.latitude()).isEqualTo("55.86515");
        assertThat(city.longitude()).isEqualTo("-4.25763");
        assertThat(city.geonameid()).isEqualTo(2648579L);
    }

    @Test
    void decodesCityWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 100, "name": "Glasgow" } }
                """;

        City city = codec.decode(json, codec.type(City.class)).data();

        assertThat(city.id()).isEqualTo(100L);
        assertThat(city.name()).isEqualTo("Glasgow");
        assertThat(city.countryId()).isNull();
        assertThat(city.region()).isNull();
        assertThat(city.latitude()).isNull();
        assertThat(city.longitude()).isNull();
        assertThat(city.geonameid()).isNull();
    }
}
