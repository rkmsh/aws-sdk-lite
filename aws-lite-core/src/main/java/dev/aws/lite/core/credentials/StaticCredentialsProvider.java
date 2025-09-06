package dev.aws.lite.core.credentials;

public final class StaticCredentialsProvider implements CredentialsProvider {

    private final AwsCredentials creds;

    public StaticCredentialsProvider(String accessKey, String secret, String token) {
        this.creds = new AwsCredentials() {
            public String accessKeyId() {
                return accessKey;
            }

            public String secretAccessKey() {
                return secret;
            }

            public String sessionToken() {
                return token;
            }
        };
    }

    @Override
    public AwsCredentials resolveCredentials() {
        return creds;
    }
}
