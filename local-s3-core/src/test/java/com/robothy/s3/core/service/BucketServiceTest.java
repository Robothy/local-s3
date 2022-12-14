package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.core.exception.BucketAlreadyExistsException;
import com.robothy.s3.core.exception.BucketNotExistException;
import com.robothy.s3.core.model.Bucket;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BucketServiceTest extends LocalS3ServiceTestBase {


  @MethodSource("bucketServices")
  @ParameterizedTest
  void createBucket(BucketService bucketService) {
    Bucket bucket1 = bucketService.createBucket("bucket1");
    assertNotNull(bucket1);
    assertEquals("bucket1", bucket1.getName());
    assertTrue(System.currentTimeMillis() - bucket1.getCreationDate() < 1000);
    assertThrows(BucketAlreadyExistsException.class, () -> bucketService.createBucket("bucket1"));
  }

  @MethodSource("bucketServices")
  @ParameterizedTest
  void deleteBucket(BucketService bucketService) {
    assertThrows(BucketNotExistException.class, () -> bucketService.deleteBucket("bucket"));
    Bucket bucket = bucketService.createBucket("bucket");
    Bucket deletedBucket = bucketService.deleteBucket("bucket");
    assertEquals(bucket, deletedBucket);
  }

  @MethodSource("bucketServices")
  @ParameterizedTest
  void getBucket(BucketService bucketService) {
    assertThrows(BucketNotExistException.class, () -> bucketService.getBucket("bucket"));
    Bucket bucket = bucketService.createBucket("bucket");
    Bucket getBucket = bucketService.getBucket("bucket");
    assertEquals(bucket, getBucket);
  }

  @MethodSource("bucketServices")
  @ParameterizedTest
  void setVersioningEnabled(BucketService bucketService) {
    String bucketName = "bucket";
    Bucket bucket = bucketService.createBucket(bucketName);
    assertNull(bucket.getVersioningEnabled());

    bucketService.setVersioningEnabled(bucketName, true);
    bucket = bucketService.getBucket(bucketName);
    assertTrue(bucket.getVersioningEnabled());

    bucketService.setVersioningEnabled(bucketName, false);
    bucket = bucketService.getBucket(bucketName);
    assertFalse(bucket.getVersioningEnabled());
  }

}