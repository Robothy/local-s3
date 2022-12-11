package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.robothy.s3.core.exception.ServerSideEncryptionConfigurationNotFoundException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BucketEncryptionServiceTest extends LocalS3ServiceTestBase {

  @MethodSource("bucketServices")
  @ParameterizedTest
  void services(BucketService bucketService) {
    String bucketName = "my-bucket";
    bucketService.createBucket(bucketName);
    assertThrows(ServerSideEncryptionConfigurationNotFoundException.class, () -> bucketService.getBucketEncryption(bucketName));
    assertDoesNotThrow(() -> bucketService.deleteBucketEncryption(bucketName));
    bucketService.putBucketEncryption(bucketName, "Bucket Encryption");
    assertEquals("Bucket Encryption", bucketService.getBucketEncryption(bucketName));
    bucketService.putBucketEncryption(bucketName, "Bucket Encryption 2");
    assertEquals("Bucket Encryption 2", bucketService.getBucketEncryption(bucketName));

    assertDoesNotThrow(() -> bucketService.deleteBucketEncryption(bucketName));
    assertThrows(ServerSideEncryptionConfigurationNotFoundException.class, () -> bucketService.getBucketEncryption(bucketName));
  }

}