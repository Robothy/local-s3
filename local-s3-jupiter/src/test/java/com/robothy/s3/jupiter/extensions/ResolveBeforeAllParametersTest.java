package com.robothy.s3.jupiter.extensions;

import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@LocalS3
public class ResolveBeforeAllParametersTest {

  private static LocalS3Endpoint endpoint;

  private static S3Client s3Client;

  @BeforeAll
  static void setup(LocalS3Endpoint endpoint, S3Client s3Client) throws Exception {
    ResolveBeforeAllParametersTest.endpoint = endpoint;
    ResolveBeforeAllParametersTest.s3Client = s3Client;

    s3Client.createBucket(b -> b.bucket("my-bucket"));

    S3Client clientWithInjectedEndpoint = S3Client.builder().region(Region.of("local"))
      .endpointOverride(new URI(endpoint.endpoint()))
      .forcePathStyle(true)
      .credentialsProvider(AnonymousCredentialsProvider.create())
      .build();
    assertDoesNotThrow(() -> clientWithInjectedEndpoint.headBucket(HeadBucketRequest.builder().bucket("my-bucket").build()));
  }

  @Test
  void testInjectedInstancesShareTheSameEndpoint(LocalS3Endpoint endpoint, S3Client s3Client) {
    assertEquals(ResolveBeforeAllParametersTest.endpoint, endpoint);
    assertDoesNotThrow(() -> s3Client.headBucket(HeadBucketRequest.builder().bucket("my-bucket").build()));
  }

  @AfterAll
  static void cleanup(LocalS3Endpoint endpoint, S3Client s3Client) {
    assertEquals(ResolveBeforeAllParametersTest.endpoint, endpoint);
    s3Client.deleteBucket(DeleteBucketRequest.builder().bucket("my-bucket").build());
  }

}
