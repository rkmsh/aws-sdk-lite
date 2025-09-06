package dev.aws.lite.core.credentials;

public final class EnvCredentialsProvider implements CredentialsProvider {
    @Override
    public AwsCredentials resolveCredentials() {
        String ak = System.getenv("AWS_ACCESS_KEY_ID");
        String sk = System.getenv("AWS_SECRET_ACCESS_KEY");
        String st = System.getenv("AWS_SESSION_TOKEN");
        if (ak == null || sk == null || st == null) throw new IllegalStateException("AWS env Credentials not set");
        final String fak = ak, fsk = sk, fst = st;
        return new AwsCredentials() {
            public String accessKeyId() {
                return fak;
            }

            public String secretAccessKey() {
                return fsk;
            }

            public String sessionToken() {
                return fst;
            }
        };
    }
}
