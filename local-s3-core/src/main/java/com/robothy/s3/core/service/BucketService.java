package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.model.Bucket;
import java.util.List;
import java.util.stream.Collectors;

public interface BucketService extends BucketTaggingService,
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
   * Set if versioning enabled of a given bucket.
   *
   * @param bucketName the bucket name.
   * @param versioningEnabled if enable versioning.
   * @return bucket info.
   */
  Bucket setVersioningEnabled(String bucketName, boolean versioningEnabled);

  /**
   * Get versioning enabled by bucket name.
   *
   * @param bucketName bucket name.
   * @return {@code null} - if not set versioning;
   * {@code true} - if versioning enabled;
   * {@code false} - if versioning disabled.
   */
  Boolean getVersioningEnabled(String bucketName);

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

