package com.example.ficketevent.global.config.awsS3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class AwsConfig {

    private final Environment environment;


    @Bean
    public AmazonS3Client amazonS3Client() {
        String accessKey = environment.getProperty("cloud.aws.credentials.access-key");
        String secretKey = environment.getProperty("cloud.aws.credentials.secret-key");
        String region = environment.getProperty("cloud.aws.region.static");

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
}
