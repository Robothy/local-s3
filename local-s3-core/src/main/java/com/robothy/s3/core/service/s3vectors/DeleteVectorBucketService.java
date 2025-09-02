package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;

public interface DeleteVectorBucketService extends S3VectorsMetadataAware {

  @BucketChanged(type = BucketChanged.Type.DELETE)
  default void deleteVectorBucket(String bucketName) {
    VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this, bucketName);
    validateBucketCanBeDeleted(bucketMetadata);
    removeBucketFromMetadata(bucketName);
  }

  private void validateBucketCanBeDeleted(VectorBucketMetadata bucketMetadata) {
    if (!bucketMetadata.getIndexes().isEmpty()) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
          "Cannot delete vector bucket that still contains indexes.");
    }
  }

  private void removeBucketFromMetadata(String bucketName) {
    metadata().getVectorBucketMetadataMap().remove(bucketName);
  }

}
