package dev.aws.lite.sqs;

import dev.aws.lite.core.credentials.CredentialsProvider;
import dev.aws.lite.core.http.AwsHttpClient;
import dev.aws.lite.core.http.AwsHttpRequest;
import dev.aws.lite.core.sigv4.SigV4Signer;

import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;


public class SqsClient {
    private final String region;
    private final CredentialsProvider credentials;
    private final AwsHttpClient http;
    private final SigV4Signer signer;

    public SqsClient(String region, CredentialsProvider credentials, AwsHttpClient http, SigV4Signer signer){
        this.region = region; this.credentials = credentials; this.http = http; this.signer = signer;
    }

    public void sendMessage(String queueUrl, String messageBody) throws Exception {
        String body = "Action=SendMessage&MessageBody=" + urlEncode(messageBody) + "&Version=2012-11-05";
        executePost(queueUrl, body);
    }

    public String receiveMessage(String queueUrl, int max) throws Exception {
        String body = "Action=ReceiveMessage&MaxNumberOfMessages=" + max + "&Version=2012-11-05";
        return executePost(queueUrl, body);
    }

    public void deleteMessage(String queueUrl, String receiptHandle) throws Exception {
        String body = "Action=DeleteMessage&ReceiptHandle=" + urlEncode(receiptHandle) + "&Version=2012-11-05";
        executePost(queueUrl, body);
    }

    private String executePost(String queueUrl, String formBody) throws Exception {
        URI uri = new URI(queueUrl);
        byte[] payload = formBody.getBytes(StandardCharsets.UTF_8);
        Map<String,String> headers = new LinkedHashMap<>();
        headers.put("content-type", "application/x-www-form-urlencoded; charset=utf-8");
        headers.put("x-amz-content-sha256", sha256Hex(payload));

        var signed = signer.sign(new AwsHttpRequest(AwsHttpRequest.Method.POST, uri, headers, payload), "dev/aws/lite/sqs", region, credentials.resolveCredentials());
        HttpResponse<byte[]> resp = http.execute(new AwsHttpRequest(AwsHttpRequest.Method.POST, uri, signed, payload));
        int sc = resp.statusCode();
        if(sc>=200 && sc<300) return new String(resp.body(), StandardCharsets.UTF_8);
        throw new RuntimeException("SQS call failed: "+sc+" - "+ new String(resp.body()));
    }

    private static String sha256Hex(byte[] b) throws Exception {
        var md = java.security.MessageDigest.getInstance("SHA-256");
        md.update(b);
        var out = md.digest();
        var sb = new StringBuilder();
        for(byte x: out) sb.append(String.format("%02x", x));
        return sb.toString();
    }
    private static String urlEncode(String s){ return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8); }
}