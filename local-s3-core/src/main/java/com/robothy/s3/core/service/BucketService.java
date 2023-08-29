package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.annotations.BucketReadLock;
import com.robothy.s3.core.annotations.BucketWriteLock;
import com.robothy.s3.core.model.Bucket;
import java.util.List;
import java.util.stream.Collectors;

public interface BucketService extends CreateBucketService, BucketVersioningService, BucketTaggingService,
    BucketAclService, BucketPolicyService, BucketReplicationService,
    BucketEncryptionService {

  /**
   * Delete a bucket.
   */
  @BucketChanged(type = BucketChanged.Type.DELETE)
  @BucketWriteLock
  Bucket deleteBucket(String bucketName);

  /**
   * Get bucket info.
   */
  @BucketReadLock
  Bucket getBucket(String bucketName);

  /**
   * List all buckets.
   *
   * @return all buckets.
   */
  @BucketReadLock
  default List<Bucket> listBuckets() {
    return localS3Metadata().listBuckets().stream()
        .map(Bucket::fromBucketMetadata).collect(Collectors.toList());
  }

}

