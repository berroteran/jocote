package dev.jocote.model;

public enum AppTheme {
    LIGHT, DARK, MODENA, CASPIAN;

    public AppTheme next() { return values()[(ordinal() + 1) % values().length]; }
}
