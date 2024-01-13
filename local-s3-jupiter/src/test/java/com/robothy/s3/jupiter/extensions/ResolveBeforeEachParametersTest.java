package com.robothy.s3.jupiter.extensions;

import com.amazonaws.services.s3.AmazonS3;
import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@LocalS3
public class ResolveBeforeEachParametersTest {

  private LocalS3Endpoint endpoint;

  @BeforeEach
  void beforeEach(LocalS3Endpoint endpoint, AmazonS3 s3, S3Client s3Client) {
    this.endpoint = endpoint;
    assertDoesNotThrow(() -> s3.createBucket(new com.amazonaws.services.s3.model.CreateBucketRequest("my-bucket")));
    assertDoesNotThrow(() -> s3Client.headBucket(builder -> builder.bucket("my-bucket")));
  }

  @Order(1)
  @Test
  void testInjectedInstancesShareTheSameEndpoint(LocalS3Endpoint endpoint, AmazonS3 s3, S3Client s3Client) {
    assertEquals(this.endpoint, endpoint);
    assertDoesNotThrow(() -> s3.headBucket(new com.amazonaws.services.s3.model.HeadBucketRequest("my-bucket")));
    assertDoesNotThrow(() -> s3Client.headBucket(builder -> builder.bucket("my-bucket")));
  }

  @Order(2)
  @Test
  void testInjectedInstancesShareTheSameEndpoint2(LocalS3Endpoint endpoint, AmazonS3 s3, S3Client s3Client) {
    assertEquals(this.endpoint, endpoint);
    assertDoesNotThrow(() -> s3.headBucket(new com.amazonaws.services.s3.model.HeadBucketRequest("my-bucket")));
    assertDoesNotThrow(() -> s3Client.headBucket(builder -> builder.bucket("my-bucket")));
  }

  @AfterEach
  void afterEach(LocalS3Endpoint endpoint, AmazonS3 s3, S3Client client) {
    assertEquals(this.endpoint, endpoint);
    assertDoesNotThrow(() -> s3.headBucket(new com.amazonaws.services.s3.model.HeadBucketRequest("my-bucket")));
    assertDoesNotThrow(() -> client.headBucket(HeadBucketRequest.builder().bucket("my-bucket").build()));
    assertDoesNotThrow(() -> client.deleteBucket(builder -> builder.bucket("my-bucket")));
  }

}
