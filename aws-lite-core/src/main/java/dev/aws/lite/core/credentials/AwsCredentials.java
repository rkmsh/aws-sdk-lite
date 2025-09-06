package dev.aws.lite.core.credentials;

public interface AwsCredentials {
    String accessKeyId();
    String secretAccessKey();
    default String sessionToken() {return null;}
}
