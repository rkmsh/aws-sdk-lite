package dev.aws.lite.spring;

import dev.aws.lite.core.credentials.*;
import dev.aws.lite.core.http.AwsHttpClient;
import dev.aws.lite.core.sigv4.SigV4Signer;
import dev.aws.lite.s3.S3Client;
import dev.aws.lite.sqs.SqsClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(AwsLiteProperties.class)
public class AwsLiteAutoConfiguration {

    @Bean
    public CredentialsProvider credentialsProvider(AwsLiteProperties p){
        if(p.getAccessKeyId()!=null && p.getSecretAccessKey()!=null){
            return new StaticCredentialsProvider(p.getAccessKeyId(), p.getSecretAccessKey(), p.getSessionToken());
        }
        return new EnvCredentialsProvider();
    }

    @Bean public AwsHttpClient awsHttpClient(){ return new AwsHttpClient(); }
    @Bean public SigV4Signer sigV4Signer(){ return new SigV4Signer(); }

    @Bean
    public S3Client s3Client(AwsLiteProperties p, CredentialsProvider cp, AwsHttpClient http, SigV4Signer signer){
        return new S3Client(p.getRegion(), cp, http, signer);
    }

    @Bean
    public SqsClient sqsClient(AwsLiteProperties p, CredentialsProvider cp, AwsHttpClient http, SigV4Signer signer){
        return new SqsClient(p.getRegion(), cp, http, signer);
    }
}