package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.model.internal.BucketMetadata;

/**
 * Bucket policy service.
 */
public interface BucketPolicyService extends LocalS3MetadataApplicable {

  /**
   * Get policy of the specified bucket.
   *
   * @param bucketName the bucket whose policy is retrieved.
   * @return the bucket policy of the specified bucket.
   */
  default String getBucketPolicy(String bucketName) {
    return BucketAssertions.assertBucketPolicyExist(localS3Metadata(), bucketName);
  }

  /**
   * Put bucket policy to the specified bucket.
   *
   * @param bucketName the bucket whose policy is being set.
   * @param policyJson the policy to apply to the specified bucket.
   */
  @BucketChanged
  default void putBucketPolicy(String bucketName, String policyJson) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    bucketMetadata.setPolicy(policyJson);
  }

  /**
   * Deletes the policy of the specified bucket.
   *
   * @param bucketName the bucket whose policy is being deleted.
   */
  @BucketChanged
  default void deleteBucketPolicy(String bucketName) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    bucketMetadata.setPolicy(null);
  }

}
