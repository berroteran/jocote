package dev.jocote.model;

public record KeyValue(boolean enabled, String key, String value) {
    public KeyValue {
        key = key == null ? "" : key;
        value = value == null ? "" : value;
    }
}
