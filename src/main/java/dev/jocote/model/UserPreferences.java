package dev.jocote.model;

import java.util.Locale;

public record UserPreferences(int version, AppTheme theme, String displayName, String alias, String email,
                              LayoutDensity density) {
    public UserPreferences {
        if (version != 1) throw new IllegalArgumentException("Versión de preferencias no compatible.");
        theme = theme == null ? AppTheme.LIGHT : theme;
        density = density == null ? LayoutDensity.NORMAL : density;
        displayName = clean(displayName, 80, "El nombre");
        alias = clean(alias, 40, "El alias");
        email = clean(email, 254, "El correo");
        if (displayName.isEmpty()) displayName = "Usuario local";
        if (!email.isEmpty() && !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Escribe un correo válido o deja el campo vacío.");
        }
    }

    public UserPreferences(int version, AppTheme theme, String displayName, String alias, String email) {
        this(version, theme, displayName, alias, email, LayoutDensity.NORMAL);
    }

    public static UserPreferences defaults() { return new UserPreferences(1, AppTheme.LIGHT, "Usuario local", "", ""); }

    public UserPreferences withTheme(AppTheme value) { return new UserPreferences(version, value, displayName, alias, email, density); }

    public UserPreferences withDensity(LayoutDensity value) { return new UserPreferences(version, theme, displayName, alias, email, value); }

    public UserPreferences withProfile(String name, String nickname, String address) {
        return new UserPreferences(version, theme, name, nickname, address, density);
    }

    public String initials() {
        var words = displayName.split("\\s+");
        String first = new String(Character.toChars(words[0].codePointAt(0)));
        String last = words.length > 1 ? new String(Character.toChars(words[words.length - 1].codePointAt(0))) : "";
        return (first + last).toUpperCase(Locale.ROOT);
    }

    private static String clean(String text, int limit, String field) {
        String value = text == null ? "" : text.strip();
        if (value.length() > limit || value.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field + " admite hasta " + limit + " caracteres, sin caracteres de control.");
        }
        return value;
    }
}
