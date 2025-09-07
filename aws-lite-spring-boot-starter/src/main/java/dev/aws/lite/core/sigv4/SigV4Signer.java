package dev.aws.lite.core.sigv4;

import dev.aws.lite.core.credentials.AwsCredentials;
import dev.aws.lite.core.http.AwsHttpRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class SigV4Signer {
    private static final DateTimeFormatter AMZ_DATE = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Map<String,String> sign(AwsHttpRequest request, String service, String region, AwsCredentials creds){
        var now = Instant.now();
        var amzDate = AMZ_DATE.format(now.atOffset(ZoneOffset.UTC));
        var date = DATE.format(now.atOffset(ZoneOffset.UTC));

        Map<String,String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.putAll(request.headers());
        headers.put("host", request.uri().getHost());
        headers.put("x-amz-date", amzDate);
        if (creds.sessionToken() != null) headers.put("x-amz-security-token", creds.sessionToken());

        String canonicalRequest = canonicalRequest(request, headers);
        String credentialScope = date + "/" + region + "/" + service + "/aws4_request";
        String stringToSign = "AWS4-HMAC-SHA256\n" + amzDate + "\n" + credentialScope + "\n" + sha256Hex(canonicalRequest);

        byte[] signingKey = signingKey(creds.secretAccessKey(), date, region, service);
        String signature = hmacHex(signingKey, stringToSign);

        String signedHeaders = String.join(";", canonicalHeaderNames(headers));
        String authHeader = String.format(
                "AWS4-HMAC-SHA256 Credential=%s/%s, SignedHeaders=%s, Signature=%s",
                creds.accessKeyId(), credentialScope, signedHeaders, signature);

        Map<String,String> out = new LinkedHashMap<>(headers);
        out.put("Authorization", authHeader);
        return out;
    }

    private String canonicalRequest(AwsHttpRequest req, Map<String,String> headers){
        String method = req.method().name();
        String canonicalUri = canonicalUri(req);
        String canonicalQuery = canonicalQuery(req);
        String canonicalHeaders = canonicalHeaders(headers);
        String signedHeaders = String.join(";", canonicalHeaderNames(headers));
        String payloadHash = sha256Hex(req.body());
        return method+"\n"+canonicalUri+"\n"+canonicalQuery+"\n"+canonicalHeaders+"\n\n"+signedHeaders+"\n"+payloadHash;
    }

    private String canonicalUri(AwsHttpRequest req){
        String path = req.uri().getRawPath();
        return (path == null || path.isEmpty()) ? "/" : path;
    }

    private String canonicalQuery(AwsHttpRequest req){
        String raw = req.uri().getRawQuery();
        if(raw == null || raw.isEmpty()) return "";
        Map<String,List<String>> params = new TreeMap<>();
        for(String p : raw.split("&")){
            String[] kv = p.split("=",2);
            String k = urlEncode(kv[0]);
            String v = kv.length>1 ? urlEncode(kv[1]) : "";
            params.computeIfAbsent(k, k2 -> new ArrayList<>()).add(v);
        }
        StringBuilder sb = new StringBuilder();
        boolean first=true;
        for(var e: params.entrySet()){
            for(String v: e.getValue()){
                if(!first) sb.append('&');
                sb.append(e.getKey()).append('=').append(v);
                first=false;
            }
        }
        return sb.toString();
    }

    private String canonicalHeaders(Map<String,String> headers){
        var sorted = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(headers);
        StringBuilder sb = new StringBuilder();
        for(var e: sorted.entrySet()){
            sb.append(e.getKey().toLowerCase()).append(':').append(e.getValue().trim()).append('\n');
        }
        return sb.toString();
    }

    private List<String> canonicalHeaderNames(Map<String,String> headers){
        var keys = new ArrayList<String>();
        for(String k: headers.keySet()) keys.add(k.toLowerCase());
        Collections.sort(keys);
        return keys;
    }

    private static String sha256Hex(String s){ return sha256Hex(s.getBytes(StandardCharsets.UTF_8)); }
    private static String sha256Hex(byte[] b){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return bytesToHex(md.digest(b));
        }catch(Exception e){ throw new RuntimeException(e); }
    }

    private static byte[] signingKey(String secret, String date, String region, String service){
        byte[] kDate = hmac(("AWS4"+secret).getBytes(StandardCharsets.UTF_8), date);
        byte[] kRegion = hmac(kDate, region);
        byte[] kService = hmac(kRegion, service);
        return hmac(kService, "aws4_request");
    }
    private static byte[] hmac(byte[] key, String data){
        try{
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        }catch(Exception e){ throw new RuntimeException(e); }
    }
    private static String hmacHex(byte[] key, String data){ return bytesToHex(hmac(key, data)); }
    private static String bytesToHex(byte[] b){
        StringBuilder sb = new StringBuilder();
        for(byte x: b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
    private static String urlEncode(String s){ return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+","%20"); }
}
