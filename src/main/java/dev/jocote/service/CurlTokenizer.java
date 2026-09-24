package dev.jocote.service;

import java.util.ArrayList;
import java.util.List;

/** Parses a literal argument list, never evaluating shell expressions. */
final class CurlTokenizer {
    private CurlTokenizer() { }

    static List<String> tokenize(String command, CurlGenerator.Shell shell) {
        if (command == null || command.isBlank()) throw new IllegalArgumentException("Pega un comando cURL.");
        if (command.length() > 1_048_576) throw new IllegalArgumentException("El comando supera el límite de 1 MiB de caracteres.");
        boolean powershell = shell == CurlGenerator.Shell.POWERSHELL;
        char escape = powershell ? '`' : '\\';
        var tokens = new ArrayList<String>();
        var token = new StringBuilder();
        char quote = 0;
        boolean started = false;
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if (c == 0) throw new IllegalArgumentException("El comando contiene un carácter NUL.");
            if (quote == '\'') {
                if (c == '\'') {
                    if (powershell && i + 1 < command.length() && command.charAt(i + 1) == '\'') {
                        token.append('\''); i++;
                    } else quote = 0;
                } else token.append(c);
                continue;
            }
            if (c == escape) {
                if (i + 1 == command.length()) throw new IllegalArgumentException("Escape incompleto al final del comando.");
                char next = command.charAt(i + 1);
                if (next == '\n' || next == '\r' && i + 2 < command.length() && command.charAt(i + 2) == '\n') {
                    i += next == '\r' ? 2 : 1;
                    continue;
                }
                if (!powershell && quote == '"' && "\\\"$`".indexOf(next) < 0) {
                    token.append(c);
                } else {
                    if (powershell && Character.isLetterOrDigit(next)) {
                        throw new IllegalArgumentException("Escape PowerShell no soportado; usa texto literal entre comillas simples.");
                    }
                    token.append(next); i++;
                }
                started = true;
                continue;
            }
            if (c == '$' || !powershell && c == '`') {
                throw new IllegalArgumentException("No se expanden variables ni expresiones del shell. Usa valores literales entre comillas simples.");
            }
            if (quote == '"') {
                if (powershell && c == '"' && i + 1 < command.length() && command.charAt(i + 1) == '"') {
                    token.append('"'); i++; continue;
                }
                if (c == '"') quote = 0; else token.append(c);
                continue;
            }
            if (c == '\'' || c == '"') { quote = c; started = true; continue; }
            if (Character.isWhitespace(c)) {
                if (started) { tokens.add(token.toString()); token.setLength(0); started = false; }
                continue;
            }
            if ("|;&<>()".indexOf(c) >= 0 || c == '#' && !started
                    || powershell && c == '@' && !started || "{}".indexOf(c) >= 0
                    || !powershell && "*?[]~".indexOf(c) >= 0) {
                throw new IllegalArgumentException("Solo se admite un comando cURL literal, sin operadores ni scripts. Encierra URL y datos entre comillas.");
            }
            token.append(c); started = true;
        }
        if (quote != 0) throw new IllegalArgumentException("Hay comillas sin cerrar. Revisa el shell seleccionado.");
        if (started) tokens.add(token.toString());
        return List.copyOf(tokens);
    }
}
