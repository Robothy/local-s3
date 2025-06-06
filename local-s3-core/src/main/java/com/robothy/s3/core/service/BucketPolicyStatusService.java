package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketReadLock;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.datatypes.PolicyStatus;
import com.robothy.s3.datatypes.PublicAccessBlockConfiguration;
import java.util.Optional;

/**
 * Service to get the policy status for a bucket.
 */
public interface BucketPolicyStatusService extends LocalS3MetadataApplicable {

  /**
   * Get policy status of the bucket.
   * A bucket has 'IsPublic' when:
   * - It has a policy that makes it public
   * - It has public access blocks disabled (we consider this public for simplicity)
   *
   * @param bucketName bucket name.
   * @return policy status of the bucket.
   */
  @BucketReadLock
  default PolicyStatus getBucketPolicyStatus(String bucketName) {
    BucketAssertions.assertBucketNameIsValid(bucketName);
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    
    // For simplicity, we consider a bucket potentially public if:
    // - It has a policy (which might make it public), or
    // - It has public access blocks disabled or not set
    boolean hasPolicy = bucketMetadata.getPolicy().isPresent();
    
    Optional<PublicAccessBlockConfiguration> publicAccessBlock = bucketMetadata.getPublicAccessBlock();
    boolean hasBlocksEnabled = publicAccessBlock.isPresent() && 
                              (Boolean.TRUE.equals(publicAccessBlock.get().getBlockPublicPolicy()) ||
                               Boolean.TRUE.equals(publicAccessBlock.get().getRestrictPublicBuckets()));

    // A bucket is considered public if it has any policy and doesn't have blocks enabled
    boolean isPublic = hasPolicy && !hasBlocksEnabled;
    
    return PolicyStatus.builder()
        .isPublic(isPublic)
        .build();
  }
}
