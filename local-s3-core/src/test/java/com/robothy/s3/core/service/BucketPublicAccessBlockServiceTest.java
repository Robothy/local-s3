package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.datatypes.PolicyStatus;
import com.robothy.s3.datatypes.PublicAccessBlockConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BucketPublicAccessBlockServiceTest {
  
  private final LocalS3Metadata localS3Metadata = new LocalS3Metadata();
  
  private final BucketService bucketService = new BucketServiceMock(localS3Metadata);

  @BeforeEach
  void setup() {
    bucketService.createBucket("test-bucket", "us-east-1");
  }

  @Test
  void testPutAndGetPublicAccessBlock() {
    PublicAccessBlockConfiguration config = PublicAccessBlockConfiguration.builder()
        .blockPublicAcls(true)
        .ignorePublicAcls(true)
        .blockPublicPolicy(false)
        .restrictPublicBuckets(false)
        .build();
    
    bucketService.putPublicAccessBlock("test-bucket", config);
    
    assertTrue(bucketService.getPublicAccessBlock("test-bucket").isPresent());
    PublicAccessBlockConfiguration retrieved = bucketService.getPublicAccessBlock("test-bucket").get();
    
    assertEquals(config.getBlockPublicAcls(), retrieved.getBlockPublicAcls());
    assertEquals(config.getIgnorePublicAcls(), retrieved.getIgnorePublicAcls());
    assertEquals(config.getBlockPublicPolicy(), retrieved.getBlockPublicPolicy());
    assertEquals(config.getRestrictPublicBuckets(), retrieved.getRestrictPublicBuckets());
  }
  
  @Test
  void testDeletePublicAccessBlock() {
    PublicAccessBlockConfiguration config = new PublicAccessBlockConfiguration();
    config.setBlockPublicAcls(true);
    
    // Set configuration
    bucketService.putPublicAccessBlock("test-bucket", config);
    assertTrue(bucketService.getPublicAccessBlock("test-bucket").isPresent());
    
    // Delete configuration
    bucketService.deletePublicAccessBlock("test-bucket");
    assertFalse(bucketService.getPublicAccessBlock("test-bucket").isPresent());
  }
  private static class BucketServiceMock implements BucketService {
    private final LocalS3Metadata localS3Metadata;
    
    public BucketServiceMock(LocalS3Metadata localS3Metadata) {
      this.localS3Metadata = localS3Metadata;
    }
    
    @Override
    public LocalS3Metadata localS3Metadata() {
      return localS3Metadata;
    }
    
    @Override
    public com.robothy.s3.core.model.Bucket getBucket(String bucketName) {
      return com.robothy.s3.core.model.Bucket.builder()
          .name(bucketName)
          .creationDate(System.currentTimeMillis())
          .build();
    }
    
    @Override
    public com.robothy.s3.core.model.Bucket deleteBucket(String bucketName) {
      throw new UnsupportedOperationException("Not implemented for test");
    }
    
    @Override
    public Boolean getVersioningEnabled(String bucketName) {
      return null; // Return null as default (versioning not set)
    }
    
    @Override
    public com.robothy.s3.core.model.Bucket setVersioningEnabled(String bucketName, boolean versioningEnabled) {
      return getBucket(bucketName);
    }
  }
}
