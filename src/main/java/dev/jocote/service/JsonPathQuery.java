package dev.jocote.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Literal JSONPath selectors: root, members, indexes, wildcards and descendants.
 * Unsupported syntax is rejected before evaluation; no scripting or filter runtime.
 */
final class JsonPathQuery {
    private static final int MAX_RESULTS = 1_000;
    private static final int MAX_VISITS = 200_000;
    private record Step(boolean descendant, String member, Integer index, boolean wildcard) { }
    private final List<Step> steps = new ArrayList<>();
    private int visits;

    private JsonPathQuery(String expression) {
        if (expression.isEmpty() || expression.charAt(0) != '$') throw syntax();
        int position = 1;
        while (position < expression.length()) {
            if (steps.size() >= 64) throw new IllegalArgumentException("Máximo 64 selectores JSONPath.");
            boolean descendant = false;
            char c = expression.charAt(position);
            if (c == '.') {
                position++;
                if (position < expression.length() && expression.charAt(position) == '.') { descendant = true; position++; }
                if (position >= expression.length()) throw syntax();
                if (expression.charAt(position) == '*') {
                    steps.add(new Step(descendant, null, null, true)); position++; continue;
                }
                if (expression.charAt(position) != '[') {
                    int start = position;
                    if (!isNameStart(expression.charAt(position))) throw syntax();
                    while (position < expression.length() && isNamePart(expression.charAt(position))) position++;
                    steps.add(new Step(descendant, expression.substring(start, position), null, false)); continue;
                }
                if (!descendant) throw syntax();
            } else if (c != '[') throw syntax();
            if (position >= expression.length() || expression.charAt(position++) != '[') throw syntax();
            while (position < expression.length() && Character.isWhitespace(expression.charAt(position))) position++;
            if (position >= expression.length()) throw syntax();
            c = expression.charAt(position++);
            String member = null;
            Integer index = null;
            boolean wildcard = c == '*';
            if (c == '\'' || c == '"') {
                char quote = c;
                var key = new StringBuilder();
                boolean closed = false;
                while (position < expression.length()) {
                    c = expression.charAt(position++);
                    if (c == quote) { closed = true; break; }
                    if (c < 32) throw syntax();
                    if (c == '\\') {
                        if (position >= expression.length()) throw syntax();
                        c = expression.charAt(position++);
                        if (c == 'u') {
                            if (position + 4 > expression.length()) throw syntax();
                            try { key.append((char) Integer.parseInt(expression.substring(position, position + 4), 16)); }
                            catch (NumberFormatException e) { throw syntax(); }
                            position += 4; continue;
                        }
                        c = switch (c) {
                            case 'n' -> '\n'; case 'r' -> '\r'; case 't' -> '\t';
                            case 'b' -> '\b'; case 'f' -> '\f';
                            case '\\', '/', '"', '\'' -> c;
                            default -> throw syntax();
                        };
                    }
                    key.append(c);
                }
                if (!closed) throw syntax();
                member = key.toString();
            } else if (!wildcard) {
                int start = position - 1;
                while (position < expression.length() && Character.isDigit(expression.charAt(position))) position++;
                String number = expression.substring(start, position);
                if (!number.matches("-?(0|[1-9][0-9]*)")) throw syntax();
                try { index = Integer.valueOf(number); }
                catch (NumberFormatException e) { throw new IllegalArgumentException("Índice JSONPath fuera de rango."); }
            }
            while (position < expression.length() && Character.isWhitespace(expression.charAt(position))) position++;
            if (position >= expression.length() || expression.charAt(position++) != ']') throw syntax();
            steps.add(new Step(descendant, member, index, wildcard));
        }
    }

    static List<JsonNode> evaluate(JsonNode root, String expression) {
        var query = new JsonPathQuery(expression);
        List<JsonNode> current = List.of(root);
        for (Step step : query.steps) {
            var next = new ArrayList<JsonNode>();
            for (JsonNode node : current) query.select(node, step, next);
            current = next;
        }
        return current;
    }

    private void select(JsonNode node, Step step, List<JsonNode> results) {
        if (++visits > MAX_VISITS || Thread.currentThread().isInterrupted()) {
            throw new IllegalArgumentException("La consulta supera el límite de trabajo o fue cancelada.");
        }
        if (step.wildcard()) {
            if (node.isContainerNode()) node.forEach(child -> add(results, child));
        } else if (step.member() != null) {
            if (node.isObject() && node.has(step.member())) add(results, node.get(step.member()));
        } else if (node.isArray()) {
            long index = step.index() < 0 ? (long) node.size() + step.index() : step.index();
            if (index >= 0 && index < node.size()) add(results, node.get((int) index));
        }
        if (step.descendant() && node.isContainerNode()) {
            for (JsonNode child : node) select(child, step, results);
        }
    }

    private static void add(List<JsonNode> results, JsonNode value) {
        if (results.size() >= MAX_RESULTS) throw new IllegalArgumentException("La consulta supera 1 000 resultados; usa una ruta más específica.");
        results.add(value);
    }

    private static boolean isNameStart(char c) { return c == '_' || Character.isLetter(c) || c >= 128; }
    private static boolean isNamePart(char c) { return isNameStart(c) || Character.isDigit(c); }
    private static IllegalArgumentException syntax() {
        return new IllegalArgumentException("JSONPath no soportado. Usa $, .nombre, ['clave'], [índice], [*] o ..nombre. Sin filtros, uniones, slices ni funciones.");
    }
}
