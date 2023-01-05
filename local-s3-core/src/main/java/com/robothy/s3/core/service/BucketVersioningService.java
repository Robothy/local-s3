package com.robothy.s3.core.service;

import com.robothy.s3.core.model.Bucket;

public interface BucketVersioningService {

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

}
