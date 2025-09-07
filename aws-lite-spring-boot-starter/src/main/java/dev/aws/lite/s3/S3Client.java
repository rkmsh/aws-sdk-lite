package dev.aws.lite.s3;

import dev.aws.lite.core.credentials.CredentialsProvider;
import dev.aws.lite.core.http.AwsHttpClient;
import dev.aws.lite.core.http.AwsHttpRequest;
import dev.aws.lite.core.sigv4.SigV4Signer;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

public class S3Client {
    private final String region;
    private final CredentialsProvider credentials;
    private final AwsHttpClient http;
    private final SigV4Signer signer;

    public S3Client(String region, CredentialsProvider credentials, AwsHttpClient http, SigV4Signer signer){
        this.region = region; this.credentials = credentials; this.http = http; this.signer = signer;
    }

    public void putObject(String bucket, String key, byte[] data, String contentType) throws Exception {
        URI uri = new URI("https://"+bucket+".s3."+region+".amazonaws.com/"+key);
        Map<String,String> headers = new LinkedHashMap<>();
        headers.put("content-type", contentType);
        headers.put("x-amz-content-sha256", sha256Hex(data));

        AwsHttpRequest req = AwsHttpRequest.builder()
                .method(AwsHttpRequest.Method.PUT)
                .uri(uri)
                .body(data)
                .build();

        var signed = signer.sign(new AwsHttpRequest(req.method(), req.uri(), headers, data), "dev/aws/lite/s3", region, credentials.resolveCredentials());
        AwsHttpRequest signedReq = AwsHttpRequest.builder()
                .method(req.method())
                .uri(req.uri())
                .body(req.body())
                .build();
        signed.forEach((k,v)-> headers.put(k,v));

        HttpResponse<byte[]> resp = http.execute(new AwsHttpRequest(req.method(), req.uri(), headers, data));
        int sc = resp.statusCode();
        if(sc>=200 && sc<300) return; else throw new RuntimeException("S3 putObject failed: "+sc+" - "+ new String(resp.body()));
    }

    public byte[] getObject(String bucket, String key) throws Exception {
        URI uri = new URI("https://"+bucket+".s3."+region+".amazonaws.com/"+key);
        Map<String,String> headers = new LinkedHashMap<>();
        headers.put("x-amz-content-sha256", sha256Hex(new byte[0]));

        AwsHttpRequest req = AwsHttpRequest.builder().method(AwsHttpRequest.Method.GET).uri(uri).build();
        var signed = signer.sign(new AwsHttpRequest(req.method(), req.uri(), headers, new byte[0]), "dev/aws/lite/s3", region, credentials.resolveCredentials());
        signed.putAll(headers);

        HttpResponse<byte[]> resp = http.execute(new AwsHttpRequest(req.method(), req.uri(), signed, new byte[0]));
        int sc = resp.statusCode();
        if(sc==200) return resp.body();
        throw new RuntimeException("S3 getObject failed: "+sc+" - "+ new String(resp.body()));
    }

    private static String sha256Hex(byte[] b) throws Exception {
        var md = java.security.MessageDigest.getInstance("SHA-256");
        md.update(b);
        var out = md.digest();
        var sb = new StringBuilder();
        for(byte x: out) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}
