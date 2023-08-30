package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketReadLock;
import com.robothy.s3.core.annotations.BucketWriteLock;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.model.internal.BucketMetadata;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Bucket tagging related operations.
 */
public interface BucketTaggingService extends LocalS3MetadataApplicable {

  /**
   * Put tagging to the specified bucket.
   *
   * @param bucketName The name of the bucket for which to set the tagging.
   * @param tagging tagging to the bucket.
   */
  @BucketWriteLock
  default void putTagging(String bucketName, Collection<Map<String, String>> tagging) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    bucketMetadata.setTagging(tagging);
  }

  /**
   * Gets the tagging configuration for the specified bucket.
   *
   * @param bucketName The request object for retrieving the bucket tagging
   * @return tagging of the specified bucket.
   */
  @BucketReadLock
  default Collection<Map<String, String>> getTagging(String bucketName) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    return BucketAssertions.assertBucketTaggingExist(bucketMetadata);
  }

  /**
   * Delete the bucket tagging.
   *
   * @param bucketName the name of the bucket for which to remove the tagging
   */
  @BucketWriteLock
  default void deleteTagging(String bucketName) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    bucketMetadata.setTagging(null);
  }

}
