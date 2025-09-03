package com.robothy.s3.core.service.s3vectors;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.assertions.vectors.VectorBucketAssertions;
import com.robothy.s3.core.assertions.vectors.VectorIndexAssertions;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;

public interface DeleteIndexService extends S3VectorsMetadataAware {

  @BucketChanged
  default void deleteIndex(String vectorBucketName, String indexName) {
    VectorBucketMetadata bucketMetadata = VectorBucketAssertions.assertVectorBucketExists(this, vectorBucketName);
    VectorIndexAssertions.assertVectorIndexExists(bucketMetadata, indexName);
    bucketMetadata.removeIndexMetadata(indexName);
  }
}
