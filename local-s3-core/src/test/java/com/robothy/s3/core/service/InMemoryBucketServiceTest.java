package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.robothy.s3.core.model.Bucket;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import org.junit.jupiter.api.Test;

class InMemoryBucketServiceTest {

  @Test
  void createInstance() {
    final LocalS3Metadata s3Metadata = new LocalS3Metadata();
    BucketMetadata bucketMetadata = new BucketMetadata();
    bucketMetadata.setBucketName("test");
    s3Metadata.addBucketMetadata(bucketMetadata);
    bucketMetadata.setCreationDate(System.currentTimeMillis());
    bucketMetadata.setVersioningEnabled(true);
    BucketService bucketService = InMemoryBucketService.create(s3Metadata);
    assertNotNull(bucketService);
    assertEquals(Bucket.fromBucketMetadata(bucketMetadata), bucketService.getBucket("test"));
  }

}