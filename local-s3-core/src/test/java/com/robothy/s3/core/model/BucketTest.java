package com.robothy.s3.core.model;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.model.internal.BucketMetadata;
import org.junit.jupiter.api.Test;

class BucketTest {

  @Test
  void fromBucketMetadata() {
    BucketMetadata bucketMetadata = new BucketMetadata();
    bucketMetadata.setBucketName("test");
    bucketMetadata.setCreationDate(1000);
    bucketMetadata.setVersioningEnabled(true);

    Bucket bucket = Bucket.fromBucketMetadata(bucketMetadata);
    assertEquals(bucketMetadata.getBucketName(), bucket.getName());
    assertEquals(bucketMetadata.getCreationDate(), bucket.getCreationDate());
    assertEquals(bucketMetadata.getVersioningEnabled(), bucket.getVersioningEnabled());
  }
}