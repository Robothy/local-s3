package com.robothy.s3.jupiter.extensions;

import com.amazonaws.services.s3.AmazonS3;
import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@LocalS3
public class ResolveBeforeAllParametersTest {

  private static LocalS3Endpoint endpoint;

  private static AmazonS3 s3;

  private static S3Client s3Client;

  @BeforeAll
  static void setup(LocalS3Endpoint endpoint, AmazonS3 s3, S3Client s3Client) throws Exception {
    ResolveBeforeAllParametersTest.endpoint = endpoint;
    ResolveBeforeAllParametersTest.s3 = s3;
    ResolveBeforeAllParametersTest.s3Client = s3Client;

    s3.createBucket("my-bucket");
    assertDoesNotThrow(() -> s3Client.headBucket(HeadBucketRequest.builder().bucket("my-bucket").build()));

    S3Client clientWithInjectedEndpoint = S3Client.builder().region(Region.of("local"))
      .endpointOverride(new URI(endpoint.endpoint()))
      .forcePathStyle(true)
      .credentialsProvider(AnonymousCredentialsProvider.create())
      .build();
    assertDoesNotThrow(() -> clientWithInjectedEndpoint.headBucket(HeadBucketRequest.builder().bucket("my-bucket").build()));
  }

  @Test
  void testInjectedInstancesShareTheSameEndpoint(LocalS3Endpoint endpoint, AmazonS3 s3, S3Client s3Client) {
    assertEquals(ResolveBeforeAllParametersTest.endpoint, endpoint);
    assertDoesNotThrow(() -> s3.headBucket(new com.amazonaws.services.s3.model.HeadBucketRequest("my-bucket")));
    assertDoesNotThrow(() -> s3Client.headBucket(b -> b.bucket("my-bucket")));
  }

  @AfterAll
  static void cleanup(LocalS3Endpoint endpoint, AmazonS3 s3, S3Client s3Client) {
    assertEquals(ResolveBeforeAllParametersTest.endpoint, endpoint);
    assertDoesNotThrow(() -> s3.headBucket(new com.amazonaws.services.s3.model.HeadBucketRequest("my-bucket")));
    assertDoesNotThrow(() -> s3Client.headBucket(HeadBucketRequest.builder().bucket("my-bucket").build()));
    assertDoesNotThrow(() -> s3Client.deleteBucket(builder -> builder.bucket("my-bucket")));
  }

}
