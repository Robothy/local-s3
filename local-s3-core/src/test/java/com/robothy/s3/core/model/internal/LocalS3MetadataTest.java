package com.robothy.s3.core.model.internal;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.BucketAlreadyExistsException;

import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocalS3MetadataTest {

  @Test
  void listBuckets() {

    LocalS3Metadata s3Metadata = new LocalS3Metadata();
    BucketMetadata bucket1 = new BucketMetadata();
    bucket1.setBucketName("bucket1");
    bucket1.setCreationDate(System.currentTimeMillis());

    BucketMetadata bucket2 = new BucketMetadata();
    bucket2.setBucketName("bucket2");
    bucket2.setCreationDate(System.currentTimeMillis() + 1);

    s3Metadata.addBucketMetadata(bucket1);
    s3Metadata.addBucketMetadata(bucket2);

    assertEquals(s3Metadata.listBuckets(), Arrays.asList(bucket1, bucket2));
    assertEquals(s3Metadata.listBuckets(Comparator.comparing(BucketMetadata::getBucketName).reversed()),
        Arrays.asList(bucket2, bucket1));
  }


  @Test
  void addBucketMetadata() {
    LocalS3Metadata s3Metadata = new LocalS3Metadata();
    BucketMetadata bucket1 = new BucketMetadata();
    bucket1.setBucketName("bucket1");
    bucket1.setCreationDate(System.currentTimeMillis());
    s3Metadata.addBucketMetadata(bucket1);

    BucketMetadata bucket2 = new BucketMetadata();
    bucket2.setBucketName("bucket1");
    bucket2.setCreationDate(System.currentTimeMillis());
    Assertions.assertThrows(BucketAlreadyExistsException.class, () -> s3Metadata.addBucketMetadata(bucket2));
  }



}