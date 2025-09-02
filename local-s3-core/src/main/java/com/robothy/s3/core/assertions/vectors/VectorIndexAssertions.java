package com.robothy.s3.core.assertions.vectors;

import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.IndexIdentifier;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorIndexMetadata;

/**
 * Vector index related assertions and utilities.
 */
public class VectorIndexAssertions {

  /**
   * Resolve index information from either indexArn or (vectorBucketName + indexName).
   *
   * @param vectorBucketName the vector bucket name
   * @param indexArn         the index ARN
   * @param indexName        the index name
   * @return index identifier containing bucket name and index name.
   * @throws LocalS3VectorException if required parameters are missing
   */
  public static IndexIdentifier resolveIndexIdentifier(String vectorBucketName, String indexArn, String indexName) {
    if (indexArn != null) {
      return IndexIdentifier.fromIndexArn(indexArn);
    }
    return new IndexIdentifier(vectorBucketName, indexName);
  }

  /**
   * Assert that the vector index exists.
   *
   * @param bucketMetadata bucket metadata to search in
   * @param indexName      index name to validate
   * @return fetched VectorIndexMetadata instance
   * @throws LocalS3VectorException if index doesn't exist
   */
  public static VectorIndexMetadata assertVectorIndexExists(VectorBucketMetadata bucketMetadata, String indexName) {
    VectorIndexMetadata indexMetadata = bucketMetadata.getIndexMetadata(indexName).orElse(null);
    if (indexMetadata == null) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.NOT_FOUND,
          "The specified index could not be found");
    }
    return indexMetadata;
  }

  /**
   * Assert that the vector index does not exist.
   *
   * @param bucketMetadata bucket metadata to search in
   * @param indexName      index name to validate
   * @throws LocalS3VectorException if index already exists
   */
  public static void assertVectorIndexNotExists(VectorBucketMetadata bucketMetadata, String indexName) {
    if (bucketMetadata.getIndexMetadata(indexName).isPresent()) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INDEX_ALREADY_EXISTS,
          "The index already exists");
    }
  }
}
