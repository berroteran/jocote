package dev.jocote.model;

import java.util.List;
import java.util.UUID;

public record RequestDefinition(String id, String name, String method, String url,
                                List<KeyValue> parameters, List<KeyValue> headers,
                                AuthConfig auth, BodyType bodyType, String body,
                                List<KeyValue> formFields, int timeoutSeconds) {
    public enum BodyType { NONE, JSON, TEXT, XML, FORM }

    public RequestDefinition {
        id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        name = name == null || name.isBlank() ? "Nueva petición" : name;
        method = method == null ? "GET" : method;
        url = url == null ? "" : url;
        parameters = parameters == null ? List.of() : List.copyOf(parameters);
        headers = headers == null ? List.of() : List.copyOf(headers);
        auth = auth == null ? AuthConfig.none() : auth;
        bodyType = bodyType == null ? BodyType.NONE : bodyType;
        body = body == null ? "" : body;
        formFields = formFields == null ? List.of() : List.copyOf(formFields);
        timeoutSeconds = timeoutSeconds == 0 ? 30 : timeoutSeconds;
    }

    public static RequestDefinition blank() {
        return new RequestDefinition(null, "Nueva petición", "GET", "", List.of(), List.of(),
                AuthConfig.none(), BodyType.NONE, "", List.of(), 30);
    }

    public RequestDefinition withName(String newName) {
        return new RequestDefinition(id, newName, method, url, parameters, headers, auth, bodyType, body, formFields, timeoutSeconds);
    }

    public RequestDefinition duplicate() {
        return new RequestDefinition(null, name + " (copia)", method, url, parameters, headers, auth, bodyType, body, formFields, timeoutSeconds);
    }
}
