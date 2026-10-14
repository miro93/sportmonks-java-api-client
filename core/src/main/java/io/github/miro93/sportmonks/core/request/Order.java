package io.github.miro93.sportmonks.core.request;

/// Direction of the {@code order} query option (ordering by id, or by the
/// {@code sortBy} field when one is set).
public enum Order {
    ASC("asc"),
    DESC("desc");

    private final String value;

    Order(String value) {
        this.value = value;
    }

    /// The query-string value ({@code asc} / {@code desc}).
    public String value() {
        return value;
    }
}
