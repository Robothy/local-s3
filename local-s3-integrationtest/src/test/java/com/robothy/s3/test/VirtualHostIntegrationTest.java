package com.robothy.s3.test;

import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class VirtualHostIntegrationTest {

  @Test
  @LocalS3
  void testVirtualHostWithLocalEndpoint(LocalS3Endpoint endpoint) throws URISyntaxException {
    S3Client s3Client = S3Client.builder()
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("accessKeyId", "secretAccessKey")))
        .httpClientBuilder(ApacheHttpClient.builder()
            .dnsResolver(host -> InetAddress.getAllByName("localhost"))
            .connectionTimeout(Duration.ofSeconds(1))
            .socketTimeout(Duration.ofSeconds(1))
        )
        .endpointOverride(new URI(endpoint.endpoint()))
        .region(Region.of(endpoint.region()))
        .build();
    s3Client.createBucket(b -> b.bucket("my-bucket"));
    assertDoesNotThrow(() -> s3Client.headBucket(b -> b.bucket("my-bucket")));
  }

  @Test
  @LocalS3
  void testVirtualHostWithAwsLegacyGlobalEndpoint(LocalS3Endpoint endpoint) throws URISyntaxException {
    S3Client s3Client = S3Client.builder()
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("accessKeyId", "secretAccess")))
        .httpClientBuilder(ApacheHttpClient.builder()
            .dnsResolver(host -> InetAddress.getAllByName("localhost"))
            .connectionTimeout(Duration.ofSeconds(1))
            .socketTimeout(Duration.ofSeconds(1))
        )
        .endpointOverride(new URI("http://s3.amazonaws.com:" + endpoint.port()))
        .region(Region.of("local"))
        .build();
    s3Client.createBucket(b -> b.bucket("my-bucket"));
    assertDoesNotThrow(() -> s3Client.headBucket(b -> b.bucket("my-bucket")));
  }

  @Test
  @LocalS3
  void testVirtualHostWithAwsEndpoint(LocalS3Endpoint endpoint) throws URISyntaxException {
    S3Client s3Client = S3Client.builder()
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("accessKeyId", "secretAccess")))
        .httpClientBuilder(ApacheHttpClient.builder()
            .dnsResolver(host -> InetAddress.getAllByName("localhost"))
            .connectionTimeout(Duration.ofSeconds(1))
            .socketTimeout(Duration.ofSeconds(1))
        )
        .endpointOverride(new URI("http://s3.amazonaws.com:" + endpoint.port()))
        .region(Region.of("local"))
        .build();
    s3Client.createBucket(b -> b.bucket("my-bucket"));
    assertDoesNotThrow(() -> s3Client.headBucket(b -> b.bucket("my-bucket")));
  }

}
