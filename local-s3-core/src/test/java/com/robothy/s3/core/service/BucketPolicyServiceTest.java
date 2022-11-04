package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.robothy.s3.core.exception.BucketNotExistException;
import com.robothy.s3.core.exception.BucketPolicyNotExistException;
import com.robothy.s3.core.model.Bucket;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BucketPolicyServiceTest extends LocalS3ServiceTestBase {

  @ParameterizedTest
  @MethodSource("bucketServices")
  void bucketPolicy(BucketService bucketService) {

    assertThrows(BucketNotExistException.class, () -> bucketService.getBucketPolicy("not-exist-bucket"));
    Bucket bucket = bucketService.createBucket("my-bucket");
    assertThrows(BucketPolicyNotExistException.class, () -> bucketService.getBucketPolicy(bucket.getName()));
    String policyJson = "policy JSON text";
    bucketService.putBucketPolicy(bucket.getName(), policyJson);
    String bucketPolicy = bucketService.getBucketPolicy(bucket.getName());
    assertEquals(policyJson, bucketPolicy);

    assertThrows(BucketNotExistException.class, () -> bucketService.deleteBucketPolicy("not-exist-policy"));
    assertDoesNotThrow(() -> bucketService.deleteBucketPolicy(bucket.getName()));
    assertThrows(BucketPolicyNotExistException.class, () -> bucketService.getBucketPolicy(bucket.getName()));
  }

}