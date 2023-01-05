package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.model.Bucket;
import java.util.List;
import java.util.stream.Collectors;

public interface BucketService extends BucketVersioningService, BucketTaggingService,
    BucketAclService, BucketPolicyService, BucketReplicationService,
    BucketEncryptionService {

  /**
   * Create a bucket.
   */
  @BucketChanged(type = BucketChanged.Type.CREATE)
  Bucket createBucket(String bucketName);

  /**
   * Delete a bucket.
   */
  @BucketChanged(type = BucketChanged.Type.DELETE)
  Bucket deleteBucket(String bucketName);

  /**
   * Get bucket info.
   */
  Bucket getBucket(String bucketName);

  /**
   * Put a bucket settings. Call {@linkplain BucketService#getSetting(String, String)} will return the setting back.
   * @param bucketName the bucket name.
   * @param key setting key.
   * @param value setting value.
   */
  void putSetting(String bucketName, String key, String value);

  /**
   * Get a bucket setting.
   */
  String getSetting(String bucketName, String key);

  /**
   * List all buckets.
   *
   * @return all buckets.
   */
  default List<Bucket> listBuckets() {
    return localS3Metadata().listBuckets().stream()
        .map(Bucket::fromBucketMetadata).collect(Collectors.toList());
  }

}

