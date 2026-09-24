package dev.jocote.service;

import com.sun.net.httpserver.HttpServer;
import dev.jocote.model.ResponseData;
import dev.jocote.model.ResponsePreview.Format;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ResponseInspectorTest {
    private final ResponseInspector inspector = new ResponseInspector();
    @TempDir Path directory;

    @Test void presentsNestedJsonWithoutChangingOriginalBytes() {
        String source = "{\"items\":[{\"name\":\"Jocote ñ\",\"n\":9007199254740993}],\"ok\":true,\"empty\":null}";
        var response = text("application/problem+json", source);
        var preview = inspector.inspect(response);
        assertEquals(Format.JSON, preview.format());
        assertTrue(preview.formattedText().contains("\n"));
        assertTrue(preview.formattedText().contains("9007199254740993"));
        assertEquals("items", preview.tree().children().getFirst().name());
        assertEquals("[0]", preview.tree().children().getFirst().children().getFirst().name());
        assertEquals("boolean", preview.tree().children().get(1).type());
        assertEquals(source, response.text());
        assertArrayEquals(source.getBytes(StandardCharsets.UTF_8), response.body());
    }

    @ParameterizedTest @ValueSource(strings = {"", "{broken", "{\"a\":1}{\"b\":2}", "{\"a\":1,\"a\":2}"})
    void preservesInvalidOrAmbiguousJsonAsText(String value) {
        var result = inspector.inspect(text("application/json", value));
        assertEquals(Format.TEXT, result.format());
        assertEquals(value, result.formattedText());
        assertFalse(result.notice().isBlank());
    }

    @Test void jsonPathSupportsLiteralSelectorsWithoutDroppingNulls() {
        String source = "{\"items\":[{\"name\":\"uno\"},{\"name\":\"dos\"}],\"a.b\":null,\"quote'key\":42}";
        assertEquals(2, inspector.query(Format.JSON, source, "$.items[*].name").matches());
        assertTrue(inspector.query(Format.JSON, source, "$.items[-1].name").text().contains("dos"));
        assertEquals(2, inspector.query(Format.JSON, source, "$..name").matches());
        assertEquals(1, inspector.query(Format.JSON, source, "$['a.b']").matches());
        assertTrue(inspector.query(Format.JSON, source, "$['a.b']").text().contains("null"));
        assertEquals(1, inspector.query(Format.JSON, source, "$[\"quote'key\"]").matches());
        assertEquals(0, inspector.query(Format.JSON, source, "$.missing").matches());
        assertEquals(1, inspector.query(Format.JSON, source, "$").matches());
        assertEquals(5, inspector.query(Format.JSON, "{\"a\":[1,2],\"b\":{\"c\":3}}", "$..*").matches());
    }

    @ParameterizedTest @ValueSource(strings = {"name", "$.", "$.items[?(@.n>2)]", "$[0:2]", "$['a','b']", "$.a()", "$[999999999999]", "$['a'", "$.[0]"})
    void rejectsUnsupportedJsonPathBeforeReturningPartialResults(String expression) {
        assertThrows(IllegalArgumentException.class, () -> inspector.query(Format.JSON, "{\"items\":[]}", expression));
    }

    @Test void capsComplexityAndQueryOutput() {
        assertEquals(Format.TEXT, inspector.inspect(text("application/json", "[".repeat(130) + "0" + "]".repeat(130))).format());
        assertEquals(Format.TEXT, inspector.inspect(text("application/json", "[0," + "0,".repeat(10_000) + "0]")).format());
        assertEquals(Format.TEXT, inspector.inspect(text("application/json", " ".repeat(ResponseInspector.TEXT_LIMIT + 1))).format());
        String array = "[" + "0,".repeat(1_001) + "0]";
        assertThrows(IllegalArgumentException.class, () -> inspector.query(Format.JSON, array, "$[*]"));
        assertThrows(IllegalArgumentException.class, () -> inspector.query(Format.JSON, "{}", "$" + " ".repeat(1_025)));
        assertThrows(IllegalArgumentException.class, () -> inspector.query(Format.JSON, " ".repeat(ResponseInspector.TEXT_LIMIT + 1), "$"));
        String large = "{\"nested\":{\"value\":\"" + "a".repeat(160_000) + "\"}}";
        assertThrows(IllegalArgumentException.class, () -> inspector.query(Format.JSON, large, "$..*"));
    }

    @Test void formatsXmlAndEvaluatesNodesAttributesAndScalars() {
        String source = "<root><item id=\"1\">uno</item><item id=\"2\">dos</item></root>";
        var response = text("application/xml", source);
        var preview = inspector.inspect(response);
        assertEquals(Format.XML, preview.format());
        assertTrue(preview.formattedText().contains("\n"));
        assertEquals(source, response.text());
        assertEquals(2, inspector.query(Format.XML, source, "//item").matches());
        assertTrue(inspector.query(Format.XML, source, "//item/@id").text().contains("2"));
        assertEquals("2.0", inspector.query(Format.XML, source, "count(//item)").text());
        assertEquals("uno", inspector.query(Format.XML, source, "string(//item[1])").text());
        assertEquals("false", inspector.query(Format.XML, source, "boolean(//missing)").text());
        assertEquals(0, inspector.query(Format.XML, source, "//missing").matches());
        assertThrows(IllegalArgumentException.class, () -> inspector.query(Format.XML, source, "//*["));
    }

    @Test void xpathUnderstandsRootPrefixesAndDefaultNamespacesExplicitly() {
        String xml = "<root xmlns=\"urn:default\" xmlns:p=\"urn:items\"><p:item>uno</p:item><item>dos</item></root>";
        assertEquals(1, inspector.query(Format.XML, xml, "//p:item").matches());
        assertEquals(2, inspector.query(Format.XML, xml, "//*[local-name()='item']").matches());
        assertEquals(0, inspector.query(Format.XML, xml, "//item").matches());
    }

    @Test void neverReadsExternalXmlOrHtmlResources() throws Exception {
        var requests = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> { requests.incrementAndGet(); exchange.sendResponseHeaders(204, -1); exchange.close(); });
        server.start();
        try {
            String address = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            Path secret = directory.resolve("secret.txt"); Files.writeString(secret, "LOCAL_TEST_SECRET");
            for (String resource : List.of(address, secret.toUri().toString())) {
                String xml = "<!DOCTYPE root [<!ENTITY x SYSTEM \"" + resource + "\">]><root>&x;</root>";
                var preview = inspector.inspect(text("application/xml", xml));
                assertEquals(Format.TEXT, preview.format());
                assertFalse(preview.formattedText().contains("LOCAL_TEST_SECRET"));
                assertThrows(IllegalArgumentException.class, () -> inspector.query(Format.XML, xml, "/root"));
            }
            String xml = "<root xmlns:xi=\"http://www.w3.org/2001/XInclude\"><xi:include href=\"" + address + "\"/></root>";
            assertEquals(Format.XML, inspector.inspect(text("application/xml", xml)).format());
            String html = "<html><head><style>@import url('" + address + "');</style></head><body>"
                    + "<h1>Title</h1><p>Hello <b>world</b> &amp; ñ</p>"
                    + "<script src='" + address + "'>fetch('" + address + "')</script>"
                    + "<img src='" + address + "' onerror='alert(1)'><iframe src='" + address + "'></iframe>"
                    + "<a href='" + address + "'>link</a><form action='" + address + "'><input></form></body></html>";
            var preview = inspector.inspect(text("text/html", html));
            assertEquals(Format.HTML, preview.format());
            String rendered = preview.html().stream().map(span -> span.text()).reduce("", String::concat);
            assertTrue(rendered.contains("Hello")); assertTrue(rendered.contains("& ñ"));
            assertFalse(rendered.contains("fetch")); assertFalse(rendered.contains("@import"));
            assertFalse(rendered.contains(address));
            assertTrue(preview.html().stream().anyMatch(span -> span.bold() && span.text().equals("world")));
            assertEquals(0, requests.get());
        } finally { server.stop(0); }
    }

    @Test void rejectsDeepXmlAndEntityExpansion() {
        assertEquals(Format.TEXT, inspector.inspect(text("application/xml", "<r>".repeat(130) + "</r>".repeat(130))).format());
        String entity = "<!DOCTYPE r [<!ENTITY a 'hello'>]><r>&a;</r>";
        assertEquals(Format.TEXT, inspector.inspect(text("text/xml", entity)).format());
        assertEquals(Format.TEXT, inspector.inspect(text("application/xml", "<root>")).format());
    }

    @Test void probesRasterImagesAndRejectsInvalidOrOversizedDimensions() throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(16, 8, BufferedImage.TYPE_INT_RGB), "png", bytes);
        var preview = inspector.inspect(binary("image/png", bytes.toByteArray()));
        assertEquals(Format.IMAGE, preview.format());
        assertEquals(16, preview.image().width()); assertEquals(8, preview.image().height());
        assertEquals(Format.TEXT, inspector.inspect(binary("image/png", new byte[]{1, 2, 3})).format());
        // Image metadata is read before decoding pixels; patch the PNG width to exceed the limit.
        byte[] oversized = bytes.toByteArray();
        java.nio.ByteBuffer.wrap(oversized).putInt(16, 20_000);
        assertEquals(Format.TEXT, inspector.inspect(binary("image/png", oversized)).format());
        assertEquals(Format.XML, inspector.inspect(text("image/svg+xml", "<svg xmlns=\"http://www.w3.org/2000/svg\"/>")).format());
    }

    @Test void keepsPlainAndEmptyResponsesReadable() {
        assertEquals("hello", inspector.inspect(text("text/plain", "hello")).formattedText());
        assertEquals(Format.TEXT, inspector.inspect(text("", "")).format());
        assertEquals(Format.JSON, inspector.inspect(text("text/plain", "{\"ok\":true}")).format());
        assertThrows(IllegalArgumentException.class, () -> inspector.query(Format.HTML, "<p>x</p>", "$"));
    }

    private static ResponseData text(String type, String text) { return binary(type, text.getBytes(StandardCharsets.UTF_8)); }
    private static ResponseData binary(String type, byte[] bytes) {
        return new ResponseData(200, Map.of("Content-Type", List.of(type)), bytes, 1, URI.create("https://example.test/"), "HTTP_1_1");
    }
}
