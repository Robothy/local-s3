package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.regions.Region;
import com.robothy.s3.jupiter.LocalS3;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

public class GetBucketLocationIntegrationTest {

  @Test
  @LocalS3
  void getBucketLocation(S3Client s3) {
    assertThrows(NoSuchBucketException.class, () -> s3.getBucketLocation(b -> b.bucket("non-exist-bucket")));
    CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
        .bucket("bucket")
        .createBucketConfiguration(builder -> builder.locationConstraint(Region.AP_EAST_1.id()))
        .build();
    s3.createBucket(createBucketRequest);
    assertNotNull(s3.headBucket(builder -> builder.bucket("bucket").build()));
    assertEquals(Region.AP_EAST_1.id(), s3.getBucketLocation(builder -> builder.bucket("bucket").build()).locationConstraintAsString());
  }

}
