package io.github.miro93.sportmonks.football.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/// Null-safe parsers for the two date formats SportMonks uses: {@code yyyy-MM-dd} and
/// {@code yyyy-MM-dd HH:mm:ss} (in the requested {@code timezone}, UTC by default).
final class ApiDates {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ApiDates() {
    }

    /// @return the parsed date, or {@code null} when {@code value} is {@code null}
    /// @throws java.time.format.DateTimeParseException if the value is not {@code yyyy-MM-dd}
    static LocalDate date(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    /// Parses {@code yyyy-MM-dd HH:mm:ss}; a date-only value is taken at start of day.
    ///
    /// @return the parsed date-time, or {@code null} when {@code value} is {@code null}
    /// @throws java.time.format.DateTimeParseException if the value matches neither format
    static LocalDateTime dateTime(String value) {
        if (value == null) {
            return null;
        }
        return value.length() == 10 ? LocalDate.parse(value).atStartOfDay() : LocalDateTime.parse(value, DATE_TIME);
    }
}
