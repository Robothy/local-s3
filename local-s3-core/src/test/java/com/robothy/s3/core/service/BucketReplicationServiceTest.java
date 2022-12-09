package com.robothy.s3.core.service;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.robothy.s3.core.exception.BucketReplicationNotExistException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BucketReplicationServiceTest extends LocalS3ServiceTestBase {

  @MethodSource("bucketServices")
  @ParameterizedTest(name = "Bucket Replication Service {index}.")
  void testBucketReplication(BucketService bucketService) {
    String bucketName = "my-bucket";
    bucketService.createBucket(bucketName);
    assertThrows(BucketReplicationNotExistException.class, () -> bucketService.getBucketReplication(bucketName));
    assertDoesNotThrow(() -> bucketService.deleteBucketReplication(bucketName));
    bucketService.putBucketReplication(bucketName, "Replication Configuration");
    assertEquals("Replication Configuration", bucketService.getBucketReplication(bucketName));

    assertDoesNotThrow(() -> bucketService.deleteBucketReplication(bucketName));
    assertThrows(BucketReplicationNotExistException.class, () -> bucketService.getBucketReplication(bucketName));
    assertDoesNotThrow(() -> bucketService.deleteBucketReplication(bucketName));
  }

}