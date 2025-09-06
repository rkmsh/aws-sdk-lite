package dev.aws.lite.demo.controller;

import dev.aws.lite.s3.S3Client;
import dev.aws.lite.sqs.SqsClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class Api {
    private final S3Client s3;
    private final SqsClient sqs;
    public Api(S3Client s3, SqsClient sqs) { this.s3 = s3; this.sqs = sqs; }

    @PostMapping("/s3/{bucket}/{key}")
    public String put(@PathVariable String bucket, @PathVariable String key, @RequestBody byte[] body) throws Exception {
        s3.putObject(bucket, key, body, "application/octet-stream");
        return "OK";
    }

    @GetMapping("/s3/{bucket}/{key}")
    public byte[] get(@PathVariable String bucket, @PathVariable String key) throws Exception {
        return s3.getObject(bucket, key);
    }

    @PostMapping("/sqs/send")
    public String send(@RequestParam String queueUrl, @RequestParam String body) throws Exception {
        sqs.sendMessage(queueUrl, body);
        return "SENT";
    }
}