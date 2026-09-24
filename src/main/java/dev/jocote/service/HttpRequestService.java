package dev.jocote.service;

import dev.jocote.model.PreparedRequest;
import dev.jocote.model.ResponseData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class HttpRequestService implements AutoCloseable {
    public static final int MAX_RESPONSE_BYTES = 10 * 1024 * 1024;
    private static final Logger LOG = Logger.getLogger(HttpRequestService.class.getName());
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NEVER).build();

    public CompletableFuture<ResponseData> send(PreparedRequest request) {
        var builder = HttpRequest.newBuilder(request.uri()).timeout(Duration.ofSeconds(request.timeoutSeconds()));
        request.headers().forEach(h -> builder.header(h.key(), h.value()));
        builder.method(request.method(), request.hasBody()
                ? HttpRequest.BodyPublishers.ofString(request.body(), StandardCharsets.UTF_8)
                : HttpRequest.BodyPublishers.noBody());
        long started = System.nanoTime();
        var exchange = client.sendAsync(builder.build(), info -> new LimitedBodySubscriber(MAX_RESPONSE_BYTES));
        var result = new CompletableFuture<ResponseData>();
        exchange.whenComplete((response, error) -> {
            if (error != null) result.completeExceptionally(error);
            else {
                long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
                LOG.fine(() -> "HTTP " + request.method() + " -> " + response.statusCode() + " in " + elapsed + " ms");
                result.complete(new ResponseData(response.statusCode(), response.headers().map(), response.body(),
                        elapsed, response.uri(), response.version().toString()));
            }
        });
        result.orTimeout(request.timeoutSeconds(), TimeUnit.SECONDS);
        result.whenComplete((response, error) -> { if (error != null) exchange.cancel(true); });
        return result;
    }

    @Override public void close() { client.shutdownNow(); }

    private static final class LimitedBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
        private final int limit;
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private final CompletableFuture<byte[]> result = new CompletableFuture<>();
        private Flow.Subscription subscription;

        private LimitedBodySubscriber(int limit) { this.limit = limit; }
        @Override public CompletionStage<byte[]> getBody() { return result; }
        @Override public void onSubscribe(Flow.Subscription value) { subscription = value; value.request(1); }
        @Override public void onNext(List<ByteBuffer> buffers) {
            for (ByteBuffer buffer : buffers) {
                if ((long) output.size() + buffer.remaining() > limit) {
                    subscription.cancel();
                    result.completeExceptionally(new IOException("La respuesta supera el límite de 10 MiB."));
                    return;
                }
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                output.writeBytes(bytes);
            }
            subscription.request(1);
        }
        @Override public void onError(Throwable error) { result.completeExceptionally(error); }
        @Override public void onComplete() { result.complete(output.toByteArray()); }
    }
}
