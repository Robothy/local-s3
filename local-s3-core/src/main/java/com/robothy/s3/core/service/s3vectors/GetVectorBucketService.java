package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.datatypes.s3vectors.VectorBucket;

public interface GetVectorBucketService extends S3VectorsMetadataAware {

  default VectorBucket getVectorBucket(String bucketName) {
    VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this, bucketName);
    return buildVectorBucket(bucketMetadata);
  }

  private VectorBucket buildVectorBucket(VectorBucketMetadata bucketMetadata) {
    return VectorBucket.builder()
        .vectorBucketName(bucketMetadata.getVectorBucketName())
        .arn(VectorBucket.generateArn(bucketMetadata.getVectorBucketName()))
        .creationTime(java.time.Instant.ofEpochMilli(bucketMetadata.getCreationDate()))
        .encryptionConfiguration(bucketMetadata.getEncryptionConfiguration().orElse(null))
        .build();
  }

}
