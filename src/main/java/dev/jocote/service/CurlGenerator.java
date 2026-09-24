package dev.jocote.service;

import dev.jocote.model.PreparedRequest;

import java.util.ArrayList;
import java.util.List;

public final class CurlGenerator {
    public enum Shell { BASH, POWERSHELL }

    public String generate(PreparedRequest request, Shell shell) {
        List<String> lines = new ArrayList<>();
        String command = shell == Shell.POWERSHELL ? "curl.exe" : "curl";
        lines.add(command + " --request " + request.method());
        lines.add("--url " + quote(request.uri().toASCIIString(), shell));
        lines.add("--globoff");
        lines.add("--max-time " + request.timeoutSeconds());
        if (request.method().equals("HEAD")) lines.add("--head");
        request.headers().forEach(h -> lines.add("--header " + quote(h.value().isEmpty() ? h.key() + ";" : h.key() + ": " + h.value(), shell)));
        if (request.hasBody()) lines.add("--data-raw " + quote(request.body(), shell));
        return String.join(shell == Shell.POWERSHELL ? " `\n  " : " \\\n  ", lines);
    }

    private static String quote(String text, Shell shell) {
        return "'" + text.replace("'", shell == Shell.POWERSHELL ? "''" : "'\"'\"'") + "'";
    }
}
