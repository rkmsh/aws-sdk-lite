package dev.aws.lite.core.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class AwsHttpClient {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public HttpResponse<byte[]> execute(AwsHttpRequest req) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(req.uri()).timeout(
                Duration.ofSeconds(30));
        req.headers().forEach(b::header);
        switch (req.method()) {
            case GET -> b.GET();
            case DELETE ->  b.DELETE();
            case PUT -> b.PUT(HttpRequest.BodyPublishers.ofByteArray(req.body()));
            case POST -> b.POST(HttpRequest.BodyPublishers.ofByteArray(req.body()));
        }
        return client.send(b.build(), HttpResponse.BodyHandlers.ofByteArray());
    }
}
