package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.BucketAlreadyExistsException;
import com.robothy.s3.core.model.Bucket;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CreateBucketServiceTest extends LocalS3ServiceTestBase {

  @MethodSource("bucketServices")
  @ParameterizedTest
  void createBucket(BucketService bucketService) {
    Bucket bucket1 = bucketService.createBucket("bucket1");
    assertNotNull(bucket1);
    assertEquals("bucket1", bucket1.getName());
    assertTrue(System.currentTimeMillis() - bucket1.getCreationDate() < 2000);
    assertThrows(BucketAlreadyExistsException.class, () -> bucketService.createBucket("bucket1"));

    Bucket bucket2 = bucketService.createBucket("bucket2", "my-region");
    assertNotNull(bucket2);
    assertEquals("bucket2", bucket2.getName());
    assertEquals("my-region", bucket2.getRegion().orElse(null));
  }

}