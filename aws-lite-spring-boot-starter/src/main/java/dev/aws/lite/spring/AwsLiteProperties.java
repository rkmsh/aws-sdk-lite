package dev.aws.lite.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.lite")
public class AwsLiteProperties {
    private String region = "us-east-1";
    private String accessKeyId;
    private String secretAccessKey;
    private String sessionToken;
    public String getRegion(){ return region; }
    public void setRegion(String r){ this.region = r; }
    public String getAccessKeyId(){ return accessKeyId; }
    public void setAccessKeyId(String v){ this.accessKeyId = v; }
    public String getSecretAccessKey(){ return secretAccessKey; }
    public void setSecretAccessKey(String v){ this.secretAccessKey = v; }
    public String getSessionToken(){ return sessionToken; }
    public void setSessionToken(String v){ this.sessionToken = v; }
}