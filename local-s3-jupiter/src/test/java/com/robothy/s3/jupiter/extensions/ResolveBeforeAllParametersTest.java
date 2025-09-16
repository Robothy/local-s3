package com.robothy.s3.jupiter.extensions;

import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@LocalS3
public class ResolveBeforeAllParametersTest {

  @BeforeAll
  static void setup(LocalS3Endpoint endpoint, S3Client s3Client) throws Exception {

    s3Client.createBucket(b -> b.bucket("my-bucket"));

    try (S3Client clientWithInjectedEndpoint = S3Client.builder().region(Region.of("local"))
      .endpointOverride(new URI(endpoint.endpoint()))
      .forcePathStyle(true)
      .credentialsProvider(AnonymousCredentialsProvider.create())
      .build()) {
      assertDoesNotThrow(() -> clientWithInjectedEndpoint.headBucket(HeadBucketRequest.builder().bucket("my-bucket").build()));
    }
  }

  @AfterAll
  static void cleanup(S3Client s3Client) {
    s3Client.deleteBucket(DeleteBucketRequest.builder().bucket("my-bucket").build());
  }

}
