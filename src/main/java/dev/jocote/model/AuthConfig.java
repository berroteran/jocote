package dev.jocote.model;

public record AuthConfig(Type type, String username, String secret, String key, Placement placement) {
    public enum Type { NONE, BEARER, BASIC, API_KEY }
    public enum Placement { HEADER, QUERY }

    public AuthConfig {
        type = type == null ? Type.NONE : type;
        username = username == null ? "" : username;
        secret = secret == null ? "" : secret;
        key = key == null ? "" : key;
        placement = placement == null ? Placement.HEADER : placement;
    }

    public static AuthConfig none() {
        return new AuthConfig(Type.NONE, "", "", "", Placement.HEADER);
    }
}
