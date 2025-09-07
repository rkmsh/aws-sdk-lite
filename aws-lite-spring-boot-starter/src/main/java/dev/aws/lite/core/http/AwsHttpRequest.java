package dev.aws.lite.core.http;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AwsHttpRequest {

    public enum Method { GET, POST, PUT, DELETE }
    private final Method method;
    private final URI uri;
    private final Map<String, String> headers;
    private final byte[] body;

    public AwsHttpRequest(Method method, URI uri, Map<String, String> headers, byte[] body) {
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.body = body;
    }

    public Method method() {return method;}
    public URI uri() {return uri;}
    public Map<String, String> headers() {return headers;}
    public byte[] body() {return body;}

    public static Builder builder() {return new Builder();}
    public static final class Builder {
        private Method method;
        public URI uri;
        public Map<String, String> headers = new LinkedHashMap<>();
        public byte[] body = new byte[0];

        public Builder method(Method m) {
            this.method = m;
            return this;
        }
        public Builder uri(URI u) {
            this.uri = u;
            return this;
        }
        public Builder header(String k, String v) {
            this.headers.put(k, v);
            return this;
        }
        public Builder body(byte[] b) {
            this.body = b;
            return this;
        }
        public AwsHttpRequest build() {
            return new AwsHttpRequest(method, uri, headers, body);
        }

    }
}
