package dev.jocote.service;

import com.sun.net.httpserver.HttpServer;
import dev.jocote.model.KeyValue;
import dev.jocote.model.PreparedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestServiceTest {
    private HttpServer server;
    private HttpRequestService service;
    private java.util.concurrent.ExecutorService executor;

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        executor = Executors.newVirtualThreadPerTaskExecutor(); server.setExecutor(executor);
        server.createContext("/echo", exchange -> {
            byte[] bytes = exchange.getRequestBody().readAllBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().add("X-Method", exchange.getRequestMethod());
            exchange.getResponseHeaders().add("X-Auth", exchange.getRequestHeaders().getFirst("Authorization"));
            exchange.sendResponseHeaders(201, bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
        });
        server.createContext("/missing", exchange -> { exchange.sendResponseHeaders(404, -1); exchange.close(); });
        server.createContext("/redirect", exchange -> { exchange.getResponseHeaders().add("Location", "/missing"); exchange.sendResponseHeaders(302, -1); exchange.close(); });
        server.createContext("/slow", exchange -> {
            try { Thread.sleep(2500); exchange.sendResponseHeaders(200, -1); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { exchange.close(); }
        });
        server.createContext("/large", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            try { for (int i = 0; i < 11; i++) exchange.getResponseBody().write(new byte[1024 * 1024]); }
            catch (java.io.IOException ignored) { /* Client rejects over-limit response and closes connection. */ }
            finally { exchange.close(); }
        });
        server.start(); service = new HttpRequestService();
    }

    @AfterEach void stop() { service.close(); server.stop(0); executor.shutdownNow(); }

    private PreparedRequest get(String path, int timeout) {
        return new PreparedRequest("GET", URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path), List.of(), "", false, timeout);
    }

    @Test void sendsActualMethodHeadersAndUnicodeBody() throws Exception {
        var request = new PreparedRequest("PATCH", get("/echo", 5).uri(), List.of(new KeyValue(true, "Authorization", "Bearer token")), "{\"saludo\":\"¡Hola!\"}", true, 5);
        var response = service.send(request).get(6, TimeUnit.SECONDS);
        assertEquals(201, response.statusCode()); assertEquals(request.body(), response.text());
        assertEquals("PATCH", response.headers().get("x-method").getFirst());
        assertEquals("Bearer token", response.headers().get("x-auth").getFirst());
        assertArrayEquals(request.body().getBytes(StandardCharsets.UTF_8), response.body());
    }

    @Test void returnsErrorsAndRedirectsAsInspectableResponses() throws Exception {
        assertEquals(404, service.send(get("/missing", 5)).get(6, TimeUnit.SECONDS).statusCode());
        assertEquals(302, service.send(get("/redirect", 5)).get(6, TimeUnit.SECONDS).statusCode());
    }

    @Test void executesImportedCurlAgainstLocalServer() throws Exception {
        var preparer = new RequestPreparer();
        var imported = new CurlImporter(preparer).parse("curl '" + get("/echo", 5).uri()
                + "' -XPATCH -H 'Authorization: Bearer example-token' --json '{\"saludo\":\"¡Hola!\"}' --max-time 5",
                CurlGenerator.Shell.BASH);
        var response = service.send(preparer.prepare(imported.request())).get(6, TimeUnit.SECONDS);
        assertEquals(201, response.statusCode());
        assertEquals("{\"saludo\":\"¡Hola!\"}", response.text());
        assertEquals("PATCH", response.headers().get("x-method").getFirst());
        assertEquals("Bearer example-token", response.headers().get("x-auth").getFirst());
    }

    @Test void requestTimesOutWithoutBlockingCaller() {
        var future = service.send(get("/slow", 1));
        assertFalse(future.isDone());
        assertThrows(ExecutionException.class, () -> future.get(3, TimeUnit.SECONDS));
    }

    @Test void cancellationCompletesFuture() {
        var future = service.send(get("/slow", 5)); assertTrue(future.cancel(true)); assertTrue(future.isCancelled());
    }

    @Test void limitsResponseMemoryUsage() {
        var exception = assertThrows(ExecutionException.class, () -> service.send(get("/large", 5)).get(6, TimeUnit.SECONDS));
        Throwable cause = exception;
        while (cause.getCause() != null) cause = cause.getCause();
        assertTrue(cause.getMessage().contains("10 MiB"));
    }
}
