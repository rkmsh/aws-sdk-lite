package dev.aws.lite.core.credentials;

public interface CredentialsProvider {
    AwsCredentials resolveCredentials();
}
