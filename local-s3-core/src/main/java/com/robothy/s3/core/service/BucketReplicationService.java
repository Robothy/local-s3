package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.annotations.BucketReadLock;
import com.robothy.s3.core.annotations.BucketWriteLock;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.model.internal.BucketMetadata;

/**
 * Bucket replication configuration service. Put/Get/Delete bucket configuration.
 */
public interface BucketReplicationService extends LocalS3MetadataApplicable {

  /**
   * Put bucket replication configuration to the specified bucket.
   *
   * @param bucketName bucket that the replication configuration applies to.
   * @param replicationConfig replication configuration. LocalS3 only stores it, won't parse it.
   */
  @BucketChanged
  @BucketWriteLock
  default void putBucketReplication(String bucketName, String replicationConfig) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    bucketMetadata.setReplication(replicationConfig);
  }

  /**
   * Get bucket configuration of the specified bucket.
   *
   * @param bucketName the bucket where the replication configuration is fetched.
   * @return bucket configuration.
   */
  @BucketReadLock
  default String getBucketReplication(String bucketName) {
    return BucketAssertions.assertBucketReplicationExist(localS3Metadata(), bucketName);
  }

  /**
   * Delete the replication configuration of the specified bucket.
   *
   * @param bucketName the bucket name.
   */
  @BucketChanged
  @BucketWriteLock
  default void deleteBucketReplication(String bucketName) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    bucketMetadata.setReplication(null);
  }

}
